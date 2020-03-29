package openhoangnc.browser.browser

import android.app.Activity
import android.content.Context
import android.webkit.DownloadListener
import openhoangnc.browser.unit.BrowserUnit
import openhoangnc.browser.unit.IntentUnit

class NinjaDownloadListener(private val context: Context?) : DownloadListener {
    override fun onDownloadStart(url: String, userAgent: String, contentDisposition: String, mimeType: String, contentLength: Long) {
        val holder = IntentUnit.context
        if (holder !is Activity) {
            BrowserUnit.download(context, url, contentDisposition, mimeType)
            return
        }
    }

}