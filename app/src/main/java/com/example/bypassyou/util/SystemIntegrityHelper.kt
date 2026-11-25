package com.example.bypassyou.util

import android.Manifest
import android.app.NotificationManager
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.ContactsContract
import android.provider.Settings
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.bypassyou.data.repository.SettingsRepository
import com.example.bypassyou.data.repository.VipRepository
import java.util.Calendar

object SystemIntegrityHelper {

    private const val TAG = "SystemIntegrityHelper"

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            ?: return true
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun requestIgnoreBatteryOptimizations(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to open battery optimization settings", e2)
            }
        }
    }

    fun isDndPolicyAccessGranted(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return false
        return notificationManager.isNotificationPolicyAccessGranted
    }

    fun openDndPolicySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed opening DND settings", e)
        }
    }

    fun isCallScreeningRoleHeld(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            return roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
        }
        return true
    }

    fun hasPhonePermissions(context: Context): Boolean {
        val hasState = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        val hasCallLog = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
        val hasContacts = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        return hasState && hasCallLog && hasContacts
    }

    fun hasContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    fun isStarredContactNumber(context: Context, rawNumber: String?): Boolean {
        if (rawNumber.isNullOrBlank()) return false
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CONTACTS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        val resolver = context.contentResolver

        try {
            val cursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.STARRED,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                ),
                "${ContactsContract.CommonDataKinds.Phone.STARRED} = 1",
                null,
                null
            )

            cursor?.use {
                val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (it.moveToNext()) {
                    if (numIdx >= 0) {
                        val num = it.getString(numIdx) ?: ""
                        if (VipRepository.isSamePhoneNumber(rawNumber, num)) {
                            return true
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking starred contacts", e)
        }
        return false
    }

    fun getStarredContactsCount(context: Context): Int {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CONTACTS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return 0
        }
        try {
            val cursor = context.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                arrayOf(ContactsContract.Contacts._ID),
                "${ContactsContract.Contacts.STARRED} = 1 AND ${ContactsContract.Contacts.HAS_PHONE_NUMBER} = 1",
                null,
                null
            )
            return cursor?.use { it.count } ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting starred contacts count", e)
            return 0
        }
    }

    fun getContactDisplayName(context: Context, rawNumber: String?): String? {
        if (rawNumber.isNullOrBlank()) return null
        try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(rawNumber)
            )
            context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    if (idx >= 0) {
                        return cursor.getString(idx)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error looking up contact name", e)
        }
        return null
    }

    fun isScheduleActiveNow(settings: SettingsRepository): Boolean {
        if (!settings.isScheduleEnabled) return true
        val calendar = Calendar.getInstance()
        return isScheduleActive(
            dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK),
            currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE),
            startMinutes = settings.scheduleStartHour * 60 + settings.scheduleStartMinute,
            endMinutes = settings.scheduleEndHour * 60 + settings.scheduleEndMinute,
            days = settings.scheduleDays
        )
    }

    /**
     * [dayOfWeek] is a [Calendar.DAY_OF_WEEK] value; [days] uses "1"=Mon .. "7"=Sun.
     * For an overnight range (e.g. Fri 23:00 to 07:00) the morning part belongs to the
     * day the range started on, so it is checked against yesterday.
     */
    internal fun isScheduleActive(
        dayOfWeek: Int,
        currentMinutes: Int,
        startMinutes: Int,
        endMinutes: Int,
        days: Set<String>
    ): Boolean {
        // Calendar: 1=Sun..7=Sat -> 1=Mon..7=Sun
        fun dayKey(calendarDay: Int) = (((calendarDay + 5) % 7) + 1).toString()
        val today = dayKey(dayOfWeek)
        val yesterday = dayKey(if (dayOfWeek == Calendar.SUNDAY) Calendar.SATURDAY else dayOfWeek - 1)

        return if (startMinutes <= endMinutes) {
            today in days && currentMinutes in startMinutes..endMinutes
        } else {
            (today in days && currentMinutes >= startMinutes) ||
                (yesterday in days && currentMinutes <= endMinutes)
        }
    }
}
