package openhoangnc.browser.view

import android.app.Activity
import android.content.Context
import android.widget.Toast

object NinjaToast {
    fun show(context: Context?, stringResId: Int) {
        show(context, context!!.getString(stringResId))
    }

    fun show(context: Context?, text: String?) {
        val activity = context as Activity?
        val toast = Toast(activity!!.applicationContext)
        toast.setText(text)
        toast.show()
    }
}