package openhoangnc.browser.browser

import android.net.Uri
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient.CustomViewCallback

interface BrowserController {
    fun updateAutoComplete()
    fun updateProgress(progress: Int)
    fun showAlbum(albumController: AlbumController?)
    fun removeAlbum(albumController: AlbumController?)
    fun showFileChooser(filePathCallback: ValueCallback<Array<Uri>>?)
    fun onShowCustomView(view: View?, callback: CustomViewCallback?)
    fun onLongPress(url: String?)
    fun hideOverview()
    fun onHideCustomView(): Boolean
}