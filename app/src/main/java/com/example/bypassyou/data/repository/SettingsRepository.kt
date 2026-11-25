package com.example.bypassyou.data.repository

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun registerChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) =
        prefs.registerOnSharedPreferenceChangeListener(listener)

    fun unregisterChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) =
        prefs.unregisterOnSharedPreferenceChangeListener(listener)

    var isBypassEnabled: Boolean
        get() = prefs.getBoolean(KEY_BYPASS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_BYPASS_ENABLED, value).apply()

    var targetVolumePercent: Int
        get() = prefs.getInt(KEY_TARGET_VOLUME, 100)
        set(value) = prefs.edit().putInt(KEY_TARGET_VOLUME, value.coerceIn(0, 100)).apply()

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    var isStarredContactsBypassEnabled: Boolean
        get() = prefs.getBoolean(KEY_STARRED_BYPASS, false)
        set(value) = prefs.edit().putBoolean(KEY_STARRED_BYPASS, value).apply()

    var isAscendingVolumeEnabled: Boolean
        get() = prefs.getBoolean(KEY_ASCENDING_VOLUME, false)
        set(value) = prefs.edit().putBoolean(KEY_ASCENDING_VOLUME, value).apply()

    var ascendingDurationSec: Int
        get() = prefs.getInt(KEY_ASCENDING_DURATION, 6)
        set(value) = prefs.edit().putInt(KEY_ASCENDING_DURATION, value.coerceIn(2, 20)).apply()

    var isScheduleEnabled: Boolean
        get() = prefs.getBoolean(KEY_SCHEDULE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SCHEDULE_ENABLED, value).apply()

    var scheduleStartHour: Int
        get() = prefs.getInt(KEY_SCHEDULE_START_HOUR, 23)
        set(value) = prefs.edit().putInt(KEY_SCHEDULE_START_HOUR, value.coerceIn(0, 23)).apply()

    var scheduleStartMinute: Int
        get() = prefs.getInt(KEY_SCHEDULE_START_MINUTE, 0)
        set(value) = prefs.edit().putInt(KEY_SCHEDULE_START_MINUTE, value.coerceIn(0, 59)).apply()

    var scheduleEndHour: Int
        get() = prefs.getInt(KEY_SCHEDULE_END_HOUR, 7)
        set(value) = prefs.edit().putInt(KEY_SCHEDULE_END_HOUR, value.coerceIn(0, 23)).apply()

    var scheduleEndMinute: Int
        get() = prefs.getInt(KEY_SCHEDULE_END_MINUTE, 0)
        set(value) = prefs.edit().putInt(KEY_SCHEDULE_END_MINUTE, value.coerceIn(0, 59)).apply()

    var scheduleDays: Set<String>
        get() = prefs.getStringSet(KEY_SCHEDULE_DAYS, setOf("1", "2", "3", "4", "5", "6", "7"))
            ?: setOf("1", "2", "3", "4", "5", "6", "7")
        set(value) = prefs.edit().putStringSet(KEY_SCHEDULE_DAYS, value).apply()

    companion object {
        private const val PREFS_NAME = "bypass_you_settings"
        private const val KEY_BYPASS_ENABLED = "key_bypass_enabled"
        private const val KEY_TARGET_VOLUME = "key_target_volume"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_STARRED_BYPASS = "key_starred_bypass"
        private const val KEY_ASCENDING_VOLUME = "key_ascending_volume"
        private const val KEY_ASCENDING_DURATION = "key_ascending_duration"
        private const val KEY_SCHEDULE_ENABLED = "key_schedule_enabled"
        private const val KEY_SCHEDULE_START_HOUR = "key_schedule_start_hour"
        private const val KEY_SCHEDULE_START_MINUTE = "key_schedule_start_minute"
        private const val KEY_SCHEDULE_END_HOUR = "key_schedule_end_hour"
        private const val KEY_SCHEDULE_END_MINUTE = "key_schedule_end_minute"
        private const val KEY_SCHEDULE_DAYS = "key_schedule_days"
    }
}
