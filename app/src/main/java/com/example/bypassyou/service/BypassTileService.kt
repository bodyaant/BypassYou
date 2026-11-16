package com.example.bypassyou.service

import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.example.bypassyou.R
import com.example.bypassyou.data.repository.SettingsRepository
import com.example.bypassyou.data.repository.VipRepository

class BypassTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val settings = SettingsRepository(this)
        val newState = !settings.isBypassEnabled
        settings.isBypassEnabled = newState

        if (newState) {
            try {
                BypassForegroundService.start(this)
            } catch (e: Exception) {
                Log.e(TAG, "Error starting service from tile", e)
            }
        } else {
            try {
                BypassForegroundService.stop(this)
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping service from tile", e)
            }
        }

        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val settings = SettingsRepository(this)
        val isEnabled = settings.isBypassEnabled
        val vipCount = VipRepository(this).getVipContacts().size

        tile.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.icon = Icon.createWithResource(this, R.drawable.ic_qs_bypass)
        tile.label = getString(R.string.app_name)
        tile.subtitle = if (isEnabled) getString(R.string.tile_subtitle_active, vipCount) else getString(R.string.tile_subtitle_inactive)
        tile.updateTile()
    }

    companion object {
        private const val TAG = "BypassTileService"

        fun requestTileUpdate(context: Context) {
            try {
                requestListeningState(
                    context,
                    ComponentName(context, BypassTileService::class.java)
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed requesting tile state update", e)
            }
        }
    }
}
