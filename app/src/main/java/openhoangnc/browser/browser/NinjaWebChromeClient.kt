package openhoangnc.browser.browser

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Message
import android.view.View
import android.webkit.GeolocationPermissions
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import openhoangnc.browser.unit.HelperUnit
import openhoangnc.browser.view.NinjaWebView

class NinjaWebChromeClient(private val ninjaWebView: NinjaWebView) : WebChromeClient() {
    override fun onCreateWindow(view: WebView, dialog: Boolean, userGesture: Boolean, resultMsg: Message): Boolean {
        val result = view.hitTestResult
        val data = result.extra
        val context = view.context
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(data))
        context.startActivity(browserIntent)
        return false
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        ninjaWebView.update(newProgress)
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        ninjaWebView.update(title)
    }

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        ninjaWebView.getBrowserController()?.onShowCustomView(view, callback)
        super.onShowCustomView(view, callback)
    }

    override fun onHideCustomView() {
        ninjaWebView.getBrowserController()?.onHideCustomView()
        super.onHideCustomView()
    }

    override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: FileChooserParams): Boolean {
        ninjaWebView.getBrowserController()?.showFileChooser(filePathCallback)
        return true
    }

    override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
        val activity = ninjaWebView.context as Activity
        HelperUnit.grantPermissionsLoc(activity)
        callback.invoke(origin, true, false)
        super.onGeolocationPermissionsShowPrompt(origin, callback)
    }

}