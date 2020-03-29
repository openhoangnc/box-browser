package openhoangnc.browser.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient.CustomViewCallback
import android.webkit.WebView
import android.webkit.WebView.HitTestResult
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mobapphome.mahencryptorlib.MAHEncryptor
import openhoangnc.browser.Ninja.R
import openhoangnc.browser.browser.*
import openhoangnc.browser.database.BookmarkList
import openhoangnc.browser.database.Record
import openhoangnc.browser.database.RecordAction
import openhoangnc.browser.service.ClearService
import openhoangnc.browser.task.ScreenshotTask
import openhoangnc.browser.unit.BrowserUnit
import openhoangnc.browser.unit.HelperUnit
import openhoangnc.browser.unit.IntentUnit
import openhoangnc.browser.unit.ViewUnit
import openhoangnc.browser.view.*
import java.io.File
import java.util.*
import kotlin.math.floor

class BrowserActivity : AppCompatActivity(), BrowserController, View.OnClickListener {
    // Menus
    private var menu_tabPreview: LinearLayout? = null
    private var menu_newTabOpen: LinearLayout? = null
    private var menu_closeTab: LinearLayout? = null
    private var menu_quit: LinearLayout? = null
    private var menu_shareScreenshot: LinearLayout? = null
    private var menu_shareLink: LinearLayout? = null
    private var menu_sharePDF: LinearLayout? = null
    private var menu_openWith: LinearLayout? = null
    private var menu_searchSite: LinearLayout? = null
    private var menu_settings: LinearLayout? = null
    private var menu_download: LinearLayout? = null
    private var menu_saveScreenshot: LinearLayout? = null
    private var menu_saveBookmark: LinearLayout? = null
    private var menu_savePDF: LinearLayout? = null
    private var menu_saveStart: LinearLayout? = null
    private var menu_fileManager: LinearLayout? = null
    private var menu_fav: LinearLayout? = null
    private var menu_sc: LinearLayout? = null
    private var menu_openFav: LinearLayout? = null
    private var menu_shareCP: LinearLayout? = null
    private var floatButton_tabView: View? = null
    private var floatButton_saveView: View? = null
    private var floatButton_shareView: View? = null
    private var floatButton_moreView: View? = null
    private var fab_tab: ImageButton? = null
    private var fab_share: ImageButton? = null
    private var fab_save: ImageButton? = null
    private var fab_more: ImageButton? = null
    private var tab_plus: ImageButton? = null
    private var tab_plus_bottom: ImageButton? = null
    private var adapter: Adapter_Record? = null

    // Views
    private var searchUp: ImageButton? = null
    private var searchDown: ImageButton? = null
    private var searchCancel: ImageButton? = null
    private var omniboxRefresh: ImageButton? = null
    private var omniboxOverflow: ImageButton? = null
    private var omniboxOverview: ImageButton? = null
    private var open_startPage: ImageButton? = null
    private var open_bookmark: ImageButton? = null
    private var open_history: ImageButton? = null
    private var open_menu: ImageButton? = null
    private var fab_imageButtonNav: FloatingActionButton? = null
    private var inputBox: AutoCompleteTextView? = null
    private var progressBar: ProgressBar? = null
    private var searchBox: EditText? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var bottomSheetDialog_OverView: BottomSheetDialog? = null
    private var ninjaWebView: NinjaWebView? = null
    private var listView: ListView? = null
    private var omniboxTitle: TextView? = null
    private var dialogTitle: TextView? = null
    private var gridView: GridView? = null
    private var customView: View? = null
    private var videoView: VideoView? = null
    private var tab_ScrollView: HorizontalScrollView? = null
    private var overview_top: LinearLayout? = null
    private var overview_topButtons: LinearLayout? = null

    // Layouts
    private var appBar: RelativeLayout? = null
    private var omnibox: RelativeLayout? = null
    private var searchPanel: RelativeLayout? = null
    private var contentFrame: FrameLayout? = null
    private var tab_container: LinearLayout? = null
    private var fullscreenHolder: FrameLayout? = null
    private var open_startPageView: View? = null
    private var open_bookmarkView: View? = null
    private var open_historyView: View? = null
    private var overview_titleIcons_startView: View? = null
    private var overview_titleIcons_bookmarksView: View? = null
    private var overview_titleIcons_historyView: View? = null

    // Others
    private var title: String? = null
    private var url: String? = null
    private var overViewTab: String? = null
    private var downloadReceiver: BroadcastReceiver? = null
    private var mBehavior: BottomSheetBehavior<*>? = null
    private var activity: Activity? = null
    private var context: Context? = null
    private var sp: SharedPreferences? = null
    private var mahEncryptor: MAHEncryptor? = null
    private var javaHosts: Javascript? = null
    private var cookieHosts: Cookie? = null
    private var adBlock: AdBlock? = null
    private var gridAdapter: GridAdapter? = null
    private fun prepareRecord(): Boolean {
        val webView = currentAlbumController as NinjaWebView?
        val title = webView?.title
        val url = webView?.url
        return (title == null || title.isEmpty()
                || url == null || url.isEmpty()
                || url.startsWith(BrowserUnit.URL_SCHEME_ABOUT)
                || url.startsWith(BrowserUnit.URL_SCHEME_MAIL_TO)
                || url.startsWith(BrowserUnit.URL_SCHEME_INTENT))
    }

    private var originalOrientation = 0
    private var dimen156dp = 0f
    private var dimen117dp = 0f
    private var searchOnSite = false
    private var onPause = false
    private var customViewCallback: CustomViewCallback? = null
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var currentAlbumController: AlbumController? = null
    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null

    // Classes
    private inner class VideoCompletionListener : OnCompletionListener,
        MediaPlayer.OnErrorListener {
        override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
            return false
        }

        override fun onCompletion(mp: MediaPlayer?) {
            onHideCustomView()
        }
    }

    // Overrides
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        WebView.enableSlowWholeDocumentDraw()
        //StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//StrictMode.setVmPolicy(builder.build());
        context = this@BrowserActivity
        activity = this@BrowserActivity
        sp = PreferenceManager.getDefaultSharedPreferences(context)
        sp?.edit()?.putInt("restart_changed", 0)?.apply()
        sp?.edit()?.putBoolean("pdf_create", false)?.commit()
        HelperUnit.applyTheme(context!!)
        setContentView(R.layout.activity_main)
        if (sp?.getString("saved_key_ok", "no") == "no") {
            val chars =
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!§$%&/()=?;:_-.,+#*<>".toCharArray()
            val sb = StringBuilder()
            val random = Random()
            for (i in 0..24) {
                val c = chars[random.nextInt(chars.size)]
                sb.append(c)
            }
            if (Locale.getDefault().country == "CN") {
                sp?.edit()?.putString(getString(R.string.sp_search_engine), "2")?.apply()
            }
            sp?.edit()?.putString("saved_key", sb.toString())?.apply()
            sp?.edit()?.putString("saved_key_ok", "yes")?.apply()
            sp?.edit()?.putString("setting_gesture_tb_up", "08")?.apply()
            sp?.edit()?.putString("setting_gesture_tb_down", "01")?.apply()
            sp?.edit()?.putString("setting_gesture_tb_left", "07")?.apply()
            sp?.edit()?.putString("setting_gesture_tb_right", "06")?.apply()
            sp?.edit()?.putString("setting_gesture_nav_up", "04")?.apply()
            sp?.edit()?.putString("setting_gesture_nav_down", "05")?.apply()
            sp?.edit()?.putString("setting_gesture_nav_left", "03")?.apply()
            sp?.edit()?.putString("setting_gesture_nav_right", "02")?.apply()
            sp?.edit()?.putBoolean(getString(R.string.sp_location), false)?.apply()
        }
        try {
            mahEncryptor = MAHEncryptor.newInstance(sp?.getString("saved_key", ""))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        contentFrame = findViewById(R.id.main_content)
        appBar = findViewById(R.id.appBar)
        dimen156dp = resources.getDimensionPixelSize(R.dimen.layout_width_156dp).toFloat()
        dimen117dp = resources.getDimensionPixelSize(R.dimen.layout_height_117dp).toFloat()
        initOmnibox()
        initSearchPanel()
        initOverview()
        AdBlock(context) // For AdBlock cold boot
        Javascript(context)
        Cookie(context)
        downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                bottomSheetDialog = BottomSheetDialog(context!!)
                val dialogView = View.inflate(context, R.layout.dialog_action, null)
                val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
                textView.setText(R.string.toast_downloadComplete)
                val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
                action_ok.setOnClickListener {
                    startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
                    hideBottomSheetDialog()
                }
                val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
                action_cancel.setOnClickListener { hideBottomSheetDialog() }
                bottomSheetDialog?.setContentView(dialogView)
                bottomSheetDialog?.show()
                HelperUnit.setBottomSheetBehavior(
                    bottomSheetDialog!!,
                    dialogView,
                    BottomSheetBehavior.STATE_EXPANDED
                )
            }
        }
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        registerReceiver(downloadReceiver, filter)
        dispatchIntent(intent)
        if (sp?.getBoolean("start_tabStart", false)!!) {
            showOverview()
            Handler().postDelayed({ mBehavior?.state = BottomSheetBehavior.STATE_EXPANDED }, 250)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }
        var results: Array<Uri>? = null
        // Check that the response is a good one
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) { // If there is not data, then we may have taken a photo
                val dataString = data.dataString
                if (dataString != null) {
                    results = arrayOf(Uri.parse(dataString))
                }
            }
        }
        mFilePathCallback?.onReceiveValue(results)
        mFilePathCallback = null
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onPause() {
        onPause = true
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        if (sp?.getInt("restart_changed", 1) == 1) {
            sp?.edit()?.putInt("restart_changed", 0)?.apply()
            val dialog = BottomSheetDialog(context!!)
            val dialogView = View.inflate(context, R.layout.dialog_action, null)
            val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
            textView.setText(R.string.toast_restart)
            val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
            action_ok.setOnClickListener {
                dialog.cancel()
                onDestroy()
            }
            val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
            action_cancel.setOnClickListener {
                dialog.cancel()
            }
            dialog.setContentView(dialogView)
            dialog.show()
            HelperUnit.setBottomSheetBehavior(
                dialog,
                dialogView,
                BottomSheetBehavior.STATE_EXPANDED
            )
        }
        dispatchIntent(intent)
        updateOmnibox()
        if (sp?.getBoolean("pdf_create", false)!!) {
            sp?.edit()?.putBoolean("pdf_create", false)?.commit()
            if (sp?.getBoolean("pdf_share", false)!!) {
                sp?.edit()?.putBoolean("pdf_share", false)?.commit()
                startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
            } else {
                bottomSheetDialog = BottomSheetDialog(context!!)
                val dialogView = View.inflate(context, R.layout.dialog_action, null)
                val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
                textView.setText(R.string.toast_downloadComplete)
                val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
                action_ok.setOnClickListener {
                    startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
                    hideBottomSheetDialog()
                }
                val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
                action_cancel.setOnClickListener { hideBottomSheetDialog() }
                bottomSheetDialog?.setContentView(dialogView)
                bottomSheetDialog?.show()
                HelperUnit.setBottomSheetBehavior(
                    bottomSheetDialog!!,
                    dialogView,
                    BottomSheetBehavior.STATE_EXPANDED
                )
            }
        }
    }

    public override fun onDestroy() {
        if (sp?.getBoolean(getString(R.string.sp_clear_quit), false)!!) {
            val toClearService = Intent(this, ClearService::class.java)
            startService(toClearService)
        }
        BrowserContainer.clear()
        unregisterReceiver(downloadReceiver)
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MENU -> return showOverflow()
            KeyEvent.KEYCODE_BACK -> {
                hideKeyboard(activity)
                hideOverview()
                if (fullscreenHolder != null || customView != null || videoView != null) {
                    return onHideCustomView()
                } else if (omnibox?.visibility == View.GONE && sp?.getBoolean(
                        "sp_toolbarShow",
                        true
                    )!!
                ) {
                    showOmnibox()
                } else {
                    if (ninjaWebView?.canGoBack()!!) {
                        ninjaWebView?.goBack()
                    } else {
                        removeAlbum(currentAlbumController)
                    }
                }
                return true
            }
        }
        return false
    }

    @Synchronized
    override fun showAlbum(controller: AlbumController?) {
        if (currentAlbumController != null) {
            currentAlbumController?.deactivate()
            val av = controller as View?
            contentFrame?.removeAllViews()
            contentFrame?.addView(av)
        } else {
            contentFrame?.removeAllViews()
            contentFrame?.addView(controller as View?)
        }
        currentAlbumController = controller
        currentAlbumController?.activate()
        updateOmnibox()
    }

    override fun updateAutoComplete() {
        val action = RecordAction(this)
        action.open(false)
        val list = action.listEntries(activity, true)
        action.close()
        val adapter = CompleteAdapter(this, R.layout.complete_item, list)
        inputBox?.setAdapter(adapter)
        adapter.notifyDataSetChanged()
        inputBox?.threshold = 1
        inputBox?.dropDownVerticalOffset = -16
        inputBox?.dropDownWidth = ViewUnit.getWindowWidth(this)
        inputBox?.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            val url = (view.findViewById<View>(R.id.complete_item_url) as TextView).text.toString()
            updateAlbum(url)
            hideKeyboard(activity)
        }
    }

    private fun showOverview() {
        overview_top?.visibility = View.VISIBLE
        overview_topButtons?.visibility = View.VISIBLE
        mBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        if (currentAlbumController != null) {
            currentAlbumController?.deactivate()
            currentAlbumController?.activate()
        }
        if (currentAlbumController != null) {
            currentAlbumController?.deactivate()
            currentAlbumController?.activate()
        }
        bottomSheetDialog_OverView?.show()
        Handler().postDelayed({
            tab_ScrollView?.smoothScrollTo(
                currentAlbumController?.albumView?.left!!,
                0
            )
        }, 250)
    }

    override fun hideOverview() {
        if (bottomSheetDialog_OverView != null) {
            bottomSheetDialog_OverView?.cancel()
        }
    }

    private fun hideBottomSheetDialog() {
        if (bottomSheetDialog != null) {
            bottomSheetDialog?.cancel()
        }
    }

    override fun onClick(v: View?) {
        val action = RecordAction(context)
        ninjaWebView = currentAlbumController as NinjaWebView?
        try {
            title = ninjaWebView?.title?.trim { it <= ' ' }
            url = ninjaWebView?.url?.trim { it <= ' ' }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        when (v?.id) {
            R.id.tab_plus, R.id.tab_plus_bottom, R.id.menu_newTabOpen -> {
                hideBottomSheetDialog()
                hideOverview()
                addAlbum(
                    getString(R.string.app_name),
                    sp?.getString("favoriteURL", "https://github.com/scoute-dich/browser"),
                    true
                )
            }
            R.id.menu_closeTab -> {
                hideBottomSheetDialog()
                removeAlbum(currentAlbumController)
            }
            R.id.menu_tabPreview -> {
                hideBottomSheetDialog()
                showOverview()
            }
            R.id.menu_quit -> {
                hideBottomSheetDialog()
                doubleTapsQuit()
            }
            R.id.menu_shareScreenshot -> if (Build.VERSION.SDK_INT < 29) {
                val hasWRITE_EXTERNAL_STORAGE =
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                    HelperUnit.grantPermissionsStorage(activity)
                } else {
                    hideBottomSheetDialog()
                    sp?.edit()?.putInt("screenshot", 1)?.apply()
                    ScreenshotTask(context, ninjaWebView).execute()
                }
            } else {
                hideBottomSheetDialog()
                sp?.edit()?.putInt("screenshot", 1)?.apply()
                ScreenshotTask(context, ninjaWebView).execute()
            }
            R.id.menu_shareLink -> {
                hideBottomSheetDialog()
                if (prepareRecord()) {
                    NinjaToast.show(context, getString(R.string.toast_share_failed))
                } else {
                    IntentUnit.share(context, title, url)
                }
            }
            R.id.menu_sharePDF -> {
                hideBottomSheetDialog()
                printPDF(true)
            }
            R.id.menu_openWith -> {
                hideBottomSheetDialog()
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                val chooser = Intent.createChooser(intent, getString(R.string.menu_open_with))
                startActivity(chooser)
            }
            R.id.menu_saveScreenshot -> if (Build.VERSION.SDK_INT < 29) {
                val hasWRITE_EXTERNAL_STORAGE =
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                    HelperUnit.grantPermissionsStorage(activity)
                } else {
                    hideBottomSheetDialog()
                    sp?.edit()?.putInt("screenshot", 0)?.apply()
                    ScreenshotTask(context, ninjaWebView).execute()
                }
            } else {
                hideBottomSheetDialog()
                sp?.edit()?.putInt("screenshot", 0)?.apply()
                ScreenshotTask(context, ninjaWebView).execute()
            }
            R.id.menu_saveBookmark -> {
                hideBottomSheetDialog()
                try {
                    val mahEncryptor = MAHEncryptor.newInstance(sp?.getString("saved_key", ""))
                    val encrypted_userName = mahEncryptor.encode("")
                    val encrypted_userPW = mahEncryptor.encode("")
                    val db = BookmarkList(context)
                    db.open()
                    if (db.isExist(url)) {
                        NinjaToast.show(context, R.string.toast_newTitle)
                    } else {
                        db.insert(
                            HelperUnit.secString(ninjaWebView?.title!!),
                            url,
                            encrypted_userName,
                            encrypted_userPW,
                            "01"
                        )
                        NinjaToast.show(context, R.string.toast_edit_successful)
                        initBookmarkList()
                    }
                    db.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                    NinjaToast.show(context, R.string.toast_error)
                }
            }
            R.id.menu_saveStart -> {
                hideBottomSheetDialog()
                action.open(true)
                if (action.checkGridItem(url)) {
                    NinjaToast.show(context, getString(R.string.toast_already_exist_in_home))
                } else {
                    var counter = sp?.getInt("counter", 0)
                    counter = counter?.plus(1)
                    sp?.edit()?.putInt("counter", counter!!)?.commit()
                    val bitmap = ViewUnit.capture(
                        ninjaWebView,
                        dimen156dp,
                        dimen117dp,
                        Bitmap.Config.ARGB_8888
                    )
                    val filename = counter.toString() + BrowserUnit.SUFFIX_PNG
                    val itemAlbum = GridItem(title, url, filename, counter!!)
                    if (BrowserUnit.bitmap2File(context, bitmap, filename) && action.addGridItem(
                            itemAlbum
                        )
                    ) {
                        NinjaToast.show(context, getString(R.string.toast_add_to_home_successful))
                    } else {
                        NinjaToast.show(context, getString(R.string.toast_add_to_home_failed))
                    }
                }
                action.close()
            }
            R.id.menu_searchSite -> {
                hideBottomSheetDialog()
                hideKeyboard(activity)
                showSearchPanel()
            }
            R.id.contextLink_saveAs -> {
                hideBottomSheetDialog()
                printPDF(false)
            }
            R.id.menu_settings -> {
                hideBottomSheetDialog()
                val settings = Intent(this@BrowserActivity, SettingsActivity::class.java)
                startActivity(settings)
            }
            R.id.menu_fileManager -> {
                hideBottomSheetDialog()
                val intent2 = Intent(Intent.ACTION_VIEW)
                intent2.type = "*/*"
                intent2.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context?.startActivity(intent2, null)
            }
            R.id.menu_download -> {
                hideBottomSheetDialog()
                startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
            }
            R.id.floatButton_tab -> {
                menu_newTabOpen?.visibility = View.VISIBLE
                menu_closeTab?.visibility = View.VISIBLE
                menu_tabPreview?.visibility = View.VISIBLE
                menu_quit?.visibility = View.VISIBLE
                menu_shareScreenshot?.visibility = View.GONE
                menu_shareLink?.visibility = View.GONE
                menu_sharePDF?.visibility = View.GONE
                menu_openWith?.visibility = View.GONE
                menu_saveScreenshot?.visibility = View.GONE
                menu_saveBookmark?.visibility = View.GONE
                menu_savePDF?.visibility = View.GONE
                menu_saveStart?.visibility = View.GONE
                floatButton_tabView?.visibility = View.VISIBLE
                floatButton_saveView?.visibility = View.INVISIBLE
                floatButton_shareView?.visibility = View.INVISIBLE
                floatButton_moreView?.visibility = View.INVISIBLE
                menu_searchSite?.visibility = View.GONE
                menu_fileManager?.visibility = View.GONE
                menu_settings?.visibility = View.GONE
                menu_download?.visibility = View.GONE
                menu_fav?.visibility = View.GONE
                menu_sc?.visibility = View.GONE
                menu_openFav?.visibility = View.VISIBLE
                menu_shareCP?.visibility = View.GONE
            }
            R.id.floatButton_share -> {
                menu_newTabOpen?.visibility = View.GONE
                menu_closeTab?.visibility = View.GONE
                menu_tabPreview?.visibility = View.GONE
                menu_quit?.visibility = View.GONE
                menu_shareScreenshot?.visibility = View.VISIBLE
                menu_shareLink?.visibility = View.VISIBLE
                menu_sharePDF?.visibility = View.VISIBLE
                menu_openWith?.visibility = View.VISIBLE
                menu_saveScreenshot?.visibility = View.GONE
                menu_saveBookmark?.visibility = View.GONE
                menu_savePDF?.visibility = View.GONE
                menu_saveStart?.visibility = View.GONE
                floatButton_tabView?.visibility = View.INVISIBLE
                floatButton_saveView?.visibility = View.INVISIBLE
                floatButton_shareView?.visibility = View.VISIBLE
                floatButton_moreView?.visibility = View.INVISIBLE
                menu_searchSite?.visibility = View.GONE
                menu_fileManager?.visibility = View.GONE
                menu_settings?.visibility = View.GONE
                menu_download?.visibility = View.GONE
                menu_fav?.visibility = View.GONE
                menu_sc?.visibility = View.GONE
                menu_openFav?.visibility = View.GONE
                menu_shareCP?.visibility = View.VISIBLE
            }
            R.id.floatButton_save -> {
                menu_newTabOpen?.visibility = View.GONE
                menu_closeTab?.visibility = View.GONE
                menu_tabPreview?.visibility = View.GONE
                menu_quit?.visibility = View.GONE
                menu_shareScreenshot?.visibility = View.GONE
                menu_shareLink?.visibility = View.GONE
                menu_sharePDF?.visibility = View.GONE
                menu_openWith?.visibility = View.GONE
                menu_saveScreenshot?.visibility = View.VISIBLE
                menu_saveBookmark?.visibility = View.VISIBLE
                menu_savePDF?.visibility = View.VISIBLE
                menu_saveStart?.visibility = View.VISIBLE
                menu_searchSite?.visibility = View.GONE
                menu_fileManager?.visibility = View.GONE
                floatButton_tabView?.visibility = View.INVISIBLE
                floatButton_saveView?.visibility = View.VISIBLE
                floatButton_shareView?.visibility = View.INVISIBLE
                floatButton_moreView?.visibility = View.INVISIBLE
                menu_settings?.visibility = View.GONE
                menu_download?.visibility = View.GONE
                menu_fav?.visibility = View.GONE
                menu_sc?.visibility = View.VISIBLE
                menu_openFav?.visibility = View.GONE
                menu_shareCP?.visibility = View.GONE
            }
            R.id.floatButton_more -> {
                menu_newTabOpen?.visibility = View.GONE
                menu_closeTab?.visibility = View.GONE
                menu_tabPreview?.visibility = View.GONE
                menu_quit?.visibility = View.GONE
                menu_shareScreenshot?.visibility = View.GONE
                menu_shareLink?.visibility = View.GONE
                menu_sharePDF?.visibility = View.GONE
                menu_openWith?.visibility = View.GONE
                menu_saveScreenshot?.visibility = View.GONE
                menu_saveBookmark?.visibility = View.GONE
                menu_savePDF?.visibility = View.GONE
                menu_saveStart?.visibility = View.GONE
                floatButton_tabView?.visibility = View.INVISIBLE
                floatButton_saveView?.visibility = View.INVISIBLE
                floatButton_shareView?.visibility = View.INVISIBLE
                floatButton_moreView?.visibility = View.VISIBLE
                menu_settings?.visibility = View.VISIBLE
                menu_searchSite?.visibility = View.VISIBLE
                menu_fileManager?.visibility = View.VISIBLE
                menu_download?.visibility = View.VISIBLE
                menu_fav?.visibility = View.VISIBLE
                menu_sc?.visibility = View.GONE
                menu_openFav?.visibility = View.GONE
                menu_shareCP?.visibility = View.GONE
            }
            R.id.omnibox_overview -> showOverview()
            R.id.omnibox_refresh -> if (url != null && ninjaWebView?.isLoadFinish!!) {
                if (!url?.startsWith("https://")!!) {
                    bottomSheetDialog = BottomSheetDialog(context!!)
                    val dialogView = View.inflate(context, R.layout.dialog_action, null)
                    val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
                    textView.setText(R.string.toast_unsecured)
                    val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
                    action_ok.setOnClickListener {
                        hideBottomSheetDialog()
                        ninjaWebView?.loadUrl(url?.replace("http://", "https://"))
                    }
                    val action_cancel2 = dialogView.findViewById<Button>(R.id.action_cancel)
                    action_cancel2.setOnClickListener {
                        hideBottomSheetDialog()
                        ninjaWebView?.reload()
                    }
                    bottomSheetDialog?.setContentView(dialogView)
                    bottomSheetDialog?.show()
                    HelperUnit.setBottomSheetBehavior(
                        bottomSheetDialog!!,
                        dialogView,
                        BottomSheetBehavior.STATE_EXPANDED
                    )
                } else {
                    ninjaWebView?.reload()
                }
            } else if (url == null) {
                val text = getString(R.string.toast_load_error) + ": " + url
                NinjaToast.show(context, text)
            } else {
                ninjaWebView?.stopLoading()
            }
            else -> {
            }
        }
    }

    // Methods
    private fun printPDF(share: Boolean) {
        try {
            if (share) {
                sp?.edit()?.putBoolean("pdf_share", true)?.commit()
            } else {
                sp?.edit()?.putBoolean("pdf_share", false)?.commit()
            }
            val title = HelperUnit.fileName(ninjaWebView?.url)
            val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager?
            val printAdapter = ninjaWebView?.createPrintDocumentAdapter(title)
            printManager?.print(title, printAdapter!!, PrintAttributes.Builder().build())
            sp?.edit()?.putBoolean("pdf_create", true)?.commit()
        } catch (e: Exception) {
            NinjaToast.show(context, R.string.toast_error)
            sp?.edit()?.putBoolean("pdf_create", false)?.commit()
            e.printStackTrace()
        }
    }

    private fun dispatchIntent(intent: Intent) {
        val action = intent.action
        val url = intent.getStringExtra(Intent.EXTRA_TEXT)
        if ("" == action) {
            Log.i(ContentValues.TAG, "resumed BOX Browser")
        } else if (intent.action != null && intent.action == Intent.ACTION_WEB_SEARCH) {
            addAlbum(null, intent.getStringExtra(SearchManager.QUERY), true)
        } else if (filePathCallback != null) {
            filePathCallback = null
        } else if ("sc_history" == action) {
            addAlbum(
                null,
                sp?.getString("favoriteURL", "https://github.com/scoute-dich/browser"),
                true
            )
            showOverview()
            Handler().postDelayed({ open_history?.performClick() }, 250)
        } else if ("sc_bookmark" == action) {
            addAlbum(
                null,
                sp?.getString("favoriteURL", "https://github.com/scoute-dich/browser"),
                true
            )
            showOverview()
            Handler().postDelayed({ open_bookmark?.performClick() }, 250)
        } else if ("sc_startPage" == action) {
            addAlbum(
                null,
                sp?.getString("favoriteURL", "https://github.com/scoute-dich/browser"),
                true
            )
            showOverview()
            Handler().postDelayed({ open_startPage?.performClick() }, 250)
        } else if (Intent.ACTION_SEND == action) {
            addAlbum(null, url, true)
        } else {
            if (!onPause) {
                addAlbum(
                    null,
                    sp?.getString("favoriteURL", "https://github.com/scoute-dich/browser"),
                    true
                )
            }
        }
        getIntent().action = ""
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initOmnibox() {
        omnibox = findViewById(R.id.main_omnibox)
        inputBox = findViewById(R.id.main_omnibox_input)
        omniboxRefresh = findViewById(R.id.omnibox_refresh)
        omniboxOverview = findViewById(R.id.omnibox_overview)
        omniboxOverflow = findViewById(R.id.omnibox_overflow)
        omniboxTitle = findViewById(R.id.omnibox_title)
        progressBar = findViewById(R.id.main_progress_bar)
        val nav_position = sp?.getString("nav_position", "0")
        fab_imageButtonNav = when (nav_position) {
            "1" -> findViewById(R.id.fab_imageButtonNav_left)
            "2" -> findViewById(R.id.fab_imageButtonNav_center)
            else -> findViewById(R.id.fab_imageButtonNav_right)
        }
        fab_imageButtonNav?.setOnLongClickListener {
            show_dialogFastToggle()
            false
        }
        omniboxOverflow?.setOnLongClickListener {
            show_dialogFastToggle()
            false
        }
        fab_imageButtonNav?.setOnClickListener { showOverflow() }
        omniboxOverflow?.setOnClickListener { showOverflow() }
        if (sp?.getBoolean("sp_gestures_use", true)!!) {
            fab_imageButtonNav?.setOnTouchListener(object : SwipeTouchListener(context) {
                override fun onSwipeTop() {
                    performGesture("setting_gesture_nav_up")
                }

                override fun onSwipeBottom() {
                    performGesture("setting_gesture_nav_down")
                }

                override fun onSwipeRight() {
                    performGesture("setting_gesture_nav_right")
                }

                override fun onSwipeLeft() {
                    performGesture("setting_gesture_nav_left")
                }
            })
            omniboxOverflow?.setOnTouchListener(object : SwipeTouchListener(context) {
                override fun onSwipeTop() {
                    performGesture("setting_gesture_nav_up")
                }

                override fun onSwipeBottom() {
                    performGesture("setting_gesture_nav_down")
                }

                override fun onSwipeRight() {
                    performGesture("setting_gesture_nav_right")
                }

                override fun onSwipeLeft() {
                    performGesture("setting_gesture_nav_left")
                }
            })
            inputBox?.setOnTouchListener(object : SwipeTouchListener(context) {
                override fun onSwipeTop() {
                    performGesture("setting_gesture_tb_up")
                }

                override fun onSwipeBottom() {
                    performGesture("setting_gesture_tb_down")
                }

                override fun onSwipeRight() {
                    performGesture("setting_gesture_tb_right")
                }

                override fun onSwipeLeft() {
                    performGesture("setting_gesture_tb_left")
                }
            })
        }
        inputBox?.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            val query = inputBox?.text.toString().trim { it <= ' ' }
            if (query.isEmpty()) {
                NinjaToast.show(context, getString(R.string.toast_input_empty))
                return@OnEditorActionListener true
            }
            updateAlbum(query)
            showOmnibox()
            false
        })
        inputBox?.setOnFocusChangeListener { v, hasFocus ->
            if (inputBox?.hasFocus()!!) {
                ninjaWebView?.stopLoading()
                inputBox?.setText(ninjaWebView?.url)
                Handler().postDelayed({
                    omniboxTitle?.visibility = View.GONE
                    inputBox?.requestFocus()
                    inputBox?.setSelection(0, inputBox?.text.toString().length)
                }, 250)
            } else {
                omniboxTitle?.visibility = View.VISIBLE
                omniboxTitle?.text = ninjaWebView?.title
                hideKeyboard(activity)
            }
        }
        updateAutoComplete()
        omniboxRefresh?.setOnClickListener(this)
        omniboxOverview?.setOnClickListener(this)
    }

    private fun performGesture(gesture: String) {
        val gestureAction = sp?.getString(gesture, "0")
        val controller: AlbumController?
        ninjaWebView = currentAlbumController as NinjaWebView?
        when (gestureAction) {
            "01" -> {
            }
            "02" -> if (ninjaWebView?.canGoForward()!!) {
                ninjaWebView?.goForward()
            } else {
                NinjaToast.show(context, R.string.toast_webview_forward)
            }
            "03" -> if (ninjaWebView?.canGoBack()!!) {
                ninjaWebView?.goBack()
            } else {
                removeAlbum(currentAlbumController)
            }
            "04" -> ninjaWebView?.pageUp(true)
            "05" -> ninjaWebView?.pageDown(true)
            "06" -> {
                controller = nextAlbumController(false)
                showAlbum(controller)
            }
            "07" -> {
                controller = nextAlbumController(true)
                showAlbum(controller)
            }
            "08" -> showOverview()
            "09" -> addAlbum(
                getString(R.string.app_name),
                sp?.getString("favoriteURL", "https://github.com/scoute-dich/browser"),
                true
            )
            "10" -> removeAlbum(currentAlbumController)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initOverview() {
        bottomSheetDialog_OverView = BottomSheetDialog(context!!)
        val dialogView = View.inflate(context, R.layout.dialog_overiew, null)
        open_startPage = dialogView.findViewById(R.id.open_newTab_2)
        open_bookmark = dialogView.findViewById(R.id.open_bookmark_2)
        open_history = dialogView.findViewById(R.id.open_history_2)
        open_menu = dialogView.findViewById(R.id.open_menu)
        tab_container = dialogView.findViewById(R.id.tab_container)
        tab_plus = dialogView.findViewById(R.id.tab_plus)
        tab_plus?.setOnClickListener(this)
        tab_plus_bottom = dialogView.findViewById(R.id.tab_plus_bottom)
        tab_plus_bottom?.setOnClickListener(this)
        tab_ScrollView = dialogView.findViewById(R.id.tab_ScrollView)
        overview_top = dialogView.findViewById(R.id.overview_top)
        overview_topButtons = dialogView.findViewById(R.id.overview_topButtons)
        listView = dialogView.findViewById(R.id.home_list_2)
        open_startPageView = dialogView.findViewById(R.id.open_newTabView)
        open_bookmarkView = dialogView.findViewById(R.id.open_bookmarkView)
        open_historyView = dialogView.findViewById(R.id.open_historyView)
        overview_titleIcons_startView = dialogView.findViewById(R.id.overview_titleIcons_startView)
        overview_titleIcons_bookmarksView =
            dialogView.findViewById(R.id.overview_titleIcons_bookmarksView)
        overview_titleIcons_historyView =
            dialogView.findViewById(R.id.overview_titleIcons_historyView)
        gridView = dialogView.findViewById(R.id.home_grid_2)
        val overview_titleIcons_start =
            dialogView.findViewById<ImageButton>(R.id.overview_titleIcons_start)
        val overview_titleIcons_bookmarks =
            dialogView.findViewById<ImageButton>(R.id.overview_titleIcons_bookmarks)
        val overview_titleIcons_history =
            dialogView.findViewById<ImageButton>(R.id.overview_titleIcons_history)
        // allow scrolling in listView without closing the bottomSheetDialog
        listView?.setOnTouchListener { v, event ->
            val action = event.action
            if (action == MotionEvent.ACTION_DOWN) { // Disallow NestedScrollView to intercept touch events.
                if (listView?.canScrollVertically(-1)!!) {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                }
            }
            // Handle ListView touch events.
            v.onTouchEvent(event)
            true
        }
        gridView?.setOnTouchListener { v, event ->
            val action = event.action
            if (action == MotionEvent.ACTION_DOWN) { // Disallow NestedScrollView to intercept touch events.
                if (gridView?.canScrollVertically(-1)!!) {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                }
            }
            // Handle ListView touch events.
            v.onTouchEvent(event)
            true
        }
        open_menu?.setOnClickListener {
            bottomSheetDialog = BottomSheetDialog(context!!)
            val dialogView = View.inflate(context, R.layout.dialog_menu_overview, null)
            val bookmark_sort = dialogView.findViewById<LinearLayout>(R.id.bookmark_sort)
            val bookmark_filter = dialogView.findViewById<LinearLayout>(R.id.bookmark_filter)
            if (overViewTab == getString(R.string.album_title_bookmarks)) {
                bookmark_filter.visibility = View.VISIBLE
                bookmark_sort.visibility = View.VISIBLE
            } else if (overViewTab == getString(R.string.album_title_home)) {
                bookmark_filter.visibility = View.GONE
                bookmark_sort.visibility = View.VISIBLE
            } else if (overViewTab == getString(R.string.album_title_history)) {
                bookmark_filter.visibility = View.GONE
                bookmark_sort.visibility = View.GONE
            }
            bookmark_filter.setOnClickListener { show_dialogFilter() }
            bookmark_sort.setOnClickListener {
                hideBottomSheetDialog()
                bottomSheetDialog = BottomSheetDialog(context!!)
                val dialogView = View.inflate(context, R.layout.dialog_bookmark_sort, null)
                val dialog_sortName = dialogView.findViewById<LinearLayout>(R.id.dialog_sortName)
                val bookmark_sort_tv = dialogView.findViewById<TextView>(R.id.bookmark_sort_tv)
                if (overViewTab == getString(R.string.album_title_bookmarks)) {
                    bookmark_sort_tv.text = resources.getString(R.string.dialog_sortIcon)
                } else if (overViewTab == getString(R.string.album_title_home)) {
                    bookmark_sort_tv.text = resources.getString(R.string.dialog_sortDate)
                }
                dialog_sortName.setOnClickListener {
                    if (overViewTab == getString(R.string.album_title_bookmarks)) {
                        sp?.edit()?.putString("sortDBB", "title")?.apply()
                        initBookmarkList()
                        hideBottomSheetDialog()
                    } else if (overViewTab == getString(R.string.album_title_home)) {
                        sp?.edit()?.putString("sort_startSite", "title")?.apply()
                        open_startPage?.performClick()
                        hideBottomSheetDialog()
                    }
                }
                val dialog_sortIcon = dialogView.findViewById<LinearLayout>(R.id.dialog_sortIcon)
                dialog_sortIcon.setOnClickListener {
                    if (overViewTab == getString(R.string.album_title_bookmarks)) {
                        sp?.edit()?.putString("sortDBB", "icon")?.apply()
                        initBookmarkList()
                        hideBottomSheetDialog()
                    } else if (overViewTab == getString(R.string.album_title_home)) {
                        sp?.edit()?.putString("sort_startSite", "ordinal")?.apply()
                        open_startPage?.performClick()
                        hideBottomSheetDialog()
                    }
                }
                bottomSheetDialog?.setContentView(dialogView)
                bottomSheetDialog?.show()
                HelperUnit.setBottomSheetBehavior(
                    bottomSheetDialog!!,
                    dialogView,
                    BottomSheetBehavior.STATE_EXPANDED
                )
            }
            val tv_delete = dialogView.findViewById<LinearLayout>(R.id.tv_delete)
            tv_delete.setOnClickListener {
                hideBottomSheetDialog()
                bottomSheetDialog = BottomSheetDialog(context!!)
                val dialogView3 = View.inflate(context, R.layout.dialog_action, null)
                val textView = dialogView3.findViewById<TextView>(R.id.dialog_text)
                textView.setText(R.string.hint_database)
                val action_ok = dialogView3.findViewById<Button>(R.id.action_ok)
                action_ok.setOnClickListener {
                    if (overViewTab == getString(R.string.album_title_home)) {
                        BrowserUnit.clearHome(context)
                        open_startPage?.performClick()
                    } else if (overViewTab == getString(R.string.album_title_bookmarks)) {
                        val data = Environment.getDataDirectory()
                        val bookmarksPath_app = "//data//$packageName//databases//pass_DB_v01.db"
                        val bookmarkFile_app = File(data, bookmarksPath_app)
                        BrowserUnit.deleteDir(bookmarkFile_app)
                        open_bookmark?.performClick()
                    } else if (overViewTab == getString(R.string.album_title_history)) {
                        BrowserUnit.clearHistory(context)
                        open_history?.performClick()
                    }
                    hideBottomSheetDialog()
                }
                val action_cancel = dialogView3.findViewById<Button>(R.id.action_cancel)
                action_cancel.setOnClickListener { hideBottomSheetDialog() }
                bottomSheetDialog?.setContentView(dialogView3)
                bottomSheetDialog?.show()
                HelperUnit.setBottomSheetBehavior(
                    bottomSheetDialog!!,
                    dialogView3,
                    BottomSheetBehavior.STATE_EXPANDED
                )
            }
            bottomSheetDialog?.setContentView(dialogView)
            bottomSheetDialog?.show()
            HelperUnit.setBottomSheetBehavior(
                bottomSheetDialog!!,
                dialogView,
                BottomSheetBehavior.STATE_EXPANDED
            )
        }
        bottomSheetDialog_OverView?.setContentView(dialogView)
        mBehavior = BottomSheetBehavior.from(dialogView.parent as View)
        val peekHeight = Math.round(200 * resources.displayMetrics.density)
        mBehavior?.peekHeight = peekHeight
        mBehavior?.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    hideOverview()
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    if (overViewTab == getString(R.string.album_title_bookmarks)) {
                        initBookmarkList()
                    } else if (overViewTab == getString(R.string.album_title_home)) {
                        open_startPage?.performClick()
                    }
                    if (sp?.getBoolean("overView_hide", true)!!) {
                        overview_top?.visibility = View.GONE
                    } else {
                        overview_topButtons?.visibility = View.GONE
                    }
                } else {
                    if (sp?.getBoolean("overView_hide", true)!!) {
                        overview_top?.visibility = View.VISIBLE
                    } else {
                        overview_topButtons?.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
        open_startPage?.setOnClickListener {
            mBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            gridView?.visibility = View.VISIBLE
            listView?.visibility = View.GONE
            open_startPageView?.visibility = View.VISIBLE
            open_bookmarkView?.visibility = View.INVISIBLE
            open_historyView?.visibility = View.INVISIBLE
            overview_titleIcons_startView?.visibility = View.VISIBLE
            overview_titleIcons_bookmarksView?.visibility = View.INVISIBLE
            overview_titleIcons_historyView?.visibility = View.INVISIBLE
            overViewTab = getString(R.string.album_title_home)
            val action = RecordAction(context)
            action.open(false)
            val gridList = action.listGrid(context)
            action.close()
            gridAdapter = GridAdapter(context, gridList)
            gridView?.adapter = gridAdapter
            gridAdapter?.notifyDataSetChanged()
            gridView?.setOnItemClickListener { _, _, position, id ->
                updateAlbum(gridList[position].url)
                hideOverview()
            }
            gridView?.setOnItemLongClickListener { parent, view, position, id ->
                showContextMenuList(
                    gridList[position].title, gridList[position].url,
                    null, null, 0,
                    null, null, null, null, gridList[position]
                )
                true
            }
        }
        open_bookmark?.setOnClickListener {
            mBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            gridView?.visibility = View.GONE
            listView?.visibility = View.VISIBLE
            open_startPageView?.visibility = View.INVISIBLE
            open_bookmarkView?.visibility = View.VISIBLE
            open_historyView?.visibility = View.INVISIBLE
            overview_titleIcons_startView?.visibility = View.INVISIBLE
            overview_titleIcons_bookmarksView?.visibility = View.VISIBLE
            overview_titleIcons_historyView?.visibility = View.INVISIBLE
            overViewTab = getString(R.string.album_title_bookmarks)
            sp?.edit()?.putString("filter_bookmarks", "00")?.apply()
            initBookmarkList()
        }
        open_bookmark?.setOnLongClickListener {
            show_dialogFilter()
            false
        }
        open_history?.setOnClickListener {
            mBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            gridView?.visibility = View.GONE
            listView?.visibility = View.VISIBLE
            open_startPageView?.visibility = View.INVISIBLE
            open_bookmarkView?.visibility = View.INVISIBLE
            open_historyView?.visibility = View.VISIBLE
            overview_titleIcons_startView?.visibility = View.INVISIBLE
            overview_titleIcons_bookmarksView?.visibility = View.INVISIBLE
            overview_titleIcons_historyView?.visibility = View.VISIBLE
            overViewTab = getString(R.string.album_title_history)
            val action = RecordAction(context)
            action.open(false)
            val list: MutableList<Record>?
            list = action.listEntries(activity, false)
            action.close()
            adapter = Adapter_Record(context, list)
            listView?.adapter = adapter
            adapter?.notifyDataSetChanged()
            listView?.setOnItemClickListener { parent, view, position, id ->
                updateAlbum(list[position].url)
                hideOverview()
            }
            listView?.setOnItemLongClickListener { parent, view, position, id ->
                showContextMenuList(
                    list[position].title, list[position].url, adapter, list, position,
                    null,
                    null,
                    null,
                    null,
                    null
                )
                true
            }
        }
        overview_titleIcons_start.setOnClickListener { open_startPage?.performClick() }
        overview_titleIcons_bookmarks.setOnClickListener { open_bookmark?.performClick() }
        overview_titleIcons_bookmarks.setOnLongClickListener {
            if (overViewTab != getString(R.string.album_title_bookmarks)) {
                open_bookmark?.performClick()
            }
            show_dialogFilter()
            false
        }
        overview_titleIcons_history.setOnClickListener { open_history?.performClick() }
        when (Objects.requireNonNull(sp?.getString("start_tab", "0"))) {
            "3" -> {
                overview_top?.visibility = View.GONE
                open_bookmark?.performClick()
            }
            "4" -> {
                overview_top?.visibility = View.GONE
                open_history?.performClick()
            }
            else -> {
                overview_top?.visibility = View.GONE
                open_startPage?.performClick()
            }
        }
    }

    private fun initSearchPanel() {
        searchPanel = findViewById(R.id.main_search_panel)
        searchBox = findViewById(R.id.main_search_box)
        searchUp = findViewById(R.id.main_search_up)
        searchDown = findViewById(R.id.main_search_down)
        searchCancel = findViewById(R.id.main_search_cancel)
        searchBox?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (currentAlbumController != null) {
                    (currentAlbumController as NinjaWebView).findAllAsync(s.toString())
                }
            }
        })
        searchBox?.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId != EditorInfo.IME_ACTION_DONE) {
                return@OnEditorActionListener false
            }
            if (searchBox?.text.toString().isEmpty()) {
                NinjaToast.show(context, getString(R.string.toast_input_empty))
                return@OnEditorActionListener true
            }
            false
        })
        searchUp?.setOnClickListener(View.OnClickListener {
            val query = searchBox?.text.toString()
            if (query.isEmpty()) {
                NinjaToast.show(context, getString(R.string.toast_input_empty))
                return@OnClickListener
            }
            hideKeyboard(activity)
            (currentAlbumController as NinjaWebView?)?.findNext(false)
        })
        searchDown?.setOnClickListener(View.OnClickListener {
            val query = searchBox?.text.toString()
            if (query.isEmpty()) {
                NinjaToast.show(context, getString(R.string.toast_input_empty))
                return@OnClickListener
            }
            hideKeyboard(activity)
            (currentAlbumController as NinjaWebView?)?.findNext(true)
        })
        searchCancel?.setOnClickListener { hideSearchPanel() }
    }

    private fun initBookmarkList() {
        val db = BookmarkList(context)
        val row: Cursor?
        db.open()
        val layoutStyle = R.layout.list_item_bookmark
        val xml_id = intArrayOf(
            R.id.record_item_title
        )
        val column = arrayOf(
            "pass_title"
        )
        val search = sp?.getString("filter_bookmarks", "00")
        row = if (search!! == "00") {
            db.fetchAllData(activity)
        } else {
            db.fetchDataByFilter(search, "pass_creation")
        }
        val adapter: SimpleCursorAdapter =
            object : SimpleCursorAdapter(context, layoutStyle, row, column, xml_id, 0) {
                override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
                    val row = listView?.getItemAtPosition(position) as Cursor
                    val bookmarks_icon = row.getString(row.getColumnIndexOrThrow("pass_creation"))
                    val v = super.getView(position, convertView, parent)
                    val iv_icon = v.findViewById<ImageView>(R.id.ib_icon)
                    HelperUnit.switchIcon(activity, bookmarks_icon, "pass_creation", iv_icon)
                    row.close()
                    return v
                }
            }
        listView?.adapter = adapter
        listView?.onItemClickListener = OnItemClickListener { adapterView, view, position, id ->
            val pass_content = row?.getString(row.getColumnIndexOrThrow("pass_content"))
            val pass_icon = row?.getString(row.getColumnIndexOrThrow("pass_icon"))
            val pass_attachment = row?.getString(row.getColumnIndexOrThrow("pass_attachment"))
            updateAlbum(pass_content)
            toast_login(pass_icon!!, pass_attachment!!)
            hideOverview()
        }
        listView?.onItemLongClickListener = OnItemLongClickListener { parent, view, position, id ->
            val row = listView?.getItemAtPosition(position) as Cursor
            val _id = row.getString(row.getColumnIndexOrThrow("_id"))
            val pass_title = row.getString(row.getColumnIndexOrThrow("pass_title"))
            val pass_content = row.getString(row.getColumnIndexOrThrow("pass_content"))
            val pass_icon = row.getString(row.getColumnIndexOrThrow("pass_icon"))
            val pass_attachment = row.getString(row.getColumnIndexOrThrow("pass_attachment"))
            val pass_creation = row.getString(row.getColumnIndexOrThrow("pass_creation"))
            showContextMenuList(
                pass_title, pass_content, null, null, 0,
                pass_icon, pass_attachment, _id, pass_creation, null
            )
            row.close()
            true
        }
        row?.close()
        db.close()
    }

    private fun show_dialogFastToggle() {
        bottomSheetDialog = BottomSheetDialog(context!!)
        val dialogView = View.inflate(context, R.layout.dialog_toggle, null)
        val sw_java = dialogView.findViewById<CheckBox>(R.id.switch_js)
        val whiteList_js = dialogView.findViewById<ImageButton>(R.id.imageButton_js)
        val sw_adBlock = dialogView.findViewById<CheckBox>(R.id.switch_adBlock)
        val whiteList_ab = dialogView.findViewById<ImageButton>(R.id.imageButton_ab)
        val sw_cookie = dialogView.findViewById<CheckBox>(R.id.switch_cookie)
        val whitelist_cookie = dialogView.findViewById<ImageButton>(R.id.imageButton_cookie)
        val dialog_title = dialogView.findViewById<TextView>(R.id.dialog_title)
        dialog_title.text = HelperUnit.domain(ninjaWebView?.url)
        javaHosts = Javascript(context)
        cookieHosts = Cookie(context)
        adBlock = AdBlock(context)
        ninjaWebView = currentAlbumController as NinjaWebView?
        val url = ninjaWebView?.url
        sw_java.isChecked = sp?.getBoolean(getString(R.string.sp_javascript), true)!!
        sw_adBlock.isChecked = sp?.getBoolean(getString(R.string.sp_ad_block), true)!!
        sw_cookie.isChecked = sp?.getBoolean(getString(R.string.sp_cookies), true)!!
        if (javaHosts?.isWhite(url)!!) {
            whiteList_js.setImageResource(R.drawable.check_green)
        } else {
            whiteList_js.setImageResource(R.drawable.ic_action_close_red)
        }
        if (cookieHosts?.isWhite(url)!!) {
            whitelist_cookie.setImageResource(R.drawable.check_green)
        } else {
            whitelist_cookie.setImageResource(R.drawable.ic_action_close_red)
        }
        if (adBlock?.isWhite(url)!!) {
            whiteList_ab.setImageResource(R.drawable.check_green)
        } else {
            whiteList_ab.setImageResource(R.drawable.ic_action_close_red)
        }
        whiteList_js.setOnClickListener {
            if (javaHosts?.isWhite(ninjaWebView?.url)!!) {
                whiteList_js.setImageResource(R.drawable.ic_action_close_red)
                javaHosts?.removeDomain(HelperUnit.domain(url))
            } else {
                whiteList_js.setImageResource(R.drawable.check_green)
                javaHosts?.addDomain(HelperUnit.domain(url))
            }
        }
        whitelist_cookie.setOnClickListener {
            if (cookieHosts?.isWhite(ninjaWebView?.url)!!) {
                whitelist_cookie.setImageResource(R.drawable.ic_action_close_red)
                cookieHosts?.removeDomain(HelperUnit.domain(url))
            } else {
                whitelist_cookie.setImageResource(R.drawable.check_green)
                cookieHosts?.addDomain(HelperUnit.domain(url))
            }
        }
        whiteList_ab.setOnClickListener {
            if (adBlock?.isWhite(ninjaWebView?.url)!!) {
                whiteList_ab.setImageResource(R.drawable.ic_action_close_red)
                adBlock?.removeDomain(HelperUnit.domain(url))
            } else {
                whiteList_ab.setImageResource(R.drawable.check_green)
                adBlock?.addDomain(HelperUnit.domain(url))
            }
        }
        sw_java.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sp?.edit()?.putBoolean(getString(R.string.sp_javascript), true)?.commit()
            } else {
                sp?.edit()?.putBoolean(getString(R.string.sp_javascript), false)?.commit()
            }
        }
        sw_adBlock.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                sp?.edit()?.putBoolean(getString(R.string.sp_ad_block), true)?.commit()
            } else {
                sp?.edit()?.putBoolean(getString(R.string.sp_ad_block), false)?.commit()
            }
        }
        sw_cookie.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                sp?.edit()?.putBoolean(getString(R.string.sp_cookies), true)?.commit()
            } else {
                sp?.edit()?.putBoolean(getString(R.string.sp_cookies), false)?.commit()
            }
        }
        val toggleHistory = dialogView.findViewById<ImageButton>(R.id.toggle_history)
        val toggleHistoryView = dialogView.findViewById<View>(R.id.toggle_historyView)
        val toggleLocation = dialogView.findViewById<ImageButton>(R.id.toggle_location)
        val toggleLocationView = dialogView.findViewById<View>(R.id.toggle_locationView)
        val toggleImages = dialogView.findViewById<ImageButton>(R.id.toggle_images)
        val toggleImagesView = dialogView.findViewById<View>(R.id.toggle_imagesView)
        val toggleRemote = dialogView.findViewById<ImageButton>(R.id.toggle_remote)
        val toggleRemoteView = dialogView.findViewById<View>(R.id.toggle_remoteView)
        val toggleInvert = dialogView.findViewById<ImageButton>(R.id.toggle_invert)
        val toggleInvertView = dialogView.findViewById<View>(R.id.toggle_invertView)
        val toggleFont = dialogView.findViewById<ImageButton>(R.id.toggle_font)
        toggleFont.setOnClickListener {
            bottomSheetDialog?.cancel()
            val intent = Intent(context, SettingsActivity::class.java)
            startActivity(intent)
        }
        if (sp?.getBoolean("saveHistory", false)!!) {
            toggleHistoryView.visibility = View.VISIBLE
        } else {
            toggleHistoryView.visibility = View.INVISIBLE
        }
        toggleHistory.setOnClickListener {
            if (sp?.getBoolean("saveHistory", false)!!) {
                toggleHistoryView.visibility = View.INVISIBLE
                sp?.edit()?.putBoolean("saveHistory", false)?.commit()
            } else {
                toggleHistoryView.visibility = View.VISIBLE
                sp?.edit()?.putBoolean("saveHistory", true)?.commit()
            }
        }
        if (sp?.getBoolean(getString(R.string.sp_location), false)!!) {
            toggleLocationView.visibility = View.VISIBLE
        } else {
            toggleLocationView.visibility = View.INVISIBLE
        }
        toggleLocation.setOnClickListener {
            if (sp?.getBoolean(getString(R.string.sp_location), false)!!) {
                toggleLocationView.visibility = View.INVISIBLE
                sp?.edit()?.putBoolean(getString(R.string.sp_location), false)?.commit()
            } else {
                toggleLocationView.visibility = View.VISIBLE
                sp?.edit()?.putBoolean(getString(R.string.sp_location), true)?.commit()
            }
        }
        if (sp?.getBoolean(getString(R.string.sp_images), true)!!) {
            toggleImagesView.visibility = View.VISIBLE
        } else {
            toggleImagesView.visibility = View.INVISIBLE
        }
        toggleImages.setOnClickListener {
            if (sp?.getBoolean(getString(R.string.sp_images), true)!!) {
                toggleImagesView.visibility = View.INVISIBLE
                sp?.edit()?.putBoolean(getString(R.string.sp_images), false)?.commit()
            } else {
                toggleImagesView.visibility = View.VISIBLE
                sp?.edit()?.putBoolean(getString(R.string.sp_images), true)?.commit()
            }
        }
        if (sp?.getBoolean("sp_remote", true)!!) {
            toggleRemoteView.visibility = View.VISIBLE
        } else {
            toggleRemoteView.visibility = View.INVISIBLE
        }
        toggleRemote.setOnClickListener {
            if (sp?.getBoolean("sp_remote", true)!!) {
                toggleRemoteView.visibility = View.INVISIBLE
                sp?.edit()?.putBoolean("sp_remote", false)?.commit()
            } else {
                toggleRemoteView.visibility = View.VISIBLE
                sp?.edit()?.putBoolean("sp_remote", true)?.commit()
            }
        }
        if (sp?.getBoolean("sp_invert", false)!!) {
            toggleInvertView.visibility = View.VISIBLE
        } else {
            toggleInvertView.visibility = View.INVISIBLE
        }
        toggleInvert.setOnClickListener {
            if (sp?.getBoolean("sp_invert", false)!!) {
                toggleInvertView.visibility = View.INVISIBLE
                sp?.edit()?.putBoolean("sp_invert", false)?.commit()
            } else {
                toggleInvertView.visibility = View.VISIBLE
                sp?.edit()?.putBoolean("sp_invert", true)?.commit()
            }
            HelperUnit.initRendering(contentFrame)
        }
        val but_OK = dialogView.findViewById<Button>(R.id.action_ok)
        but_OK.setOnClickListener {
            if (ninjaWebView != null) {
                hideBottomSheetDialog()
                ninjaWebView?.initPreferences()
                ninjaWebView?.reload()
            }
        }
        val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
        action_cancel.setOnClickListener { hideBottomSheetDialog() }
        bottomSheetDialog?.setContentView(dialogView)
        bottomSheetDialog?.show()
        HelperUnit.setBottomSheetBehavior(
            bottomSheetDialog!!,
            dialogView,
            BottomSheetBehavior.STATE_EXPANDED
        )
    }

    private fun toast_login(userName: String, passWord: String) {
        try {
            val decrypted_userName = mahEncryptor?.decode(userName)
            val decrypted_userPW = mahEncryptor?.decode(passWord)
            val clipboard = (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            val unCopy: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val clip = ClipData.newPlainText("text", decrypted_userName)
                    clipboard.setPrimaryClip(clip)
                    NinjaToast.show(context, R.string.toast_copy_successful)
                }
            }
            val pwCopy: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val clip = ClipData.newPlainText("text", decrypted_userPW)
                    clipboard.setPrimaryClip(clip)
                    NinjaToast.show(context, R.string.toast_copy_successful)
                }
            }
            val intentFilter = IntentFilter("unCopy")
            registerReceiver(unCopy, intentFilter)
            val copy = Intent("unCopy")
            val copyUN =
                PendingIntent.getBroadcast(context, 0, copy, PendingIntent.FLAG_CANCEL_CURRENT)
            val intentFilter2 = IntentFilter("pwCopy")
            registerReceiver(pwCopy, intentFilter2)
            val copy2 = Intent("pwCopy")
            val copyPW =
                PendingIntent.getBroadcast(context, 1, copy2, PendingIntent.FLAG_CANCEL_CURRENT)

            val mNotificationManager =
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            val builder: NotificationCompat.Builder = {
                val CHANNEL_ID = "browser_not" // The id of the channel.
                val name: CharSequence =
                    getString(R.string.app_name) // The user-visible name of the channel.
                val mChannel =
                    NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH)
                mNotificationManager.createNotificationChannel(mChannel)
                NotificationCompat.Builder(context!!, CHANNEL_ID)
            }()

            val action_UN = NotificationCompat.Action.Builder(
                R.drawable.icon_earth,
                getString(R.string.toast_titleConfirm_pasteUN),
                copyUN
            ).build()
            val action_PW = NotificationCompat.Action.Builder(
                R.drawable.icon_earth,
                getString(R.string.toast_titleConfirm_pastePW),
                copyPW
            ).build()
            val n = builder
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setSmallIcon(R.drawable.ic_notification_ninja)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.toast_titleConfirm_paste))
                .setColor(resources.getColor(R.color.colorAccent))
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setVibrate(LongArray(0))
                .addAction(action_UN)
                .addAction(action_PW)
                .build()
            val notificationManager =
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            if (decrypted_userName?.length!! > 0 || decrypted_userPW?.length!! > 0) {
                notificationManager.notify(0, n)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            NinjaToast.show(context, R.string.toast_error)
        }
    }

    @Synchronized
    private fun addAlbum(title: String?, url: String?, foreground: Boolean) {
        ninjaWebView = NinjaWebView(context)
        ninjaWebView?.setBrowserController(this)
        ninjaWebView?.albumTitle = title
        ViewUnit.bound(context, ninjaWebView!!)
        val albumView = ninjaWebView?.albumView
        if (currentAlbumController != null) {
            val index = BrowserContainer.indexOf(currentAlbumController) + 1
            BrowserContainer.add(ninjaWebView!!, index)
            tab_container?.addView(
                albumView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        } else {
            BrowserContainer.add(ninjaWebView!!)
            tab_container?.addView(
                albumView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        if (!foreground) {
            ViewUnit.bound(context, ninjaWebView!!)
            ninjaWebView?.loadUrl(url!!)
            ninjaWebView?.deactivate()
            return
        } else {
            showOmnibox()
            showAlbum(ninjaWebView)
        }
        if (url != null && !url.isEmpty()) {
            ninjaWebView?.loadUrl(url)
        }
    }

    @Synchronized
    private fun updateAlbum(url: String?) {
        (currentAlbumController as NinjaWebView?)?.loadUrl(url!!)
        updateOmnibox()
    }

    private fun closeTabConfirmation(okAction: Runnable) {
        if (!sp?.getBoolean("sp_close_tab_confirm", false)!!) {
            okAction.run()
        } else {
            bottomSheetDialog = BottomSheetDialog(context!!)
            val dialogView = View.inflate(context, R.layout.dialog_action, null)
            val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
            textView.setText(R.string.toast_close_tab)
            val actionOk = dialogView.findViewById<Button>(R.id.action_ok)
            actionOk.setOnClickListener {
                okAction.run()
                hideBottomSheetDialog()
            }
            val actionCancel = dialogView.findViewById<Button>(R.id.action_cancel)
            actionCancel.setOnClickListener { hideBottomSheetDialog() }
            bottomSheetDialog?.setContentView(dialogView)
            bottomSheetDialog?.show()
            HelperUnit.setBottomSheetBehavior(
                bottomSheetDialog!!,
                dialogView,
                BottomSheetBehavior.STATE_EXPANDED
            )
        }
    }

    @Synchronized
    override fun removeAlbum(controller: AlbumController?) {
        if (BrowserContainer.size() <= 1) {
            if (!sp?.getBoolean("sp_reopenLastTab", false)!!) {
                doubleTapsQuit()
            } else {
                updateAlbum(sp?.getString("favoriteURL", "https://github.com/scoute-dich/browser"))
                hideOverview()
            }
        } else {
            closeTabConfirmation(Runnable {
                tab_container?.removeView(controller?.albumView)
                var index = BrowserContainer.indexOf(controller)
                BrowserContainer.remove(controller)
                if (index >= BrowserContainer.size()) {
                    index = BrowserContainer.size() - 1
                }
                showAlbum(BrowserContainer.get(index))
            })
        }
    }

    private fun updateOmnibox() {
        if (ninjaWebView === currentAlbumController) {
            omniboxTitle?.text = ninjaWebView?.title
        } else {
            ninjaWebView = currentAlbumController as NinjaWebView?
            updateProgress(ninjaWebView?.progress!!)
        }
    }

    private fun scrollChange() {
        if (sp?.getBoolean("hideToolbar", true)!!) {
            ninjaWebView?.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                val height =
                    floor(ninjaWebView?.contentHeight!! * ninjaWebView?.resources?.displayMetrics?.density?.toDouble()!!).toInt()
                val webViewHeight = ninjaWebView?.height
                val cutoff =
                    height - webViewHeight!! - 112 * Math.round(resources.displayMetrics.density)
                if (scrollY in oldScrollY + 1..cutoff) {
                    hideOmnibox()
                } else if (scrollY < oldScrollY) {
                    showOmnibox()
                }
            }
        }
    }

    @Synchronized
    override fun updateProgress(progress: Int) {
        progressBar?.progress = progress
        updateOmnibox()
        updateAutoComplete()
        scrollChange()
        HelperUnit.initRendering(contentFrame)
        ninjaWebView?.requestFocus()
        if (progress < BrowserUnit.PROGRESS_MAX) {
            updateRefresh(true)
            progressBar?.visibility = View.VISIBLE
        } else {
            updateRefresh(false)
            progressBar?.visibility = View.GONE
        }
    }

    private fun updateRefresh(running: Boolean) {
        if (running) {
            omniboxRefresh?.setImageResource(R.drawable.icon_close)
        } else {
            try {
                if (ninjaWebView?.url?.contains("https://")!!) {
                    omniboxRefresh?.setImageResource(R.drawable.icon_refresh)
                } else {
                    omniboxRefresh?.setImageResource(R.drawable.icon_alert)
                }
            } catch (e: Exception) {
                omniboxRefresh?.setImageResource(R.drawable.icon_refresh)
            }
        }
    }

    override fun showFileChooser(filePathCallback: ValueCallback<Array<Uri>>?) {
        if (mFilePathCallback != null) {
            mFilePathCallback?.onReceiveValue(null)
        }
        mFilePathCallback = filePathCallback
        val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
        contentSelectionIntent.type = "*/*"
        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
        startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE)
    }

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        if (view == null) {
            return
        }
        if (customView != null && callback != null) {
            callback.onCustomViewHidden()
            return
        }
        customView = view
        originalOrientation = requestedOrientation
        fullscreenHolder = FrameLayout(context!!)
        fullscreenHolder?.addView(
            customView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        val decorView = window.decorView as FrameLayout
        decorView.addView(
            fullscreenHolder,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        customView?.keepScreenOn = true
        (currentAlbumController as View?)?.visibility = View.GONE
        setCustomFullscreen(true)
        if (view is FrameLayout) {
            if (view.focusedChild is VideoView) {
                videoView = view.focusedChild as VideoView
                videoView?.setOnErrorListener(VideoCompletionListener())
                videoView?.setOnCompletionListener(VideoCompletionListener())
            }
        }
        customViewCallback = callback
        // TODO: check requestedOrientation
        // requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun onHideCustomView(): Boolean {
        if (customView == null || customViewCallback == null || currentAlbumController == null) {
            return false
        }
        val decorView = window.decorView as FrameLayout
        decorView.removeView(fullscreenHolder)
        customView?.keepScreenOn = false
        (currentAlbumController as View).visibility = View.VISIBLE
        setCustomFullscreen(false)
        fullscreenHolder = null
        customView = null
        if (videoView != null) {
            videoView?.setOnErrorListener(null)
            videoView?.setOnCompletionListener(null)
            videoView = null
        }
        requestedOrientation = originalOrientation
        return true
    }

    private fun show_contextMenu_link(url: String?) {
        bottomSheetDialog = BottomSheetDialog(context!!)
        val dialogView = View.inflate(context, R.layout.dialog_menu_context_link, null)
        dialogTitle = dialogView.findViewById(R.id.dialog_title)
        dialogTitle?.text = url
        val contextLinkNewTab = dialogView.findViewById<LinearLayout>(R.id.contextLink_newTab)
        contextLinkNewTab.setOnClickListener {
            addAlbum(getString(R.string.app_name), url, false)
            NinjaToast.show(context, getString(R.string.toast_new_tab_successful))
            hideBottomSheetDialog()
        }
        val contextLinkShareLink =
            dialogView.findViewById<LinearLayout>(R.id.contextLink__shareLink)
        contextLinkShareLink.setOnClickListener {
            if (prepareRecord()) {
                NinjaToast.show(context, getString(R.string.toast_share_failed))
            } else {
                IntentUnit.share(context, "", url)
            }
            hideBottomSheetDialog()
        }
        val contextLinkOpenWith = dialogView.findViewById<LinearLayout>(R.id.contextLink_openWith)
        contextLinkOpenWith.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            val chooser = Intent.createChooser(intent, getString(R.string.menu_open_with))
            startActivity(chooser)
            hideBottomSheetDialog()
        }
        val contextLinkNewTabOpen =
            dialogView.findViewById<LinearLayout>(R.id.contextLink_newTabOpen)
        contextLinkNewTabOpen.setOnClickListener {
            addAlbum(getString(R.string.app_name), url, true)
            hideBottomSheetDialog()
        }
        val contextLinkSaveAs = dialogView.findViewById<LinearLayout>(R.id.contextLink_saveAs)
        contextLinkSaveAs.setOnClickListener {
            try {
                hideBottomSheetDialog()
                val builder = AlertDialog.Builder(context!!)
                val dialogView = View.inflate(context, R.layout.dialog_edit_extension, null)
                val editTitle = dialogView.findViewById<EditText>(R.id.dialog_edit)
                val editExtension = dialogView.findViewById<EditText>(R.id.dialog_edit_extension)
                val filename = URLUtil.guessFileName(url, null, null)
                editTitle.setHint(R.string.dialog_title_hint)
                editTitle.setText(HelperUnit.fileName(ninjaWebView?.url))
                val extension = filename.substring(filename.lastIndexOf("."))
                if (extension.length <= 8) {
                    editExtension.setText(extension)
                }
                builder.setView(dialogView)
                builder.setTitle(R.string.menu_edit)
                builder.setPositiveButton(R.string.app_ok) { dialog, whichButton ->
                    val title = editTitle.text.toString().trim { it <= ' ' }
                    val extension = editExtension.text.toString().trim { it <= ' ' }
                    val filename = title + extension
                    if (title.isEmpty() || extension.isEmpty() || !extension.startsWith(".")) {
                        NinjaToast.show(context, getString(R.string.toast_input_empty))
                    } else {
                        if (Build.VERSION.SDK_INT < 29) {
                            val hasWriteExternalStorage =
                                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            if (hasWriteExternalStorage != PackageManager.PERMISSION_GRANTED) {
                                HelperUnit.grantPermissionsStorage(activity)
                            } else {
                                val source = Uri.parse(url)
                                val request = DownloadManager.Request(source)
                                request.addRequestHeader(
                                    "Cookie",
                                    CookieManager.getInstance().getCookie(url)
                                )
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) //Notify client once download is completed!
                                request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    filename
                                )
                                val dm =
                                    (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
                                dm.enqueue(request)
                                hideKeyboard(activity)
                            }
                        } else {
                            val source = Uri.parse(url)
                            val request = DownloadManager.Request(source)
                            request.addRequestHeader(
                                "Cookie",
                                CookieManager.getInstance().getCookie(url)
                            )
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) //Notify client once download is completed!
                            request.setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS,
                                filename
                            )
                            val dm = (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
                            dm.enqueue(request)
                            hideKeyboard(activity)
                        }
                    }
                }
                builder.setNegativeButton(R.string.app_cancel) { dialog, whichButton ->
                    dialog.cancel()
                    hideKeyboard(activity)
                }
                val dialog = builder.create()
                dialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        bottomSheetDialog?.setContentView(dialogView)
        bottomSheetDialog?.show()
        HelperUnit.setBottomSheetBehavior(
            bottomSheetDialog!!,
            dialogView,
            BottomSheetBehavior.STATE_EXPANDED
        )
    }

    override fun onLongPress(url: String?) {
        val result = ninjaWebView?.hitTestResult
        if (url != null) {
            show_contextMenu_link(url)
        } else if (result != null) {
            if (result.type == HitTestResult.IMAGE_TYPE || result.type == HitTestResult.SRC_IMAGE_ANCHOR_TYPE || result.type == HitTestResult.SRC_ANCHOR_TYPE) {
                show_contextMenu_link(result.extra)
            }
        }
    }

    private fun doubleTapsQuit() {
        if (!sp?.getBoolean("sp_close_browser_confirm", true)!!) {
            finish()
        } else {
            bottomSheetDialog = BottomSheetDialog(context!!)
            val dialogView = View.inflate(context, R.layout.dialog_action, null)
            val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
            textView.setText(R.string.toast_quit)
            val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
            action_ok.setOnClickListener {
                hideBottomSheetDialog()
                finish()
            }
            val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
            action_cancel.setOnClickListener { hideBottomSheetDialog() }
            bottomSheetDialog?.setContentView(dialogView)
            bottomSheetDialog?.show()
            HelperUnit.setBottomSheetBehavior(
                bottomSheetDialog!!,
                dialogView,
                BottomSheetBehavior.STATE_EXPANDED
            )
        }
    }

    @SuppressLint("RestrictedApi")
    private fun showOmnibox() {
        if (!searchOnSite) {
            fab_imageButtonNav?.visibility = View.GONE
            searchPanel?.visibility = View.GONE
            omnibox?.visibility = View.VISIBLE
            omniboxTitle?.visibility = View.VISIBLE
            appBar?.visibility = View.VISIBLE
            hideKeyboard(activity)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun hideOmnibox() {
        if (!searchOnSite) {
            fab_imageButtonNav?.visibility = View.VISIBLE
            searchPanel?.visibility = View.GONE
            omnibox?.visibility = View.GONE
            omniboxTitle?.visibility = View.GONE
            appBar?.visibility = View.GONE
        }
    }

    private fun hideSearchPanel() {
        searchOnSite = false
        searchBox?.setText("")
        showOmnibox()
    }

    @SuppressLint("RestrictedApi")
    private fun showSearchPanel() {
        searchOnSite = true
        fab_imageButtonNav?.visibility = View.GONE
        omnibox?.visibility = View.GONE
        searchPanel?.visibility = View.VISIBLE
        omniboxTitle?.visibility = View.GONE
        appBar?.visibility = View.VISIBLE
    }

    private fun showOverflow(): Boolean {
        bottomSheetDialog = BottomSheetDialog(context!!)
        val dialogView = View.inflate(context, R.layout.dialog_menu, null)
        fab_tab = dialogView.findViewById(R.id.floatButton_tab)
        fab_tab?.setOnClickListener(this@BrowserActivity)
        fab_share = dialogView.findViewById(R.id.floatButton_share)
        fab_share?.setOnClickListener(this@BrowserActivity)
        fab_save = dialogView.findViewById(R.id.floatButton_save)
        fab_save?.setOnClickListener(this@BrowserActivity)
        fab_more = dialogView.findViewById(R.id.floatButton_more)
        fab_more?.setOnClickListener(this@BrowserActivity)
        floatButton_tabView = dialogView.findViewById(R.id.floatButton_tabView)
        floatButton_saveView = dialogView.findViewById(R.id.floatButton_saveView)
        floatButton_shareView = dialogView.findViewById(R.id.floatButton_shareView)
        floatButton_moreView = dialogView.findViewById(R.id.floatButton_moreView)
        dialogTitle = dialogView.findViewById(R.id.dialog_title)
        dialogTitle?.text = ninjaWebView?.title
        menu_newTabOpen = dialogView.findViewById(R.id.menu_newTabOpen)
        menu_newTabOpen?.setOnClickListener(this@BrowserActivity)
        menu_closeTab = dialogView.findViewById(R.id.menu_closeTab)
        menu_closeTab?.setOnClickListener(this@BrowserActivity)
        menu_tabPreview = dialogView.findViewById(R.id.menu_tabPreview)
        menu_tabPreview?.setOnClickListener(this@BrowserActivity)
        menu_quit = dialogView.findViewById(R.id.menu_quit)
        menu_quit?.setOnClickListener(this@BrowserActivity)
        menu_shareScreenshot = dialogView.findViewById(R.id.menu_shareScreenshot)
        menu_shareScreenshot?.setOnClickListener(this@BrowserActivity)
        menu_shareLink = dialogView.findViewById(R.id.menu_shareLink)
        menu_shareLink?.setOnClickListener(this@BrowserActivity)
        menu_sharePDF = dialogView.findViewById(R.id.menu_sharePDF)
        menu_sharePDF?.setOnClickListener(this@BrowserActivity)
        menu_openWith = dialogView.findViewById(R.id.menu_openWith)
        menu_openWith?.setOnClickListener(this@BrowserActivity)
        menu_saveScreenshot = dialogView.findViewById(R.id.menu_saveScreenshot)
        menu_saveScreenshot?.setOnClickListener(this@BrowserActivity)
        menu_saveBookmark = dialogView.findViewById(R.id.menu_saveBookmark)
        menu_saveBookmark?.setOnClickListener(this@BrowserActivity)
        menu_savePDF = dialogView.findViewById(R.id.contextLink_saveAs)
        menu_savePDF?.setOnClickListener(this@BrowserActivity)
        menu_saveStart = dialogView.findViewById(R.id.menu_saveStart)
        menu_saveStart?.setOnClickListener(this@BrowserActivity)
        menu_searchSite = dialogView.findViewById(R.id.menu_searchSite)
        menu_searchSite?.setOnClickListener(this@BrowserActivity)
        menu_settings = dialogView.findViewById(R.id.menu_settings)
        menu_settings?.setOnClickListener(this@BrowserActivity)
        menu_download = dialogView.findViewById(R.id.menu_download)
        menu_download?.setOnClickListener(this@BrowserActivity)
        menu_fileManager = dialogView.findViewById(R.id.menu_fileManager)
        menu_fileManager?.setOnClickListener(this@BrowserActivity)
        menu_shareCP = dialogView.findViewById(R.id.menu_shareClipboard)
        menu_shareCP?.setOnClickListener {
            hideBottomSheetDialog()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clip = ClipData.newPlainText("text", url)
            clipboard?.setPrimaryClip(clip)
            NinjaToast.show(context, R.string.toast_copy_successful)
        }
        menu_openFav = dialogView.findViewById(R.id.menu_openFav)
        menu_openFav?.setOnClickListener {
            hideBottomSheetDialog()
            updateAlbum(sp?.getString("favoriteURL", "https://github.com/scoute-dich/browser"))
        }
        menu_sc = dialogView.findViewById(R.id.menu_sc)
        menu_sc?.setOnClickListener {
            hideBottomSheetDialog()
            HelperUnit.createShortcut(context, ninjaWebView?.title, ninjaWebView?.url)
        }
        menu_fav = dialogView.findViewById(R.id.menu_fav)
        menu_fav?.setOnClickListener {
            hideBottomSheetDialog()
            HelperUnit.setFavorite(context, url)
        }
        bottomSheetDialog?.setContentView(dialogView)
        bottomSheetDialog?.show()
        HelperUnit.setBottomSheetBehavior(
            bottomSheetDialog!!,
            dialogView,
            BottomSheetBehavior.STATE_EXPANDED
        )
        return true
    }

    private fun showContextMenuList(
        title: String?, url: String?,
        adapterRecord: Adapter_Record?, recordList: MutableList<Record>?, location: Int,
        userName: String?, userPW: String?, _id: String?, pass_creation: String?,
        gridItem: GridItem?
    ) {
        bottomSheetDialog = BottomSheetDialog(context!!)
        val dialogView = View.inflate(context, R.layout.dialog_menu_context_list, null)
        val db = BookmarkList(context)
        db.open()
        val contextListEdit = dialogView.findViewById<LinearLayout>(R.id.menu_contextList_edit)
        val contextListFav = dialogView.findViewById<LinearLayout>(R.id.menu_contextList_fav)
        val contextListSc = dialogView.findViewById<LinearLayout>(R.id.menu_contextLink_sc)
        val contextListNewTab = dialogView.findViewById<LinearLayout>(R.id.menu_contextList_newTab)
        val contextListNewTabOpen =
            dialogView.findViewById<LinearLayout>(R.id.menu_contextList_newTabOpen)
        val contextListDelete = dialogView.findViewById<LinearLayout>(R.id.menu_contextList_delete)
        if (overViewTab == getString(R.string.album_title_history)) {
            contextListEdit.visibility = View.GONE
        } else {
            contextListEdit.visibility = View.VISIBLE
        }
        contextListFav.setOnClickListener {
            hideBottomSheetDialog()
            HelperUnit.setFavorite(context, url)
        }
        contextListSc.setOnClickListener {
            hideBottomSheetDialog()
            HelperUnit.createShortcut(context, title, url)
        }
        contextListNewTab.setOnClickListener {
            addAlbum(getString(R.string.app_name), url, false)
            NinjaToast.show(context, getString(R.string.toast_new_tab_successful))
            hideBottomSheetDialog()
        }
        contextListNewTabOpen.setOnClickListener {
            addAlbum(getString(R.string.app_name), url, true)
            hideBottomSheetDialog()
            hideOverview()
        }
        contextListDelete.setOnClickListener {
            hideBottomSheetDialog()
            bottomSheetDialog = BottomSheetDialog(context!!)
            val dialogView = View.inflate(context, R.layout.dialog_action, null)
            val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
            textView.setText(R.string.toast_titleConfirm_delete)
            val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
            action_ok.setOnClickListener {
                if (overViewTab == getString(R.string.album_title_home)) {
                    val action = RecordAction(context)
                    action.open(true)
                    action.deleteGridItem(gridItem)
                    action.close()
                    deleteFile(gridItem?.filename)
                    open_startPage?.performClick()
                    hideBottomSheetDialog()
                } else if (overViewTab == getString(R.string.album_title_bookmarks)) {
                    db.delete(_id?.toInt()!!)
                    initBookmarkList()
                    hideBottomSheetDialog()
                } else if (overViewTab == getString(R.string.album_title_history)) {
                    val record = recordList!![location]
                    val action = RecordAction(context)
                    action.open(true)
                    action.deleteHistoryItem(record)
                    action.close()
                    recordList.removeAt(location)
                    adapterRecord?.notifyDataSetChanged()
                    updateAutoComplete()
                    hideBottomSheetDialog()
                }
            }
            val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
            action_cancel.setOnClickListener { hideBottomSheetDialog() }
            bottomSheetDialog?.setContentView(dialogView)
            bottomSheetDialog?.show()
            HelperUnit.setBottomSheetBehavior(
                bottomSheetDialog!!,
                dialogView,
                BottomSheetBehavior.STATE_EXPANDED
            )
        }
        contextListEdit.setOnClickListener {
            hideBottomSheetDialog()
            if (overViewTab == getString(R.string.album_title_home)) {
                bottomSheetDialog = BottomSheetDialog(context!!)
                val dialogView = View.inflate(context, R.layout.dialog_edit_title, null)
                val editText = dialogView.findViewById<EditText>(R.id.dialog_edit)
                editText.setHint(R.string.dialog_title_hint)
                editText.setText(title)
                val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
                action_ok.setOnClickListener {
                    val text = editText.text.toString().trim { it <= ' ' }
                    if (text.isEmpty()) {
                        NinjaToast.show(context, getString(R.string.toast_input_empty))
                    } else {
                        val action = RecordAction(context)
                        action.open(true)
                        gridItem?.title = text
                        action.updateGridItem(gridItem)
                        action.close()
                        hideKeyboard(activity)
                        open_startPage?.performClick()
                    }
                    hideBottomSheetDialog()
                }
                val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
                action_cancel.setOnClickListener {
                    hideKeyboard(activity)
                    hideBottomSheetDialog()
                }
                bottomSheetDialog?.setContentView(dialogView)
                bottomSheetDialog?.show()
                HelperUnit.setBottomSheetBehavior(
                    bottomSheetDialog!!,
                    dialogView,
                    BottomSheetBehavior.STATE_EXPANDED
                )
            } else if (overViewTab == getString(R.string.album_title_bookmarks)) {
                try {
                    bottomSheetDialog = BottomSheetDialog(context!!)
                    val dialogView = View.inflate(context, R.layout.dialog_edit_bookmark, null)
                    val pass_titleET = dialogView.findViewById<EditText>(R.id.pass_title)
                    val pass_userNameET = dialogView.findViewById<EditText>(R.id.pass_userName)
                    val pass_userPWET = dialogView.findViewById<EditText>(R.id.pass_userPW)
                    val pass_URLET = dialogView.findViewById<EditText>(R.id.pass_url)
                    val ib_icon = dialogView.findViewById<ImageView>(R.id.ib_icon)
                    val decrypted_userName = mahEncryptor?.decode(userName)
                    val decrypted_userPW = mahEncryptor?.decode(userPW)
                    pass_titleET.setText(title)
                    pass_userNameET.setText(decrypted_userName)
                    pass_userPWET.setText(decrypted_userPW)
                    pass_URLET.setText(url)
                    val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
                    action_ok.setOnClickListener {
                        try {
                            val input_pass_title = pass_titleET.text.toString().trim { it <= ' ' }
                            val input_pass_url = pass_URLET.text.toString().trim { it <= ' ' }
                            val encrypted_userName = mahEncryptor?.encode(
                                pass_userNameET.text.toString().trim { it <= ' ' })
                            val encrypted_userPW = mahEncryptor?.encode(
                                pass_userPWET.text.toString().trim { it <= ' ' })
                            db.update(
                                _id?.toInt()!!,
                                HelperUnit.secString(input_pass_title),
                                HelperUnit.secString(input_pass_url),
                                HelperUnit.secString(
                                    encrypted_userName!!
                                ),
                                HelperUnit.secString(encrypted_userPW!!),
                                pass_creation
                            )
                            initBookmarkList()
                            hideKeyboard(activity)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            NinjaToast.show(context, R.string.toast_error)
                        }
                        hideBottomSheetDialog()
                    }
                    val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
                    action_cancel.setOnClickListener {
                        hideKeyboard(activity)
                        hideBottomSheetDialog()
                    }
                    HelperUnit.switchIcon(activity, pass_creation, "pass_creation", ib_icon)
                    bottomSheetDialog?.setContentView(dialogView)
                    bottomSheetDialog?.show()
                    HelperUnit.setBottomSheetBehavior(
                        bottomSheetDialog!!,
                        dialogView,
                        BottomSheetBehavior.STATE_EXPANDED
                    )
                    ib_icon.setOnClickListener {
                        try {
                            val input_pass_title = pass_titleET.text.toString().trim { it <= ' ' }
                            val input_pass_url = pass_URLET.text.toString().trim { it <= ' ' }
                            val encrypted_userName = mahEncryptor?.encode(
                                pass_userNameET.text.toString().trim { it <= ' ' })
                            val encrypted_userPW = mahEncryptor?.encode(
                                pass_userPWET.text.toString().trim { it <= ' ' })
                            hideBottomSheetDialog()
                            hideKeyboard(activity)
                            bottomSheetDialog = BottomSheetDialog(context!!)
                            val dialogView = View.inflate(context, R.layout.dialog_edit_icon, null)
                            val grid = dialogView.findViewById<GridView>(R.id.grid_filter)
                            val itemAlbum_01 = GridItem_filter(
                                sp?.getString(
                                    "icon_01",
                                    resources.getString(R.string.color_red)
                                ), resources.getDrawable(R.drawable.circle_red_big), "01"
                            )
                            val itemAlbum_02 = GridItem_filter(
                                sp?.getString(
                                    "icon_02",
                                    resources.getString(R.string.color_pink)
                                ),
                                resources.getDrawable(R.drawable.circle_pink_big),
                                "02"
                            )
                            val itemAlbum_03 = GridItem_filter(
                                sp?.getString(
                                    "icon_03",
                                    resources.getString(R.string.color_purple)
                                ),
                                resources.getDrawable(R.drawable.circle_purple_big),
                                "03"
                            )
                            val itemAlbum_04 = GridItem_filter(
                                sp?.getString(
                                    "icon_04",
                                    resources.getString(R.string.color_blue)
                                ),
                                resources.getDrawable(R.drawable.circle_blue_big),
                                "04"
                            )
                            val itemAlbum_05 = GridItem_filter(
                                sp?.getString(
                                    "icon_05",
                                    resources.getString(R.string.color_teal)
                                ),
                                resources.getDrawable(R.drawable.circle_teal_big),
                                "05"
                            )
                            val itemAlbum_06 = GridItem_filter(
                                sp?.getString(
                                    "icon_06",
                                    resources.getString(R.string.color_green)
                                ),
                                resources.getDrawable(R.drawable.circle_green_big),
                                "06"
                            )
                            val itemAlbum_07 = GridItem_filter(
                                sp?.getString(
                                    "icon_07",
                                    resources.getString(R.string.color_lime)
                                ),
                                resources.getDrawable(R.drawable.circle_lime_big),
                                "07"
                            )
                            val itemAlbum_08 = GridItem_filter(
                                sp?.getString(
                                    "icon_08",
                                    resources.getString(R.string.color_yellow)
                                ),
                                resources.getDrawable(R.drawable.circle_yellow_big),
                                "08"
                            )
                            val itemAlbum_09 = GridItem_filter(
                                sp?.getString(
                                    "icon_09",
                                    resources.getString(R.string.color_orange)
                                ),
                                resources.getDrawable(R.drawable.circle_orange_big),
                                "09"
                            )
                            val itemAlbum_10 = GridItem_filter(
                                sp?.getString(
                                    "icon_10",
                                    resources.getString(R.string.color_brown)
                                ),
                                resources.getDrawable(R.drawable.circle_brown_big),
                                "10"
                            )
                            val itemAlbum_11 = GridItem_filter(
                                sp?.getString(
                                    "icon_11",
                                    resources.getString(R.string.color_grey)
                                ),
                                resources.getDrawable(R.drawable.circle_grey_big),
                                "11"
                            )
                            val gridList: MutableList<GridItem_filter> = LinkedList()
                            if (sp?.getBoolean("filter_01", true)!!) {
                                gridList.add(gridList.size, itemAlbum_01)
                            }
                            if (sp?.getBoolean("filter_02", true)!!) {
                                gridList.add(gridList.size, itemAlbum_02)
                            }
                            if (sp?.getBoolean("filter_03", true)!!) {
                                gridList.add(gridList.size, itemAlbum_03)
                            }
                            if (sp?.getBoolean("filter_04", true)!!) {
                                gridList.add(gridList.size, itemAlbum_04)
                            }
                            if (sp?.getBoolean("filter_05", true)!!) {
                                gridList.add(gridList.size, itemAlbum_05)
                            }
                            if (sp?.getBoolean("filter_06", true)!!) {
                                gridList.add(gridList.size, itemAlbum_06)
                            }
                            if (sp?.getBoolean("filter_07", true)!!) {
                                gridList.add(gridList.size, itemAlbum_07)
                            }
                            if (sp?.getBoolean("filter_08", true)!!) {
                                gridList.add(gridList.size, itemAlbum_08)
                            }
                            if (sp?.getBoolean("filter_09", true)!!) {
                                gridList.add(gridList.size, itemAlbum_09)
                            }
                            if (sp?.getBoolean("filter_10", true)!!) {
                                gridList.add(gridList.size, itemAlbum_10)
                            }
                            if (sp?.getBoolean("filter_11", true)!!) {
                                gridList.add(gridList.size, itemAlbum_11)
                            }
                            val gridAdapter = GridAdapter_filter(context, gridList)
                            grid.adapter = gridAdapter
                            gridAdapter.notifyDataSetChanged()
                            grid.onItemClickListener =
                                OnItemClickListener { parent, view, position, id ->
                                    db.update(
                                        _id?.toInt()!!,
                                        HelperUnit.secString(input_pass_title),
                                        HelperUnit.secString(input_pass_url),
                                        HelperUnit.secString(
                                            encrypted_userName!!
                                        ),
                                        HelperUnit.secString(encrypted_userPW!!),
                                        gridList[position].ordinal
                                    )
                                    initBookmarkList()
                                    hideBottomSheetDialog()
                                }
                            bottomSheetDialog?.setContentView(dialogView)
                            bottomSheetDialog?.show()
                            HelperUnit.setBottomSheetBehavior(
                                bottomSheetDialog!!,
                                dialogView,
                                BottomSheetBehavior.STATE_EXPANDED
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            hideBottomSheetDialog()
                            NinjaToast.show(context, R.string.toast_error)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    NinjaToast.show(context, R.string.toast_error)
                }
            }
        }
        bottomSheetDialog?.setContentView(dialogView)
        bottomSheetDialog?.show()
        HelperUnit.setBottomSheetBehavior(
            bottomSheetDialog!!,
            dialogView,
            BottomSheetBehavior.STATE_EXPANDED
        )

        db.close()
    }

    private fun show_dialogFilter() {
        hideBottomSheetDialog()
        open_bookmark?.performClick()
        bottomSheetDialog = BottomSheetDialog(context!!)
        val dialogView = View.inflate(context, R.layout.dialog_edit_icon, null)
        val grid = dialogView.findViewById<GridView>(R.id.grid_filter)
        val itemAlbum_01 = GridItem_filter(
            sp?.getString("icon_01", resources.getString(R.string.color_red)),
            resources.getDrawable(R.drawable.circle_red_big),
            "01"
        )
        val itemAlbum_02 = GridItem_filter(
            sp?.getString("icon_02", resources.getString(R.string.color_pink)),
            resources.getDrawable(R.drawable.circle_pink_big),
            "02"
        )
        val itemAlbum_03 = GridItem_filter(
            sp?.getString("icon_03", resources.getString(R.string.color_purple)),
            resources.getDrawable(R.drawable.circle_purple_big),
            "03"
        )
        val itemAlbum_04 = GridItem_filter(
            sp?.getString("icon_04", resources.getString(R.string.color_blue)),
            resources.getDrawable(R.drawable.circle_blue_big),
            "04"
        )
        val itemAlbum_05 = GridItem_filter(
            sp?.getString("icon_05", resources.getString(R.string.color_teal)),
            resources.getDrawable(R.drawable.circle_teal_big),
            "05"
        )
        val itemAlbum_06 = GridItem_filter(
            sp?.getString("icon_06", resources.getString(R.string.color_green)),
            resources.getDrawable(R.drawable.circle_green_big),
            "06"
        )
        val itemAlbum_07 = GridItem_filter(
            sp?.getString("icon_07", resources.getString(R.string.color_lime)),
            resources.getDrawable(R.drawable.circle_lime_big),
            "07"
        )
        val itemAlbum_08 = GridItem_filter(
            sp?.getString("icon_08", resources.getString(R.string.color_yellow)),
            resources.getDrawable(R.drawable.circle_yellow_big),
            "08"
        )
        val itemAlbum_09 = GridItem_filter(
            sp?.getString("icon_09", resources.getString(R.string.color_orange)),
            resources.getDrawable(R.drawable.circle_orange_big),
            "09"
        )
        val itemAlbum_10 = GridItem_filter(
            sp?.getString("icon_10", resources.getString(R.string.color_brown)),
            resources.getDrawable(R.drawable.circle_brown_big),
            "10"
        )
        val itemAlbum_11 = GridItem_filter(
            sp?.getString("icon_11", resources.getString(R.string.color_grey)),
            resources.getDrawable(R.drawable.circle_grey_big),
            "11"
        )
        val gridList: MutableList<GridItem_filter> = LinkedList()
        if (sp?.getBoolean("filter_01", true)!!) {
            gridList.add(gridList.size, itemAlbum_01)
        }
        if (sp?.getBoolean("filter_02", true)!!) {
            gridList.add(gridList.size, itemAlbum_02)
        }
        if (sp?.getBoolean("filter_03", true)!!) {
            gridList.add(gridList.size, itemAlbum_03)
        }
        if (sp?.getBoolean("filter_04", true)!!) {
            gridList.add(gridList.size, itemAlbum_04)
        }
        if (sp?.getBoolean("filter_05", true)!!) {
            gridList.add(gridList.size, itemAlbum_05)
        }
        if (sp?.getBoolean("filter_06", true)!!) {
            gridList.add(gridList.size, itemAlbum_06)
        }
        if (sp?.getBoolean("filter_07", true)!!) {
            gridList.add(gridList.size, itemAlbum_07)
        }
        if (sp?.getBoolean("filter_08", true)!!) {
            gridList.add(gridList.size, itemAlbum_08)
        }
        if (sp?.getBoolean("filter_09", true)!!) {
            gridList.add(gridList.size, itemAlbum_09)
        }
        if (sp?.getBoolean("filter_10", true)!!) {
            gridList.add(gridList.size, itemAlbum_10)
        }
        if (sp?.getBoolean("filter_11", true)!!) {
            gridList.add(gridList.size, itemAlbum_11)
        }
        val gridAdapter = GridAdapter_filter(context, gridList)
        grid.adapter = gridAdapter
        gridAdapter.notifyDataSetChanged()
        grid.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            sp?.edit()?.putString("filter_bookmarks", gridList[position].ordinal)?.apply()
            initBookmarkList()
            hideBottomSheetDialog()
        }
        bottomSheetDialog?.setContentView(dialogView)
        bottomSheetDialog?.show()
        HelperUnit.setBottomSheetBehavior(
            bottomSheetDialog!!,
            dialogView,
            BottomSheetBehavior.STATE_EXPANDED
        )
    }

    private fun setCustomFullscreen(fullscreen: Boolean) {
        val decorView = window.decorView
        if (fullscreen) {
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        } else {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    private fun nextAlbumController(next: Boolean): AlbumController? {
        if (BrowserContainer.size() <= 1) {
            return currentAlbumController
        }
        val list = BrowserContainer.list()
        var index = list.indexOf(currentAlbumController)
        if (next) {
            index++
            if (index >= list.size) {
                index = 0
            }
        } else {
            index--
            if (index < 0) {
                index = list.size - 1
            }
        }
        return list[index]
    }

    companion object {
        private const val INPUT_FILE_REQUEST_CODE = 1
        private fun hideKeyboard(activity: Activity?) {
            val imm =
                activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = activity?.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}