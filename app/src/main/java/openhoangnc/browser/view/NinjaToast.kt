package openhoangnc.browser.view

import android.content.Context
import android.widget.Toast

object NinjaToast {
    fun show(context: Context?, stringResId: Int) {
        show(context, context?.getString(stringResId))
    }

    fun show(context: Context?, text: String?) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
}