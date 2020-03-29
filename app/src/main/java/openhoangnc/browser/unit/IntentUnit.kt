package openhoangnc.browser.unit

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import openhoangnc.browser.Ninja.R

object IntentUnit {
    fun share(context: Context?, title: String?, url: String?) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, title)
        sharingIntent.putExtra(Intent.EXTRA_TEXT, url)
        context!!.startActivity(Intent.createChooser(sharingIntent, context.getString(R.string.menu_share_link)))
    }

    // activity holder
    @SuppressLint("StaticFieldLeak")
    var context: Context? = null

}