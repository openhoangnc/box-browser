package openhoangnc.browser.task

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import openhoangnc.browser.Ninja.R
import openhoangnc.browser.unit.BrowserUnit
import openhoangnc.browser.unit.HelperUnit
import openhoangnc.browser.view.NinjaToast

@SuppressLint("StaticFieldLeak")
class ImportWhitelistTask(activity: Activity?, i: Int) : AsyncTask<Void?, Void?, Boolean>() {
    private val context: Context?
    private var dialog: BottomSheetDialog?
    private var count: Int
    private val table: Int
    override fun onPreExecute() {
        dialog = BottomSheetDialog(context!!)
        val dialogView = View.inflate(context, R.layout.dialog_progress, null)
        val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
        textView.text = context.getString(R.string.toast_wait_a_minute)
        dialog!!.setContentView(dialogView)
        dialog!!.window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog!!.show()
        HelperUnit.setBottomSheetBehavior(dialog!!, dialogView, BottomSheetBehavior.STATE_EXPANDED)
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        count = when (table) {
            0 -> BrowserUnit.importWhitelist(context, 0)
            1 -> BrowserUnit.importWhitelist(context, 1)
            else -> BrowserUnit.importWhitelist(context, 2)
        }
        return !isCancelled && count >= 0
    }

    override fun onPostExecute(result: Boolean) {
        dialog!!.hide()
        dialog!!.dismiss()
        if (result) {
            NinjaToast.show(context, context!!.getString(R.string.toast_import_successful) + count)
        } else {
            NinjaToast.show(context, R.string.toast_import_failed)
        }
    }

    init {
        context = activity
        dialog = null
        count = 0
        table = i
    }
}