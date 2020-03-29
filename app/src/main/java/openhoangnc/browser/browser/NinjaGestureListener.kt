package openhoangnc.browser.browser

import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import openhoangnc.browser.view.NinjaWebView

class NinjaGestureListener(private val webView: NinjaWebView) : SimpleOnGestureListener() {
    private var longPress = true
    override fun onLongPress(e: MotionEvent) {
        if (longPress) {
            webView.onLongPress()
        }
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        longPress = false
        return false
    }

    override fun onShowPress(e: MotionEvent) {
        longPress = true
    }

}