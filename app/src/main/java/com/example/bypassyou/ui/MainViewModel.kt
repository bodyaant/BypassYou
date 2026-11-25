package com.example.bypassyou.ui

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.bypassyou.data.model.CallEventLog
import com.example.bypassyou.data.model.VipContact
import com.example.bypassyou.data.repository.EventLogRepository
import com.example.bypassyou.data.repository.SettingsRepository
import com.example.bypassyou.data.repository.VipRepository
import com.example.bypassyou.service.AudioBypassManager
import com.example.bypassyou.service.BypassForegroundService
import com.example.bypassyou.service.BypassTileService
import com.example.bypassyou.util.SystemIntegrityHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val vipRepository = VipRepository(application)
    private val settingsRepository = SettingsRepository(application)
    private val eventLogRepository = EventLogRepository(application)
    private val audioBypassManager = AudioBypassManager.getInstance(application)

    private val audioManager = application.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager

    val maxRingVolumeIndex: Int = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_RING).let { if (it <= 0) 15 else it }
    val minRingVolumeIndex: Int = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
        audioManager.getStreamMinVolume(android.media.AudioManager.STREAM_RING).coerceAtLeast(0)
    } else {
        0
    }

    private val _vipContacts = MutableStateFlow<List<VipContact>>(emptyList())
    val vipContacts: StateFlow<List<VipContact>> = _vipContacts.asStateFlow()

    private val _eventLogs = MutableStateFlow<List<CallEventLog>>(emptyList())
    val eventLogs: StateFlow<List<CallEventLog>> = _eventLogs.asStateFlow()

    private val _isBypassEnabled = MutableStateFlow(settingsRepository.isBypassEnabled)
    val isBypassEnabled: StateFlow<Boolean> = _isBypassEnabled.asStateFlow()

    private val _targetVolumePercent = MutableStateFlow(
        snapToPreset(settingsRepository.targetVolumePercent, maxRingVolumeIndex, minRingVolumeIndex)
    )
    val targetVolumePercent: StateFlow<Int> = _targetVolumePercent.asStateFlow()

    private val _themeMode = MutableStateFlow(settingsRepository.themeMode)
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _isStarredContactsEnabled = MutableStateFlow(settingsRepository.isStarredContactsBypassEnabled)
    val isStarredContactsEnabled: StateFlow<Boolean> = _isStarredContactsEnabled.asStateFlow()

    private val _starredContactsCount = MutableStateFlow(0)
    val starredContactsCount: StateFlow<Int> = _starredContactsCount.asStateFlow()

    private val _isAscendingVolumeEnabled = MutableStateFlow(settingsRepository.isAscendingVolumeEnabled)
    val isAscendingVolumeEnabled: StateFlow<Boolean> = _isAscendingVolumeEnabled.asStateFlow()

    private val _ascendingDurationSec = MutableStateFlow(settingsRepository.ascendingDurationSec)
    val ascendingDurationSec: StateFlow<Int> = _ascendingDurationSec.asStateFlow()

    private val _isScheduleEnabled = MutableStateFlow(settingsRepository.isScheduleEnabled)
    val isScheduleEnabled: StateFlow<Boolean> = _isScheduleEnabled.asStateFlow()

    private val _scheduleStartHour = MutableStateFlow(settingsRepository.scheduleStartHour)
    val scheduleStartHour: StateFlow<Int> = _scheduleStartHour.asStateFlow()

    private val _scheduleStartMinute = MutableStateFlow(settingsRepository.scheduleStartMinute)
    val scheduleStartMinute: StateFlow<Int> = _scheduleStartMinute.asStateFlow()

    private val _scheduleEndHour = MutableStateFlow(settingsRepository.scheduleEndHour)
    val scheduleEndHour: StateFlow<Int> = _scheduleEndHour.asStateFlow()

    private val _scheduleEndMinute = MutableStateFlow(settingsRepository.scheduleEndMinute)
    val scheduleEndMinute: StateFlow<Int> = _scheduleEndMinute.asStateFlow()

    private val _scheduleDays = MutableStateFlow(settingsRepository.scheduleDays)
    val scheduleDays: StateFlow<Set<String>> = _scheduleDays.asStateFlow()

    // System Health Matrix State
    private val _isIgnoringBatteryOpt = MutableStateFlow(false)
    val isIgnoringBatteryOpt: StateFlow<Boolean> = _isIgnoringBatteryOpt.asStateFlow()

    private val _isDndAccessGranted = MutableStateFlow(false)
    val isDndAccessGranted: StateFlow<Boolean> = _isDndAccessGranted.asStateFlow()

    private val _isCallScreeningRoleHeld = MutableStateFlow(false)
    val isCallScreeningRoleHeld: StateFlow<Boolean> = _isCallScreeningRoleHeld.asStateFlow()

    private val _hasPhonePermissions = MutableStateFlow(false)
    val hasPhonePermissions: StateFlow<Boolean> = _hasPhonePermissions.asStateFlow()

    private val _hasContactsPermission = MutableStateFlow(false)
    val hasContactsPermission: StateFlow<Boolean> = _hasContactsPermission.asStateFlow()

    // The Quick Settings tile writes isBypassEnabled directly; keep the hero switch in sync.
    // Held in a field because SharedPreferences keeps listeners only weakly.
    private val prefsListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        _isBypassEnabled.value = settingsRepository.isBypassEnabled
    }

    init {
        settingsRepository.registerChangeListener(prefsListener)
        loadVipContacts()
        refreshEventLogs()
        refreshSystemIntegrity()

        val snapped = snapToPreset(settingsRepository.targetVolumePercent, maxRingVolumeIndex, minRingVolumeIndex)
        if (snapped != settingsRepository.targetVolumePercent) {
            settingsRepository.targetVolumePercent = snapped
        }

        if (settingsRepository.isBypassEnabled) {
            try {
                BypassForegroundService.start(getApplication())
            } catch (e: Exception) {
                Log.w("MainViewModel", "Could not start ForegroundService automatically", e)
            }
        }
    }

    fun refreshSystemIntegrity() {
        val app = getApplication<Application>()
        _isIgnoringBatteryOpt.value = SystemIntegrityHelper.isIgnoringBatteryOptimizations(app)
        _isDndAccessGranted.value = SystemIntegrityHelper.isDndPolicyAccessGranted(app)
        _isCallScreeningRoleHeld.value = SystemIntegrityHelper.isCallScreeningRoleHeld(app)
        _hasPhonePermissions.value = SystemIntegrityHelper.hasPhonePermissions(app)
        _hasContactsPermission.value = SystemIntegrityHelper.hasContactsPermission(app)
        _starredContactsCount.value = SystemIntegrityHelper.getStarredContactsCount(app)
        _isStarredContactsEnabled.value = settingsRepository.isStarredContactsBypassEnabled
    }

    fun refreshEventLogs() {
        _eventLogs.value = eventLogRepository.getLogs()
    }

    fun clearEventLogs() {
        eventLogRepository.clearLogs()
        _eventLogs.value = emptyList()
    }

    fun loadVipContacts() {
        _vipContacts.value = vipRepository.getVipContacts()
        BypassTileService.requestTileUpdate(getApplication())
    }

    fun toggleBypassEnabled(enabled: Boolean) {
        settingsRepository.isBypassEnabled = enabled
        _isBypassEnabled.value = enabled

        try {
            if (enabled) {
                BypassForegroundService.start(getApplication())
            } else {
                BypassForegroundService.stop(getApplication())
            }
        } catch (e: Exception) {
            Log.w("MainViewModel", "Error toggling foreground service", e)
        }
        BypassTileService.requestTileUpdate(getApplication())
    }

    fun setTargetVolumePercent(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        val snapped = snapToPreset(clamped, maxRingVolumeIndex, minRingVolumeIndex)
        settingsRepository.targetVolumePercent = snapped
        _targetVolumePercent.value = snapped
        audioBypassManager.updatePreviewVolume(snapped)
    }

    fun setThemeMode(mode: String) {
        settingsRepository.themeMode = mode
        _themeMode.value = mode
    }

    fun toggleStarredBypass(enabled: Boolean) {
        settingsRepository.isStarredContactsBypassEnabled = enabled
        _isStarredContactsEnabled.value = enabled
        refreshSystemIntegrity()
    }

    fun toggleAscendingVolume(enabled: Boolean) {
        settingsRepository.isAscendingVolumeEnabled = enabled
        _isAscendingVolumeEnabled.value = enabled
    }

    fun setAscendingDuration(seconds: Int) {
        val clamped = seconds.coerceIn(2, 20)
        settingsRepository.ascendingDurationSec = clamped
        _ascendingDurationSec.value = clamped
    }

    fun toggleSchedule(enabled: Boolean) {
        settingsRepository.isScheduleEnabled = enabled
        _isScheduleEnabled.value = enabled
    }

    fun setScheduleTime(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        settingsRepository.scheduleStartHour = startHour
        settingsRepository.scheduleStartMinute = startMinute
        settingsRepository.scheduleEndHour = endHour
        settingsRepository.scheduleEndMinute = endMinute

        _scheduleStartHour.value = startHour
        _scheduleStartMinute.value = startMinute
        _scheduleEndHour.value = endHour
        _scheduleEndMinute.value = endMinute
    }

    fun toggleScheduleDay(day: String) {
        val current = _scheduleDays.value.toMutableSet()
        if (current.contains(day)) {
            if (current.size > 1) {
                current.remove(day)
            }
        } else {
            current.add(day)
        }
        settingsRepository.scheduleDays = current
        _scheduleDays.value = current
    }

    fun requestIgnoreBatteryOptimizations() {
        SystemIntegrityHelper.requestIgnoreBatteryOptimizations(getApplication())
    }

    fun openDndSettings() {
        SystemIntegrityHelper.openDndPolicySettings(getApplication())
    }

    fun removeVipContact(contactId: String) {
        vipRepository.removeVipContact(contactId)
        loadVipContacts()
    }

    fun addManualContact(name: String, number: String) {
        val cleanNumber = number.trim()
        if (cleanNumber.isNotEmpty()) {
            val contact = VipContact(
                id = UUID.randomUUID().toString(),
                name = name.trim().ifBlank { cleanNumber },
                phoneNumber = cleanNumber
            )
            vipRepository.addVipContact(contact)
            loadVipContacts()
        }
    }

    fun addContactFromLog(log: CallEventLog) {
        val contact = VipContact(
            id = UUID.randomUUID().toString(),
            name = log.contactName.ifBlank { log.phoneNumber },
            phoneNumber = log.phoneNumber
        )
        vipRepository.addVipContact(contact)
        loadVipContacts()
    }

    fun processPickedContactUri(contactUri: Uri) {
        val resolver: ContentResolver = getApplication<Application>().contentResolver
        var name = ""
        var phoneNumber = ""
        var photoUri: String? = null

        try {
            resolver.query(
                contactUri,
                arrayOf(
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER
                ),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val photoIdx = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)
                    val hasPhoneIdx = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                    val contactId = if (idIdx >= 0) cursor.getString(idIdx) else null
                    if (nameIdx >= 0) name = cursor.getString(nameIdx) ?: ""
                    if (photoIdx >= 0) photoUri = cursor.getString(photoIdx)
                    val hasPhone = if (hasPhoneIdx >= 0) cursor.getInt(hasPhoneIdx) else 0

                    if (hasPhone > 0 && !contactId.isNullOrEmpty()) {
                        resolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                            arrayOf(contactId),
                            null
                        )?.use { phoneCursor ->
                            if (phoneCursor.moveToFirst()) {
                                val numIdx = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                if (numIdx >= 0) {
                                    phoneNumber = phoneCursor.getString(numIdx) ?: ""
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error in primary contact query", e)
        }

        if (phoneNumber.isEmpty()) {
            try {
                resolver.query(contactUri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        for (i in 0 until cursor.columnCount) {
                            val colName = cursor.getColumnName(i).lowercase()
                            if (colName.contains("data1") || colName.contains("number") || colName.contains("phone")) {
                                val v = cursor.getString(i)
                                if (!v.isNullOrEmpty() && v.any { it.isDigit() }) {
                                    phoneNumber = v
                                    break
                                }
                            }
                        }
                        if (name.isEmpty()) {
                            val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                            if (nameIdx >= 0) name = cursor.getString(nameIdx) ?: ""
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error in fallback contact query", e)
            }
        }

        if (phoneNumber.isNotBlank()) {
            val vip = VipContact(
                id = UUID.randomUUID().toString(),
                name = name.ifBlank { phoneNumber },
                phoneNumber = phoneNumber,
                photoUri = photoUri
            )
            vipRepository.addVipContact(vip)
            loadVipContacts()
        }
    }

    override fun onCleared() {
        settingsRepository.unregisterChangeListener(prefsListener)
    }

    fun playSoundPreview() {
        audioBypassManager.playPreviewSound()
    }

    private fun snapToPreset(percent: Int, maxSteps: Int, minSteps: Int): Int {
        val safeMax = if (maxSteps <= 0) 15 else maxSteps
        val safeMin = minSteps.coerceIn(0, safeMax - 1)
        val step = ((percent / 100f) * safeMax).let { kotlin.math.round(it).toInt() }.coerceIn(safeMin, safeMax)
        return ((step.toFloat() / safeMax.toFloat()) * 100f).let { kotlin.math.round(it).toInt() }
    }
}
