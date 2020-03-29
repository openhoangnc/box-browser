/*
    This file is part of the browser WebApp.

    browser WebApp is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    browser WebApp is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the browser webview app.

    If not, see <http://www.gnu.org/licenses/>.
 */
package openhoangnc.browser.unit

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.Html
import android.text.SpannableString
import android.text.TextUtils
import android.text.util.Linkify
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import openhoangnc.browser.Ninja.R
import openhoangnc.browser.view.NinjaToast
import java.text.SimpleDateFormat
import java.util.*

object HelperUnit {
    private const val REQUEST_CODE_ASK_PERMISSIONS = 123
    private const val REQUEST_CODE_ASK_PERMISSIONS_1 = 1234
    private var sp: SharedPreferences? = null
    fun grantPermissionsStorage(activity: Activity?) {
        if (Build.VERSION.SDK_INT < 29) {
            val hasWRITE_EXTERNAL_STORAGE =
                activity?.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                if (!activity?.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)!!) {
                    val bottomSheetDialog = BottomSheetDialog(activity)
                    val dialogView = View.inflate(activity, R.layout.dialog_action, null)
                    val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
                    textView.setText(R.string.toast_permission_sdCard)
                    val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
                    action_ok.setOnClickListener {
                        activity.requestPermissions(
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            REQUEST_CODE_ASK_PERMISSIONS
                        )
                        bottomSheetDialog.cancel()
                    }
                    val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
                    action_cancel.setText(R.string.setting_label)
                    action_cancel.setOnClickListener {
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package", activity.packageName, null)
                        intent.data = uri
                        activity.startActivity(intent)
                        bottomSheetDialog.cancel()
                    }
                    bottomSheetDialog.setContentView(dialogView)
                    bottomSheetDialog.show()
                    setBottomSheetBehavior(
                        bottomSheetDialog,
                        dialogView,
                        BottomSheetBehavior.STATE_EXPANDED
                    )
                }
            }
        }
    }

    fun grantPermissionsLoc(activity: Activity) {
        val hasACCESS_FINE_LOCATION =
            activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        if (hasACCESS_FINE_LOCATION != PackageManager.PERMISSION_GRANTED) {
            val bottomSheetDialog = BottomSheetDialog(activity)
            val dialogView = View.inflate(activity, R.layout.dialog_action, null)
            val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
            textView.setText(R.string.toast_permission_loc)
            val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
            action_ok.setOnClickListener {
                activity.requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_ASK_PERMISSIONS_1
                )
                bottomSheetDialog.cancel()
            }
            val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
            action_cancel.setText(R.string.setting_label)
            action_cancel.setOnClickListener {
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", activity.packageName, null)
                intent.data = uri
                activity.startActivity(intent)
                bottomSheetDialog.cancel()
            }
            bottomSheetDialog.setContentView(dialogView)
            bottomSheetDialog.show()
            setBottomSheetBehavior(
                bottomSheetDialog,
                dialogView,
                BottomSheetBehavior.STATE_EXPANDED
            )
        }
    }

    fun applyTheme(context: Context) {
        sp = PreferenceManager.getDefaultSharedPreferences(context)
        val showNavButton = sp?.getString("sp_theme", "1")
        when (showNavButton) {
            "0" -> context.setTheme(R.style.AppTheme_system)
            "1" -> context.setTheme(R.style.AppTheme)
            "2" -> context.setTheme(R.style.AppTheme_dark)
            "3" -> context.setTheme(R.style.AppTheme_amoled)
            else -> context.setTheme(R.style.AppTheme)
        }
    }

    fun setFavorite(context: Context?, url: String?) {
        sp = PreferenceManager.getDefaultSharedPreferences(context)
        sp?.edit()?.putString("favoriteURL", url)?.apply()
        NinjaToast.show(context, R.string.toast_fav)
    }

    fun setBottomSheetBehavior(dialog: BottomSheetDialog, view: View, beh: Int) {
        val mBehavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(view.parent as View)
        mBehavior.state = beh
        mBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dialog.cancel()
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    fun createShortcut(context: Context?, title: String?, url: String?) {
        try {
            val i = Intent()
            i.action = Intent.ACTION_VIEW
            i.data = Uri.parse(url)
            val shortcutManager = context?.getSystemService(ShortcutManager::class.java)!!
            if (shortcutManager.isRequestPinShortcutSupported) {
                val pinShortcutInfo = ShortcutInfo.Builder(context, url)
                    .setShortLabel(title!!)
                    .setLongLabel(title)
                    .setIcon(Icon.createWithResource(context, R.drawable.qc_bookmarks))
                    .setIntent(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    .build()
                shortcutManager.requestPinShortcut(pinShortcutInfo, null)
            } else {
                println("failed_to_add")
            }
        } catch (e: Exception) {
            println("failed_to_add")
        }
    }

    fun show_dialogHelp(context: Context?) {
        val bottomSheetDialog = BottomSheetDialog(context!!)
        val dialogView = View.inflate(context, R.layout.dialog_help, null)
        val dialogHelp_tip = dialogView.findViewById<ImageButton>(R.id.dialogHelp_tip)
        val dialogHelp_overview = dialogView.findViewById<ImageButton>(R.id.dialogHelp_overview)
        val dialogHelp_tipView = dialogView.findViewById<View>(R.id.dialogHelp_tipView)
        val dialogHelp_overviewView = dialogView.findViewById<View>(R.id.dialogHelp_overviewView)
        val dialogHelp_tv_title = dialogView.findViewById<TextView>(R.id.dialogHelp_title)
        val dialogHelp_tv_text = dialogView.findViewById<TextView>(R.id.dialogHelp_tv)
        dialogHelp_tv_title.text =
            textSpannable(context.resources.getString(R.string.dialogHelp_tipTitle))
        dialogHelp_tv_text.text =
            textSpannable(context.resources.getString(R.string.dialogHelp_tipText))
        dialogHelp_tip.setOnClickListener {
            dialogHelp_tipView.visibility = View.VISIBLE
            dialogHelp_overviewView.visibility = View.GONE
            dialogHelp_tv_title.text =
                textSpannable(context.resources.getString(R.string.dialogHelp_tipTitle))
            dialogHelp_tv_text.text =
                textSpannable(context.resources.getString(R.string.dialogHelp_tipText))
        }
        dialogHelp_overview.setOnClickListener {
            dialogHelp_tipView.visibility = View.GONE
            dialogHelp_overviewView.visibility = View.VISIBLE
            dialogHelp_tv_title.text =
                textSpannable(context.resources.getString(R.string.dialogHelp_overviewTitle))
            dialogHelp_tv_text.text =
                textSpannable(context.resources.getString(R.string.dialogHelp_overviewText))
        }
        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.show()
        setBottomSheetBehavior(bottomSheetDialog, dialogView, BottomSheetBehavior.STATE_EXPANDED)
    }

    fun switchIcon(activity: Activity?, string: String?, fieldDB: String?, be: ImageView?) {
        sp = PreferenceManager.getDefaultSharedPreferences(activity)
        assert(be != null)
        when (string) {
            "02" -> {
                be?.setImageResource(R.drawable.circle_pink_big)
                sp?.edit()?.putString(fieldDB, "02")?.apply()
            }
            "03" -> {
                be?.setImageResource(R.drawable.circle_purple_big)
                sp?.edit()?.putString(fieldDB, "03")?.apply()
            }
            "04" -> {
                be?.setImageResource(R.drawable.circle_blue_big)
                sp?.edit()?.putString(fieldDB, "04")?.apply()
            }
            "05" -> {
                be?.setImageResource(R.drawable.circle_teal_big)
                sp?.edit()?.putString(fieldDB, "05")?.apply()
            }
            "06" -> {
                be?.setImageResource(R.drawable.circle_green_big)
                sp?.edit()?.putString(fieldDB, "06")?.apply()
            }
            "07" -> {
                be?.setImageResource(R.drawable.circle_lime_big)
                sp?.edit()?.putString(fieldDB, "07")?.apply()
            }
            "08" -> {
                be?.setImageResource(R.drawable.circle_yellow_big)
                sp?.edit()?.putString(fieldDB, "08")?.apply()
            }
            "09" -> {
                be?.setImageResource(R.drawable.circle_orange_big)
                sp?.edit()?.putString(fieldDB, "09")?.apply()
            }
            "10" -> {
                be?.setImageResource(R.drawable.circle_brown_big)
                sp?.edit()?.putString(fieldDB, "10")?.apply()
            }
            "11" -> {
                be?.setImageResource(R.drawable.circle_grey_big)
                sp?.edit()?.putString(fieldDB, "11")?.apply()
            }
            "01" -> {
                be?.setImageResource(R.drawable.circle_red_big)
                sp?.edit()?.putString(fieldDB, "01")?.apply()
            }
            else -> {
                be?.setImageResource(R.drawable.circle_red_big)
                sp?.edit()?.putString(fieldDB, "01")?.apply()
            }
        }
    }

    fun fileName(url: String?): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val currentTime = sdf.format(Date())
        val domain = Uri.parse(url)?.host?.replace("www.", "")?.trim { it <= ' ' }
        return domain?.replace(".", "_")?.trim { it <= ' ' } + "_" + currentTime.trim { it <= ' ' }
    }

    fun secString(string: String): String {
        return if (TextUtils.isEmpty(string)) {
            ""
        } else {
            string.replace("'".toRegex(), "\'\'")
        }
    }

    fun domain(url: String?): String {
        return if (url == null) {
            ""
        } else {
            try {
                Uri.parse(url)?.host?.replace("www.", "")?.trim { it <= ' ' }!!
            } catch (e: Exception) {
                ""
            }
        }
    }

    fun textSpannable(text: String?): SpannableString {
        val s = SpannableString(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY))
        Linkify.addLinks(s, Linkify.WEB_URLS)
        return s
    }

    private val NEGATIVE_COLOR = floatArrayOf(
        -1.0f,
        0f,
        0f,
        0f,
        255f,
        0f,
        -1.0f,
        0f,
        0f,
        255f,
        0f,
        0f,
        -1.0f,
        0f,
        255f,
        0f,
        0f,
        0f,
        1.0f,
        0f
    )

    fun initRendering(view: View?) {
        if (sp?.getBoolean("sp_invert", false)!!) {
            val paint = Paint()
            val matrix = ColorMatrix()
            matrix.set(NEGATIVE_COLOR)
            val gcm = ColorMatrix()
            gcm.setSaturation(0f)
            val concat = ColorMatrix()
            concat.setConcat(matrix, gcm)
            val filter = ColorMatrixColorFilter(concat)
            paint.colorFilter = filter
            // maybe sometime LAYER_TYPE_NONE would better?
            view?.setLayerType(View.LAYER_TYPE_HARDWARE, paint)
        } else {
            view?.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }
    }
}