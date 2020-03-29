package openhoangnc.browser.task

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import openhoangnc.browser.Ninja.R
import openhoangnc.browser.unit.HelperUnit
import openhoangnc.browser.unit.ViewUnit
import openhoangnc.browser.view.NinjaToast
import openhoangnc.browser.view.NinjaWebView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

@SuppressLint("StaticFieldLeak")
class ScreenshotTask(private val context: Context?, private val webView: NinjaWebView?) : AsyncTask<Void?, Void?, Boolean>() {
    private var windowWidth = 0
    private var contentHeight = 0f
    private var title: String? = null
    private var path: String? = null
    private var uri: Uri? = null
    private var activity: Activity? = null
    private var dialog: BottomSheetDialog? = null
    override fun onPreExecute() {
        activity = context as Activity?
        dialog = BottomSheetDialog(activity!!)
        val dialogView = View.inflate(activity, R.layout.dialog_progress, null)
        val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
        textView.text = context?.getString(R.string.toast_wait_a_minute)
        dialog?.setContentView(dialogView)
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog?.show()
        HelperUnit.setBottomSheetBehavior(dialog!!, dialogView, BottomSheetBehavior.STATE_EXPANDED)
        try {
            windowWidth = ViewUnit.getWindowWidth(context)
            contentHeight = webView?.contentHeight!! * ViewUnit.getDensity(context)
            title = HelperUnit.fileName(webView.url)
        } catch (e: Exception) {
            NinjaToast.show(activity, context?.getString(R.string.toast_error))
        }
    }

    @Throws(IOException::class)
    private fun saveImage(bitmap: Bitmap?, name: String) {
        val fos: OutputStream?
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context?.contentResolver
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                contentValues.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "Pictures/" + "Screenshots/"
                )
                uri = resolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = resolver?.openOutputStream(uri!!)
            } else {
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_SCREENSHOTS)
                        .toString() + File.separator
                val file = File(imagesDir)
                if (!file.exists()) {
                    file.mkdir()
                }
                val image = File(imagesDir, "$name.jpg")
                fos = FileOutputStream(image)
                uri = Uri.fromFile(image)
            }
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos?.flush()
            fos?.close()
        }
    }

    override fun doInBackground(vararg params: Void?): Boolean {
        if (Build.VERSION.SDK_INT < 29) {
            val hasWRITE_EXTERNAL_STORAGE =
                context?.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                HelperUnit.grantPermissionsStorage(activity)
            } else {
                try {
                    val bitmap = ViewUnit.capture(webView, windowWidth.toFloat(), contentHeight, Bitmap.Config.ARGB_8888)
                    saveImage(bitmap, title!!)
                } catch (e: Exception) {
                    path = null
                }
            }
        } else {
            try {
                val bitmap = ViewUnit.capture(webView, windowWidth.toFloat(), contentHeight, Bitmap.Config.ARGB_8888)
                saveImage(bitmap, title!!)
            } catch (e: Exception) {
                path = null
            }
        }
        return path != null && !path?.isEmpty()!!
    }

    override fun onPostExecute(result: Boolean?) {
        dialog?.cancel()
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        if (sp.getInt("screenshot", 0) == 1) {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.setDataAndType(uri, "image/*")
            context?.startActivity(intent)
        } else {
            val bottomSheetDialog = BottomSheetDialog(activity!!)
            val dialogView = View.inflate(activity, R.layout.dialog_action, null)
            val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
            textView.setText(R.string.toast_downloadComplete)
            val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
            action_ok.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, "*/*")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context?.startActivity(intent)
                bottomSheetDialog.cancel()
            }
            val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
            action_cancel.setOnClickListener { bottomSheetDialog.cancel() }
            bottomSheetDialog.setContentView(dialogView)
            bottomSheetDialog.show()
            HelperUnit.setBottomSheetBehavior(bottomSheetDialog, dialogView, BottomSheetBehavior.STATE_EXPANDED)
        }
    }

}