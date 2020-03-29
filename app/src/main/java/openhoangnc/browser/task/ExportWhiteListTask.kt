package openhoangnc.browser.task

import android.annotation.SuppressLint
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
class ExportWhiteListTask(private val context: Context?, private val table: Int) : AsyncTask<Void?, Void?, Boolean>() {
    private var dialog: BottomSheetDialog? = null
    private var path: String? = null
    override fun onPreExecute() {
        dialog = BottomSheetDialog(context!!)
        val dialogView = View.inflate(context, R.layout.dialog_progress, null)
        val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
        textView.text = context.getString(R.string.toast_wait_a_minute)
        dialog?.setContentView(dialogView)
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog?.show()
        HelperUnit.setBottomSheetBehavior(dialog!!, dialogView, BottomSheetBehavior.STATE_EXPANDED)
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        path = when (table) {
            0 -> BrowserUnit.exportWhitelist(context, 0)
            1 -> BrowserUnit.exportWhitelist(context, 1)
            else -> BrowserUnit.exportWhitelist(context, 2)
        }
        return !isCancelled && path != null && !path?.isEmpty()!!
    }

    override fun onPostExecute(result: Boolean?) {
        dialog?.hide()
        dialog?.dismiss()
        if (result!!) {
            NinjaToast.show(context, context?.getString(R.string.toast_export_successful) + path)
        } else {
            NinjaToast.show(context, R.string.toast_export_failed)
        }
    }
}