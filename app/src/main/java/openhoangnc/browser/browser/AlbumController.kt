package openhoangnc.browser.browser

import android.graphics.Bitmap
import android.view.View

interface AlbumController {
    val albumView: View?
    fun setAlbumCover(bitmap: Bitmap?)
    var albumTitle: String?
    fun activate()
    fun deactivate()
}