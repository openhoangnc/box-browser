package openhoangnc.browser.unit

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import openhoangnc.browser.Ninja.R
import openhoangnc.browser.browser.AdBlock
import openhoangnc.browser.browser.Cookie
import openhoangnc.browser.browser.Javascript
import openhoangnc.browser.database.RecordAction
import openhoangnc.browser.view.NinjaToast
import java.io.*
import java.net.URLEncoder
import java.util.*
import java.util.regex.Pattern

object BrowserUnit {
    const val PROGRESS_MAX = 100
    const val SUFFIX_PNG = ".png"
    private const val SUFFIX_TXT = ".txt"
    const val MIME_TYPE_TEXT_PLAIN = "text/plain"
    private const val SEARCH_ENGINE_GOOGLE = "https://www.google.com/search?q="
    private const val SEARCH_ENGINE_DUCKDUCKGO = "https://duckduckgo.com/?q="
    private const val SEARCH_ENGINE_STARTPAGE = "https://startpage.com/do/search?query="
    private const val SEARCH_ENGINE_BING = "http://www.bing.com/search?q="
    private const val SEARCH_ENGINE_BAIDU = "https://www.baidu.com/s?wd="
    private const val SEARCH_ENGINE_QWANT = "https://www.qwant.com/?q="
    private const val SEARCH_ENGINE_ECOSIA = "https://www.ecosia.org/search?q="
    private const val SEARCH_ENGINE_STARTPAGE_DE = "https://startpage.com/do/search?lui=deu&language=deutsch&query="
    private const val SEARCH_ENGINE_SEARX = "https://searx.me/?q="
    const val URL_ENCODING = "UTF-8"
    private const val URL_ABOUT_BLANK = "about:blank"
    const val URL_SCHEME_ABOUT = "about:"
    const val URL_SCHEME_MAIL_TO = "mailto:"
    private const val URL_SCHEME_FILE = "file://"
    private const val URL_SCHEME_HTTP = "https://"
    const val URL_SCHEME_INTENT = "intent://"
    private const val URL_PREFIX_GOOGLE_PLAY = "www.google.com/url?q="
    private const val URL_SUFFIX_GOOGLE_PLAY = "&sa"
    private const val URL_PREFIX_GOOGLE_PLUS = "plus.url.google.com/url?q="
    private const val URL_SUFFIX_GOOGLE_PLUS = "&rct"
    fun isURL(url: String?): Boolean {
        var url = url ?: return false
        url = url.toLowerCase(Locale.getDefault())
        if (url.startsWith(URL_ABOUT_BLANK)
                || url.startsWith(URL_SCHEME_MAIL_TO)
                || url.startsWith(URL_SCHEME_FILE)) {
            return true
        }
        val regex = ("^((ftp|http|https|intent)?://)" // support scheme
                + "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" // ftp的user@
                + "(([0-9]{1,3}\\.){3}[0-9]{1,3}" // IP形式的URL -> 199.194.52.184
                + "|" // 允许IP和DOMAIN（域名）
                + "(.)*" // 域名 -> www.
// + "([0-9a-z_!~*'()-]+\\.)*"                               // 域名 -> www.
                + "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\." // 二级域名
                + "[a-z]{2,6})" // first level domain -> .com or .museum
                + "(:[0-9]{1,4})?" // 端口 -> :80
                + "((/?)|" // a slash isn't required if there is no file name
                + "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$")
        val pattern = Pattern.compile(regex)
        return pattern.matcher(url).matches()
    }

    fun queryWrapper(context: Context?, query: String): String { // Use prefix and suffix to process some special links
        var query = query
        val temp = query.toLowerCase(Locale.getDefault())
        if (temp.contains(URL_PREFIX_GOOGLE_PLAY) && temp.contains(URL_SUFFIX_GOOGLE_PLAY)) {
            val start = temp.indexOf(URL_PREFIX_GOOGLE_PLAY) + URL_PREFIX_GOOGLE_PLAY.length
            val end = temp.indexOf(URL_SUFFIX_GOOGLE_PLAY)
            query = query.substring(start, end)
        } else if (temp.contains(URL_PREFIX_GOOGLE_PLUS) && temp.contains(URL_SUFFIX_GOOGLE_PLUS)) {
            val start = temp.indexOf(URL_PREFIX_GOOGLE_PLUS) + URL_PREFIX_GOOGLE_PLUS.length
            val end = temp.indexOf(URL_SUFFIX_GOOGLE_PLUS)
            query = query.substring(start, end)
        }
        if (isURL(query)) {
            if (query.startsWith(URL_SCHEME_ABOUT) || query.startsWith(URL_SCHEME_MAIL_TO)) {
                return query
            }
            if (!query.contains("://")) {
                query = URL_SCHEME_HTTP + query
            }
            return query
        }
        try {
            query = URLEncoder.encode(query, URL_ENCODING)
        } catch (u: UnsupportedEncodingException) {
            Log.w("browser", "Unsupported Encoding Exception")
        }
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val custom = sp.getString("sp_search_engine_custom", SEARCH_ENGINE_STARTPAGE)
        val i = Integer.valueOf(sp.getString(context!!.getString(R.string.sp_search_engine), "9")!!)
        return when (i) {
            0 -> SEARCH_ENGINE_STARTPAGE + query
            1 -> SEARCH_ENGINE_STARTPAGE_DE + query
            2 -> SEARCH_ENGINE_BAIDU + query
            3 -> SEARCH_ENGINE_BING + query
            4 -> SEARCH_ENGINE_DUCKDUCKGO + query
            5 -> SEARCH_ENGINE_GOOGLE + query
            6 -> SEARCH_ENGINE_SEARX + query
            7 -> SEARCH_ENGINE_QWANT + query
            8 -> custom + query
            9 -> SEARCH_ENGINE_ECOSIA + query
            else -> SEARCH_ENGINE_ECOSIA + query
        }
    }

    fun bitmap2File(context: Context?, bitmap: Bitmap?, filename: String?): Boolean {
        try {
            val fileOutputStream = context!!.openFileOutput(filename, Context.MODE_PRIVATE)
            bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: Exception) {
            return false
        }
        return true
    }

    fun file2Bitmap(context: Context?, filename: String?): Bitmap? {
        return try {
            val fileInputStream = context!!.openFileInput(filename)
            BitmapFactory.decodeStream(fileInputStream)
        } catch (e: Exception) {
            null
        }
    }

    fun download(context: Context?, url: String?, contentDisposition: String?, mimeType: String?) {
        val text = context!!.getString(R.string.dialog_title_download) + " - " + URLUtil.guessFileName(url, contentDisposition, mimeType)
        val dialog = BottomSheetDialog(context)
        val dialogView = View.inflate(context, R.layout.dialog_action, null)
        val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
        textView.text = text
        val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
        action_ok.setOnClickListener {
            val request = DownloadManager.Request(Uri.parse(url))
            val filename = URLUtil.guessFileName(url, contentDisposition, mimeType) // Maybe unexpected filename.
            val cookieManager = CookieManager.getInstance()
            val cookie = cookieManager.getCookie(url)
            request.addRequestHeader("Cookie", cookie)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setTitle(filename)
            request.setMimeType(mimeType)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
            val manager = (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
            if (Build.VERSION.SDK_INT < 29) {
                val hasWRITE_EXTERNAL_STORAGE = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                    val activity = context as Activity?
                    HelperUnit.grantPermissionsStorage(activity)
                } else {
                    manager.enqueue(request)
                    try {
                        NinjaToast.show(context, R.string.toast_start_download)
                    } catch (e: Exception) {
                        Toast.makeText(context, R.string.toast_start_download, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                manager.enqueue(request)
                try {
                    NinjaToast.show(context, R.string.toast_start_download)
                } catch (e: Exception) {
                    Toast.makeText(context, R.string.toast_start_download, Toast.LENGTH_SHORT).show()
                }
            }
            dialog.cancel()
        }
        val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
        action_cancel.setOnClickListener { dialog.cancel() }
        dialog.setContentView(dialogView)
        dialog.show()
        HelperUnit.setBottomSheetBehavior(dialog, dialogView, BottomSheetBehavior.STATE_EXPANDED)
    }

    fun exportWhitelist(context: Context?, i: Int): String? {
        val action = RecordAction(context)
        val list: List<String?>?
        val filename: String
        action.open(false)
        when (i) {
            0 -> {
                list = action.listDomains(RecordUnit.TABLE_WHITELIST)
                filename = context!!.getString(R.string.export_whitelistAdBlock)
            }
            1 -> {
                list = action.listDomains(RecordUnit.TABLE_JAVASCRIPT)
                filename = context!!.getString(R.string.export_whitelistJS)
            }
            else -> {
                list = action.listDomains(RecordUnit.TABLE_COOKIE)
                filename = context!!.getString(R.string.export_whitelistCookie)
            }
        }
        action.close()
        val file = File(context.getExternalFilesDir(null), "browser_backup//$filename$SUFFIX_TXT")
        return try {
            val writer = BufferedWriter(FileWriter(file, false))
            for (domain in list) {
                writer.write(domain)
                writer.newLine()
            }
            writer.close()
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun importWhitelist(context: Context?, i: Int): Int {
        var count = 0
        try {
            val filename: String
            var adBlock: AdBlock? = null
            var js: Javascript? = null
            var cookie: Cookie? = null
            when (i) {
                0 -> {
                    adBlock = AdBlock(context)
                    filename = context!!.getString(R.string.export_whitelistAdBlock)
                }
                1 -> {
                    js = Javascript(context)
                    filename = context!!.getString(R.string.export_whitelistJS)
                }
                else -> {
                    cookie = Cookie(context)
                    filename = context!!.getString(R.string.export_whitelistAdBlock)
                }
            }
            val file = File(context.getExternalFilesDir(null), "browser_backup//$filename$SUFFIX_TXT")
            val action = RecordAction(context)
            action.open(true)
            val reader = BufferedReader(FileReader(file))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                when (i) {
                    0 -> if (!action.checkDomain(line, RecordUnit.TABLE_WHITELIST)) {
                        adBlock!!.addDomain(line)
                        count++
                    }
                    1 -> if (!action.checkDomain(line, RecordUnit.TABLE_JAVASCRIPT)) {
                        js!!.addDomain(line)
                        count++
                    }
                    else -> if (!action.checkDomain(line, RecordUnit.COLUMN_DOMAIN)) {
                        cookie!!.addDomain(line)
                        count++
                    }
                }
            }
            reader.close()
            action.close()
        } catch (e: Exception) {
            Log.w("browser", "Error reading file", e)
        }
        return count
    }

    fun clearHome(context: Context?) {
        val action = RecordAction(context)
        action.open(true)
        action.clearHome()
        action.close()
    }

    fun clearCache(context: Context) {
        try {
            val dir = context.cacheDir
            if (dir != null && dir.isDirectory) {
                deleteDir(dir)
            }
        } catch (exception: Exception) {
            Log.w("browser", "Error clearing cache")
        }
    }

    fun clearCookie() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.flush()
        cookieManager.removeAllCookies { }
    }

    fun clearHistory(context: Context?) {
        val action = RecordAction(context)
        action.open(true)
        action.clearHistory()
        action.close()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = context!!.getSystemService(ShortcutManager::class.java)
            shortcutManager!!.removeAllDynamicShortcuts()
        }
    }

    fun clearIndexedDB(context: Context) {
        val data = Environment.getDataDirectory()
        val indexedDB = "//data//" + context.packageName + "//app_webview//" + "//IndexedDB"
        val localStorage = "//data//" + context.packageName + "//app_webview//" + "//Local Storage"
        val indexedDB_dir = File(data, indexedDB)
        val localStorage_dir = File(data, localStorage)
        deleteDir(indexedDB_dir)
        deleteDir(localStorage_dir)
    }

    fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (aChildren in children!!) {
                val success = deleteDir(File(dir, aChildren))
                if (!success) {
                    return false
                }
            }
        }
        return dir != null && dir.delete()
    }
}