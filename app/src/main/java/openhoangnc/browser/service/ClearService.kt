package openhoangnc.browser.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.preference.PreferenceManager
import openhoangnc.browser.Ninja.R
import openhoangnc.browser.unit.BrowserUnit

class ClearService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        System.exit(0) // For remove all WebView thread
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        clear()
        stopSelf()
        return START_STICKY
    }

    private fun clear() {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val clearCache = sp.getBoolean(getString(R.string.sp_clear_cache), false)
        val clearCookie = sp.getBoolean(getString(R.string.sp_clear_cookie), false)
        val clearHistory = sp.getBoolean(getString(R.string.sp_clear_history), false)
        val clearIndexedDB = sp.getBoolean("sp_clearIndexedDB", false)
        if (clearCache) {
            BrowserUnit.clearCache(this)
        }
        if (clearCookie) {
            BrowserUnit.clearCookie()
        }
        if (clearHistory) {
            BrowserUnit.clearHistory(this)
        }
        if (clearIndexedDB) {
            BrowserUnit.clearIndexedDB(this)
        }
    }
}