package openhoangnc.browser.browser

import android.os.Handler
import android.os.Message
import openhoangnc.browser.view.NinjaWebView

class NinjaClickHandler(private val webView: NinjaWebView) : Handler() {
    override fun handleMessage(message: Message) {
        super.handleMessage(message)
        webView.getBrowserController()?.onLongPress(message.data?.getString("url"))
    }

}