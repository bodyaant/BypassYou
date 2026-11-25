package com.example.bypassyou.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.bypassyou.data.model.CallEventLog
import org.json.JSONArray
import org.json.JSONObject

class EventLogRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getLogs(): List<CallEventLog> {
        val jsonString = prefs.getString(KEY_LOGS, "[]") ?: "[]"
        val list = mutableListOf<CallEventLog>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    CallEventLog(
                        id = obj.optString("id", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        phoneNumber = obj.optString("phoneNumber", ""),
                        contactName = obj.optString("contactName", ""),
                        isBypassed = obj.optBoolean("isBypassed", false),
                        reason = obj.optString("reason", ""),
                        volumePercent = obj.optInt("volumePercent", 100)
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing event logs", e)
        }
        return list
    }

    fun addLog(log: CallEventLog) {
        val current = getLogs().toMutableList()
        // CallScreeningService and PhoneCallReceiver both report the same ringing call.
        val last = current.firstOrNull()
        if (last != null && last.phoneNumber == log.phoneNumber &&
            log.timestamp - last.timestamp in 0 until DUPLICATE_WINDOW_MS
        ) {
            return
        }
        current.add(0, log)
        val trimmed = if (current.size > MAX_LOGS) current.take(MAX_LOGS) else current
        saveLogs(trimmed)
    }

    fun clearLogs() {
        prefs.edit().remove(KEY_LOGS).apply()
    }

    private fun saveLogs(logs: List<CallEventLog>) {
        val jsonArray = JSONArray()
        logs.forEach { log ->
            val obj = JSONObject().apply {
                put("id", log.id)
                put("timestamp", log.timestamp)
                put("phoneNumber", log.phoneNumber)
                put("contactName", log.contactName)
                put("isBypassed", log.isBypassed)
                put("reason", log.reason)
                put("volumePercent", log.volumePercent)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_LOGS, jsonArray.toString()).apply()
    }

    companion object {
        private const val TAG = "EventLogRepository"
        private const val PREFS_NAME = "bypass_you_event_logs"
        private const val KEY_LOGS = "event_logs_list"
        private const val MAX_LOGS = 50
        private const val DUPLICATE_WINDOW_MS = 5_000L
    }
}
