package openhoangnc.browser.view

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.preference.PreferenceManager
import openhoangnc.browser.Ninja.R
import openhoangnc.browser.browser.*
import openhoangnc.browser.unit.BrowserUnit
import openhoangnc.browser.unit.ViewUnit
import java.util.*

class NinjaWebView : WebView, AlbumController {
    private var onScrollChangeListener: OnScrollChangeListener? = null

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onScrollChanged(l: Int, t: Int, old_l: Int, old_t: Int) {
        super.onScrollChanged(l, t, old_l, old_t)
        if (onScrollChangeListener != null) {
            onScrollChangeListener?.onScrollChange(t, old_t)
        }
    }

// TODO: remove
//    fun setOnScrollChangeListener(onScrollChangeListener: OnScrollChangeListener?) {
//        this.onScrollChangeListener = onScrollChangeListener
//    }

    interface OnScrollChangeListener {
        /**
         * Called when the scroll position of a view changes.
         *
         * @param scrollY    Current vertical scroll origin.
         * @param oldScrollY Previous vertical scroll origin.
         */
        fun onScrollChange(scrollY: Int, oldScrollY: Int)
    }

    //    private var context: Context? = null
    private var dimen144dp = 0
    private var dimen108dp = 0
    private var album: Album? = null
    private var webViewClient: NinjaWebViewClient? = null
    private var webChromeClient: NinjaWebChromeClient? = null
    private var downloadListener: NinjaDownloadListener? = null
    private var clickHandler: NinjaClickHandler? = null
    private var gestureDetector: GestureDetector? = null
    var adBlock: AdBlock? = null
        private set

    var javaHosts: Javascript? = null
        private set
    var cookieHosts: Cookie? = null
        private set

    private var sp: SharedPreferences? = null
    private var webSettings: WebSettings? = null
    var isForeground = false
        private set

    private var browserController: BrowserController? = null
    fun getBrowserController(): BrowserController? {
        return browserController
    }

    fun setBrowserController(browserController: BrowserController?) {
        this.browserController = browserController
        album?.setBrowserController(browserController)
    }

    constructor(context: Context?) : super(context) // Cannot create a dialog, the WebView context is not an activity
    {
//        this.context = context
        dimen144dp = resources.getDimensionPixelSize(R.dimen.layout_width_144dp)
        dimen108dp = resources.getDimensionPixelSize(R.dimen.layout_height_108dp)
        isForeground = false
        adBlock = AdBlock(this.context)
        javaHosts = Javascript(this.context)
        cookieHosts = Cookie(this.context)
        album = Album(this.context, this, browserController)
        webViewClient = NinjaWebViewClient(this)
        webChromeClient = NinjaWebChromeClient(this)
        downloadListener = NinjaDownloadListener(this.context)
        clickHandler = NinjaClickHandler(this)
        gestureDetector = GestureDetector(context, NinjaGestureListener(this))
        initWebView()
        initWebSettings()
        initPreferences()
        initAlbum()
    }

    @Synchronized
    private fun initWebView() {
        setWebViewClient(webViewClient)
        setWebChromeClient(webChromeClient)
        setDownloadListener(downloadListener)
        setOnTouchListener { view, motionEvent ->
            gestureDetector?.onTouchEvent(motionEvent)
            false
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    @Synchronized
    private fun initWebSettings() {
        webSettings = settings
        webSettings?.builtInZoomControls = true
        webSettings?.displayZoomControls = false
        webSettings?.setSupportZoom(true)
        webSettings?.setSupportMultipleWindows(true)
        webSettings?.loadWithOverviewMode = true
        webSettings?.useWideViewPort = true
        webSettings?.safeBrowsingEnabled = true
    }

    @Synchronized
    fun initPreferences() {
        sp = PreferenceManager.getDefaultSharedPreferences(context)
        val userAgent = sp?.getString("userAgent", "")
        webSettings = settings
        if (!userAgent?.isEmpty()!!) {
            webSettings?.userAgentString = userAgent
        }
        webViewClient?.enableAdBlock(
            sp?.getBoolean(
                context?.getString(R.string.sp_ad_block),
                true
            )!!
        )
        webSettings = settings
        webSettings?.textZoom = sp?.getString("sp_fontSize", "100")?.toInt()!!
        webSettings?.allowFileAccessFromFileURLs = sp?.getBoolean("sp_remote", false)!!
        webSettings?.allowUniversalAccessFromFileURLs = sp?.getBoolean("sp_remote", false)!!
        webSettings?.domStorageEnabled = sp?.getBoolean("sp_remote", false)!!
        webSettings?.blockNetworkImage = !sp?.getBoolean(
            context?.getString(R.string.sp_images),
            true
        )!!
        webSettings?.javaScriptEnabled = sp?.getBoolean(
            context?.getString(R.string.sp_javascript),
            true
        )!!
        webSettings?.javaScriptCanOpenWindowsAutomatically = sp?.getBoolean(
            context?.getString(R.string.sp_javascript),
            true
        )!!
        webSettings?.setGeolocationEnabled(
            sp?.getBoolean(
                context?.getString(R.string.sp_location),
                false
            )!!
        )
        val manager = CookieManager.getInstance()
        manager.setAcceptCookie(sp?.getBoolean(context?.getString(R.string.sp_cookies), true)!!)
    }

    @Synchronized
    private fun initAlbum() {
        album?.setAlbumCover(null)
        album?.setAlbumTitle(context?.getString(R.string.app_name))
        album?.setBrowserController(browserController)
    }

    @get:Synchronized
    val requestHeaders: HashMap<String, String>
        get() {
            val requestHeaders = HashMap<String, String>()
            requestHeaders["DNT"] = "1"
            if (sp?.getBoolean(context?.getString(R.string.sp_savedata), false)!!) {
                requestHeaders["Save-Data"] = "on"
            }
            return requestHeaders
        }

    @SuppressLint("SetJavaScriptEnabled")
    @Synchronized
    override fun loadUrl(url: String?) {
        if (url == null || url.trim { it <= ' ' }.isEmpty()) {
            NinjaToast.show(context, R.string.toast_load_error)
            return
        } else {
            if (!sp?.getBoolean(context?.getString(R.string.sp_javascript), true)!!) {
                if (javaHosts?.isWhite(url)!!) {
                    webSettings = settings
                    webSettings?.javaScriptCanOpenWindowsAutomatically = true
                    webSettings?.javaScriptEnabled = true
                } else {
                    webSettings = settings
                    webSettings?.javaScriptCanOpenWindowsAutomatically = false
                    webSettings?.javaScriptEnabled = false
                }
            }
        }
        super.loadUrl(BrowserUnit.queryWrapper(context, url.trim { it <= ' ' }), requestHeaders)
    }

    override val albumView: View?
        get() = album?.albumView

    override fun setAlbumCover(bitmap: Bitmap?) {
        album?.setAlbumCover(bitmap)
    }

    override var albumTitle: String?
        get() = album?.getAlbumTitle()
        set(title) {
            album?.setAlbumTitle(title)
        }

    @Synchronized
    override fun activate() {
        requestFocus()
        isForeground = true
        album?.activate()
    }

    @Synchronized
    override fun deactivate() {
        clearFocus()
        isForeground = false
        album?.deactivate()
    }

    @Synchronized
    fun update(progress: Int) {
        if (isForeground) {
            browserController?.updateProgress(progress)
        }
        if (isLoadFinish) {
            Handler().postDelayed({ setAlbumCover(ViewUnit.capture(this@NinjaWebView, dimen144dp.toFloat(), dimen108dp.toFloat(), Bitmap.Config.RGB_565)) }, 250)
            if (prepareRecord()) {
                browserController?.updateAutoComplete()
            }
        }
    }

    @Synchronized
    fun update(title: String?) {
        album?.setAlbumTitle(title)
    }

    @Synchronized
    override fun destroy() {
        stopLoading()
        onPause()
        clearHistory()
        visibility = View.GONE
        removeAllViews()
        super.destroy()
    }

    val isLoadFinish: Boolean
        get() = progress >= BrowserUnit.PROGRESS_MAX

    fun onLongPress() {
        val click = clickHandler?.obtainMessage()
        click?.target = clickHandler
        requestFocusNodeHref(click)
    }

    private fun prepareRecord(): Boolean {
        val title = title
        val url = url
        return !(title == null || title.isEmpty()
                || url == null || url.isEmpty()
                || url.startsWith(BrowserUnit.URL_SCHEME_ABOUT)
                || url.startsWith(BrowserUnit.URL_SCHEME_MAIL_TO)
                || url.startsWith(BrowserUnit.URL_SCHEME_INTENT))
    }
}