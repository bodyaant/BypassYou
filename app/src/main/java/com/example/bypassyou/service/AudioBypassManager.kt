package com.example.bypassyou.service

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import com.example.bypassyou.R
import com.example.bypassyou.data.model.CallEventLog
import com.example.bypassyou.data.repository.EventLogRepository
import com.example.bypassyou.data.repository.SettingsRepository
import com.example.bypassyou.data.repository.VipRepository
import com.example.bypassyou.util.SystemIntegrityHelper
import java.util.UUID
import kotlin.math.pow

class AudioBypassManager private constructor(context: Context) {

    private val appContext: Context = context.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    private val vipRepository = VipRepository(appContext)
    private val settingsRepository = SettingsRepository(appContext)
    private val eventLogRepository = EventLogRepository(appContext)

    private val mainHandler = Handler(Looper.getMainLooper())

    private var isBypassingNow = false
    private var wakeLock: PowerManager.WakeLock? = null
    private var mediaPlayer: MediaPlayer? = null
    private var activePreviewMp: MediaPlayer? = null

    private var originalRingerMode: Int? = null
    private var originalRingVolume: Int? = null

    private var ascendingRunnable: Runnable? = null

    private fun calculateAcousticVolume(percent: Int): Float {
        val p = (percent.coerceIn(5, 100) / 100.0)
        val factor = ((10.0.pow(p) - 1.0) / 9.0).toFloat()
        return factor.coerceIn(0.01f, 1.0f)
    }

    @Synchronized
    fun handleIncomingCall(incomingNumber: String?) {
        Log.d(TAG, "handleIncomingCall called for number: '$incomingNumber'")

        if (incomingNumber.isNullOrEmpty()) {
            return
        }

        val contactName = SystemIntegrityHelper.getContactDisplayName(appContext, incomingNumber)
            ?: incomingNumber

        if (!settingsRepository.isBypassEnabled) {
            logCallEvent(
                number = incomingNumber,
                name = contactName,
                isBypassed = false,
                reason = appContext.getString(R.string.event_log_reason_disabled),
                volume = 0
            )
            return
        }

        if (!SystemIntegrityHelper.isScheduleActiveNow(settingsRepository)) {
            logCallEvent(
                number = incomingNumber,
                name = contactName,
                isBypassed = false,
                reason = appContext.getString(R.string.event_log_reason_outside_schedule),
                volume = 0
            )
            return
        }

        val isVip = vipRepository.isVipNumber(incomingNumber)
        val isStarred = settingsRepository.isStarredContactsBypassEnabled &&
                SystemIntegrityHelper.isStarredContactNumber(appContext, incomingNumber)

        if (!isVip && !isStarred) {
            logCallEvent(
                number = incomingNumber,
                name = contactName,
                isBypassed = false,
                reason = appContext.getString(R.string.event_log_reason_regular_call),
                volume = 0
            )
            return
        }

        val triggerReason = when {
            isVip && isStarred -> appContext.getString(R.string.event_log_reason_vip_and_starred)
            isVip -> appContext.getString(R.string.event_log_reason_vip)
            else -> appContext.getString(R.string.event_log_reason_starred)
        }

        logCallEvent(
            number = incomingNumber,
            name = contactName,
            isBypassed = true,
            reason = triggerReason,
            volume = settingsRepository.targetVolumePercent
        )

        performSoundBypass()
    }

    private fun logCallEvent(
        number: String,
        name: String,
        isBypassed: Boolean,
        reason: String,
        volume: Int
    ) {
        val event = CallEventLog(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            phoneNumber = number,
            contactName = name,
            isBypassed = isBypassed,
            reason = reason,
            volumePercent = volume
        )
        eventLogRepository.addLog(event)
    }

    private fun isSystemAlreadyRinging(): Boolean {
        if (audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
            return false
        }
        val filter = notificationManager?.currentInterruptionFilter ?: NotificationManager.INTERRUPTION_FILTER_ALL
        val isDndSuppressed = filter != NotificationManager.INTERRUPTION_FILTER_ALL &&
                filter != NotificationManager.INTERRUPTION_FILTER_UNKNOWN
        if (isDndSuppressed) {
            return false
        }
        val currentRingVol = audioManager.getStreamVolume(AudioManager.STREAM_RING)
        return currentRingVol > 0
    }

    private fun performSoundBypass() {
        if (isBypassingNow) {
            return
        }

        try {
            isBypassingNow = true
            acquireWakeLock()

            if (isSystemAlreadyRinging()) {
                // System is already ringing normally; adjust volume to target without playing redundant backup stream
                applyRingerVolumeAdjustment()
            } else {
                // System is silent, vibrating, or suppressed by DND; play guaranteed alarm-usage bypass audio stream
                playBackupRingtone()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in performSoundBypass", e)
        }
    }

    private fun applyRingerVolumeAdjustment() {
        try {
            if (originalRingVolume == null) {
                originalRingVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING)
            }
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
            val targetRingVol = ((settingsRepository.targetVolumePercent / 100f) * maxVol)
                .let { kotlin.math.round(it).toInt() }
                .coerceIn(1, maxVol)
            audioManager.setStreamVolume(AudioManager.STREAM_RING, targetRingVol, 0)
        } catch (e: Exception) {
            Log.w(TAG, "Could not apply ringer volume adjustment", e)
        }
    }

    @Synchronized
    fun restoreOriginalAudioState() {
        if (!isBypassingNow) {
            return
        }

        try {
            stopBackupRingtone()
            restoreOriginalVolumeState()
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring audio state", e)
        } finally {
            isBypassingNow = false
            releaseWakeLock()
        }
    }

    private fun restoreOriginalVolumeState() {
        try {
            originalRingVolume?.let { vol ->
                audioManager.setStreamVolume(AudioManager.STREAM_RING, vol, 0)
            }
            originalRingerMode?.let { mode ->
                if (notificationManager?.isNotificationPolicyAccessGranted == true) {
                    audioManager.ringerMode = mode
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not restore original ringer volume", e)
        } finally {
            originalRingerMode = null
            originalRingVolume = null
        }
    }

    private fun playBackupRingtone() {
        try {
            stopBackupRingtone()
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

            val targetVolume = settingsRepository.targetVolumePercent
            val isAscending = settingsRepository.isAscendingVolumeEnabled
            val startPercent = if (isAscending) 15 else targetVolume
            val initialAcoustic = calculateAcousticVolume(startPercent)

            val mp = MediaPlayer().apply {
                setDataSource(appContext, ringtoneUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setVolume(initialAcoustic, initialAcoustic)
                isLooping = true
                prepare()
                start()
            }
            mediaPlayer = mp

            if (isAscending && targetVolume > startPercent) {
                startAscendingVolumeRamp(mp, startPercent, targetVolume, settingsRepository.ascendingDurationSec)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play backup ringtone", e)
        }
    }

    private fun startAscendingVolumeRamp(
        mp: MediaPlayer,
        startPercent: Int,
        targetPercent: Int,
        durationSec: Int
    ) {
        cancelAscendingVolumeRamp()

        val steps = 10
        val stepIntervalMs = (durationSec * 1000L) / steps
        val volumeStep = (targetPercent - startPercent) / steps.toFloat()

        var currentStep = 0

        val runnable = object : Runnable {
            override fun run() {
                if (!isBypassingNow || mediaPlayer != mp) return

                currentStep++
                val nextPercent = (startPercent + (volumeStep * currentStep)).toInt().coerceAtMost(targetPercent)
                val acoustic = calculateAcousticVolume(nextPercent)

                try {
                    if (mp.isPlaying) {
                        mp.setVolume(acoustic, acoustic)
                    }
                } catch (e: Exception) {
                    return
                }

                if (currentStep < steps) {
                    mainHandler.postDelayed(this, stepIntervalMs)
                }
            }
        }
        ascendingRunnable = runnable
        mainHandler.postDelayed(runnable, stepIntervalMs)
    }

    private fun cancelAscendingVolumeRamp() {
        ascendingRunnable?.let { mainHandler.removeCallbacks(it) }
        ascendingRunnable = null
    }

    private fun stopBackupRingtone() {
        cancelAscendingVolumeRamp()
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping backup MediaPlayer", e)
        } finally {
            mediaPlayer = null
        }
    }

    @Synchronized
    fun stopPreviewSound() {
        try {
            activePreviewMp?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping preview sound", e)
        } finally {
            activePreviewMp = null
        }
    }

    @Synchronized
    fun playPreviewSound() {
        try {
            stopPreviewSound()
            stopBackupRingtone()

            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

            val acousticVolume = calculateAcousticVolume(settingsRepository.targetVolumePercent)

            val previewMp = MediaPlayer().apply {
                setDataSource(appContext, ringtoneUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setVolume(acousticVolume, acousticVolume)
                prepare()
                start()
            }
            activePreviewMp = previewMp

            mainHandler.postDelayed({
                synchronized(this@AudioBypassManager) {
                    if (activePreviewMp == previewMp) {
                        stopPreviewSound()
                    }
                }
            }, 3500)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound preview", e)
        }
    }

    @Synchronized
    fun updatePreviewVolume(percent: Int) {
        val acousticVolume = calculateAcousticVolume(percent)
        activePreviewMp?.let { mp ->
            try {
                if (mp.isPlaying) {
                    mp.setVolume(acousticVolume, acousticVolume)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock == null) {
                wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "BypassYou:RingingWakeLock"
                ).apply {
                    setReferenceCounted(false)
                }
            }
            wakeLock?.acquire(60_000L)
        } catch (e: Exception) {
            Log.e(TAG, "Failed acquiring WakeLock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed releasing WakeLock", e)
        }
    }

    companion object {
        private const val TAG = "AudioBypassManager"

        @Volatile
        private var instance: AudioBypassManager? = null

        fun getInstance(context: Context): AudioBypassManager {
            return instance ?: synchronized(this) {
                instance ?: AudioBypassManager(context).also { instance = it }
            }
        }
    }
}
