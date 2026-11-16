package com.example.bypassyou.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.bypassyou.data.repository.SettingsRepository

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "BootReceiver received action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val settings = SettingsRepository(context)
            if (settings.isBypassEnabled) {
                try {
                    BypassForegroundService.start(context)
                    BypassTileService.requestTileUpdate(context)
                    Log.i(TAG, "Successfully auto-started BypassForegroundService after boot/update")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to auto-start BypassForegroundService on boot", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
