package openhoangnc.browser.browser

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Message
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import openhoangnc.browser.Ninja.R
import openhoangnc.browser.database.Record
import openhoangnc.browser.database.RecordAction
import openhoangnc.browser.unit.BrowserUnit
import openhoangnc.browser.unit.HelperUnit
import openhoangnc.browser.unit.IntentUnit
import openhoangnc.browser.view.NinjaToast
import openhoangnc.browser.view.NinjaWebView
import java.io.ByteArrayInputStream
import java.net.URISyntaxException

class NinjaWebViewClient(private val ninjaWebView: NinjaWebView) : WebViewClient() {
    private val context: Context = ninjaWebView.context
    private val sp: SharedPreferences
    private val adBlock: AdBlock?
    private val cookie: Cookie?
    private val white: Boolean
    private var enable: Boolean
    fun enableAdBlock(enable: Boolean) {
        this.enable = enable
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        if (sp.getBoolean("saveHistory", true)) {
            val action = RecordAction(context)
            action.open(true)
            if (action.checkHistory(url)) {
                action.deleteHistoryItemByURL(url)
                action.addHistory(Record(ninjaWebView.title, url, System.currentTimeMillis()))
            } else {
                action.addHistory(Record(ninjaWebView.title, url, System.currentTimeMillis()))
            }
            action.close()
        }
        if (ninjaWebView.isForeground) {
            ninjaWebView.invalidate()
        } else {
            ninjaWebView.postInvalidate()
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        val uri = Uri.parse(url)
        return handleUri(view, uri)
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val uri = request.url
        return handleUri(view, uri)
    }

    private fun handleUri(webView: WebView, uri: Uri): Boolean {
        val url = uri.toString()
        val parsedUri = Uri.parse(url)
        val packageManager = context.packageManager
        val browseIntent = Intent(Intent.ACTION_VIEW).setData(parsedUri)
        if (url.startsWith("http")) {
            webView.loadUrl(url, ninjaWebView.requestHeaders)
            return true
        }
        if (browseIntent.resolveActivity(packageManager) != null) {
            context.startActivity(browseIntent)
            return true
        }
        if (url.startsWith("intent:")) {
            try {
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                if (intent.resolveActivity(context.packageManager) != null) {
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        NinjaToast.show(context, R.string.toast_load_error)
                    }
                    return true
                }
                //try to find fallback url
                val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                if (fallbackUrl != null) {
                    webView.loadUrl(fallbackUrl)
                    return true
                }
                //invite to install
                val marketIntent = Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=" + intent.getPackage()))
                if (marketIntent.resolveActivity(packageManager) != null) {
                    context.startActivity(marketIntent)
                    return true
                }
            } catch (e: URISyntaxException) { //not an intent uri
                return false
            }
        }
        return true //do nothing in other cases
    }

    // TODO: this fun should be removed due to Deprecated
    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        if (enable && !white && adBlock!!.isAd(url)) {
            return WebResourceResponse(
                    BrowserUnit.MIME_TYPE_TEXT_PLAIN,
                    BrowserUnit.URL_ENCODING,
                    ByteArrayInputStream("".toByteArray())
            )
        }
        if (!sp.getBoolean(context.getString(R.string.sp_cookies), true)) {
            if (cookie!!.isWhite(url)) {
                val manager = CookieManager.getInstance()
                manager.getCookie(url)
                manager.setAcceptCookie(true)
            } else {
                val manager = CookieManager.getInstance()
                manager.setAcceptCookie(false)
            }
        }
        return super.shouldInterceptRequest(view, url)
    }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        if (enable && !white && adBlock!!.isAd(request.url.toString())) {
            return WebResourceResponse(
                    BrowserUnit.MIME_TYPE_TEXT_PLAIN,
                    BrowserUnit.URL_ENCODING,
                    ByteArrayInputStream("".toByteArray())
            )
        }
        if (!sp.getBoolean(context.getString(R.string.sp_cookies), true)) {
            if (cookie!!.isWhite(request.url.toString())) {
                val manager = CookieManager.getInstance()
                manager.getCookie(request.url.toString())
                manager.setAcceptCookie(true)
            } else {
                val manager = CookieManager.getInstance()
                manager.setAcceptCookie(false)
            }
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun onFormResubmission(view: WebView, doNotResend: Message, resend: Message) {
        val holder = IntentUnit.context as? Activity ?: return
        val dialog = BottomSheetDialog(holder)
        val dialogView = View.inflate(holder, R.layout.dialog_action, null)
        val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
        textView.setText(R.string.dialog_content_resubmission)
        val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
        action_ok.setOnClickListener {
            resend.sendToTarget()
            dialog.cancel()
        }
        val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
        action_cancel.setOnClickListener {
            doNotResend.sendToTarget()
            dialog.cancel()
        }
        dialog.setContentView(dialogView)
        dialog.show()
        HelperUnit.setBottomSheetBehavior(dialog, dialogView, BottomSheetBehavior.STATE_EXPANDED)
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        var message = "\"SSL Certificate error.\""
        when (error.primaryError) {
            SslError.SSL_UNTRUSTED -> message = "\"Certificate authority is not trusted.\""
            SslError.SSL_EXPIRED -> message = "\"Certificate has expired.\""
            SslError.SSL_IDMISMATCH -> message = "\"Certificate Hostname mismatch.\""
            SslError.SSL_NOTYETVALID -> message = "\"Certificate is not yet valid.\""
            SslError.SSL_DATE_INVALID -> message = "\"Certificate date is invalid.\""
            SslError.SSL_INVALID -> message = "\"Certificate is invalid.\""
        }
        val text = message + " - " + context.getString(R.string.dialog_content_ssl_error)
        val dialog = BottomSheetDialog(context)
        val dialogView = View.inflate(context, R.layout.dialog_action, null)
        val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
        textView.text = text
        val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
        action_ok.setOnClickListener {
            handler.proceed()
            dialog.cancel()
        }
        val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
        action_cancel.setOnClickListener {
            handler.cancel()
            dialog.cancel()
        }
        dialog.setContentView(dialogView)
        dialog.show()
        HelperUnit.setBottomSheetBehavior(dialog, dialogView, BottomSheetBehavior.STATE_EXPANDED)
    }

    override fun onReceivedHttpAuthRequest(view: WebView, handler: HttpAuthHandler, host: String, realm: String) {
        val dialog = BottomSheetDialog(context)
        val dialogView = View.inflate(context, R.layout.dialog_edit_bookmark, null)
        val pass_userNameET = dialogView.findViewById<EditText>(R.id.pass_userName)
        val pass_userPWET = dialogView.findViewById<EditText>(R.id.pass_userPW)
        val login_title: TextInputLayout = dialogView.findViewById(R.id.login_title)
        login_title.visibility = View.GONE
        val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
        action_ok.setOnClickListener {
            val user = pass_userNameET.text.toString().trim { it <= ' ' }
            val pass = pass_userPWET.text.toString().trim { it <= ' ' }
            handler.proceed(user, pass)
            dialog.cancel()
        }
        val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
        action_cancel.setOnClickListener {
            handler.cancel()
            dialog.cancel()
        }
        dialog.setContentView(dialogView)
        dialog.show()
        HelperUnit.setBottomSheetBehavior(dialog, dialogView, BottomSheetBehavior.STATE_EXPANDED)
    }

    init {
        sp = PreferenceManager.getDefaultSharedPreferences(context)
        adBlock = ninjaWebView.adBlock
        cookie = ninjaWebView.cookieHosts
        white = false
        enable = true
    }
}