package openhoangnc.browser.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import openhoangnc.browser.Ninja.R
import openhoangnc.browser.task.ExportWhiteListTask
import openhoangnc.browser.task.ImportWhitelistTask
import openhoangnc.browser.unit.BrowserUnit
import openhoangnc.browser.unit.HelperUnit
import openhoangnc.browser.view.NinjaToast
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.SAXException
import java.io.*
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

class Fragment_settings_data : PreferenceFragmentCompat() {
    private var dialog: BottomSheetDialog? = null
    override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) {
        setPreferencesFromResource(R.xml.preference_data, rootKey)
        val sd = activity!!.getExternalFilesDir(null)
        val data = Environment.getDataDirectory()
        val previewsPath_app = "//data//" + activity!!.packageName + "//"
        val previewsPath_backup = "browser_backup//data//"
        val previewsFolder_app = File(data, previewsPath_app)
        val previewsFolder_backup = File(sd, previewsPath_backup)
        findPreference<Preference>("data_exAdBlock")!!.setOnPreferenceClickListener {
            val dialogView: View
            val textView: TextView
            val action_ok: Button
            val action_cancel: Button
            dialog = BottomSheetDialog(activity!!)
            dialogView = View.inflate(activity, R.layout.dialog_action, null)
            textView = dialogView.findViewById(R.id.dialog_text)
            textView.setText(R.string.toast_backup)
            action_ok = dialogView.findViewById(R.id.action_ok)
            action_ok.setOnClickListener {
                if (Build.VERSION.SDK_INT < 29) {
                    val hasWRITE_EXTERNAL_STORAGE = activity!!.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                        HelperUnit.grantPermissionsStorage(activity)
                        dialog!!.cancel()
                    } else {
                        dialog!!.cancel()
                        makeBackupDir()
                        ExportWhiteListTask(activity, 0).execute()
                    }
                } else {
                    dialog!!.cancel()
                    makeBackupDir()
                    ExportWhiteListTask(activity, 0).execute()
                }
            }
            action_cancel = dialogView.findViewById(R.id.action_cancel)
            action_cancel.setOnClickListener { dialog!!.cancel() }
            dialog!!.setContentView(dialogView)
            dialog!!.show()
            HelperUnit.setBottomSheetBehavior(dialog!!, dialogView, BottomSheetBehavior.STATE_EXPANDED)
            false
        }

        Objects.requireNonNull<Preference>(findPreference("data_imAdBlock")).setOnPreferenceClickListener {
            val dialogView: View
            val textView: TextView
            val action_ok: Button
            val action_cancel: Button
            dialog = BottomSheetDialog(activity!!)
            dialogView = View.inflate(activity, R.layout.dialog_action, null)
            textView = dialogView.findViewById(R.id.dialog_text)
            textView.setText(R.string.hint_database)
            action_ok = dialogView.findViewById(R.id.action_ok)
            action_ok.setOnClickListener {
                if (Build.VERSION.SDK_INT < 29) {
                    val hasWRITE_EXTERNAL_STORAGE = activity!!.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                        HelperUnit.grantPermissionsStorage(activity)
                        dialog!!.cancel()
                    } else {
                        dialog!!.cancel()
                        ImportWhitelistTask(activity, 0).execute()
                    }
                } else {
                    dialog!!.cancel()
                    ImportWhitelistTask(activity, 0).execute()
                }
            }
            action_cancel = dialogView.findViewById(R.id.action_cancel)
            action_cancel.setOnClickListener { dialog!!.cancel() }
            dialog!!.setContentView(dialogView)
            dialog!!.show()
            HelperUnit.setBottomSheetBehavior(dialog!!, dialogView, BottomSheetBehavior.STATE_EXPANDED)
            false
        }

        Objects.requireNonNull<Preference>(findPreference("data_exCookie")).setOnPreferenceClickListener {
            val dialogView: View
            val textView: TextView
            val action_ok: Button
            val action_cancel: Button
            dialog = BottomSheetDialog(activity!!)
            dialogView = View.inflate(activity, R.layout.dialog_action, null)
            textView = dialogView.findViewById(R.id.dialog_text)
            textView.setText(R.string.toast_backup)
            action_ok = dialogView.findViewById(R.id.action_ok)
            action_ok.setOnClickListener {
                if (Build.VERSION.SDK_INT < 29) {
                    val hasWRITE_EXTERNAL_STORAGE = activity!!.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                        HelperUnit.grantPermissionsStorage(activity)
                        dialog!!.cancel()
                    } else {
                        dialog!!.cancel()
                        makeBackupDir()
                        ExportWhiteListTask(activity, 2).execute()
                    }
                } else {
                    dialog!!.cancel()
                    makeBackupDir()
                    ExportWhiteListTask(activity, 2).execute()
                }
            }
            action_cancel = dialogView.findViewById(R.id.action_cancel)
            action_cancel.setOnClickListener { dialog!!.cancel() }
            dialog!!.setContentView(dialogView)
            dialog!!.show()
            HelperUnit.setBottomSheetBehavior(dialog!!, dialogView, BottomSheetBehavior.STATE_EXPANDED)
            false
        }

        Objects.requireNonNull<Preference>(findPreference("data_imCookie")).setOnPreferenceClickListener {
            val dialogView: View
            val textView: TextView
            val action_ok: Button
            val action_cancel: Button
            dialog = BottomSheetDialog(activity!!)
            dialogView = View.inflate(activity, R.layout.dialog_action, null)
            textView = dialogView.findViewById(R.id.dialog_text)
            textView.setText(R.string.hint_database)
            action_ok = dialogView.findViewById(R.id.action_ok)
            action_ok.setOnClickListener {
                if (Build.VERSION.SDK_INT < 29) {
                    val hasWRITE_EXTERNAL_STORAGE = activity!!.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                        HelperUnit.grantPermissionsStorage(activity)
                        dialog!!.cancel()
                    } else {
                        dialog!!.cancel()
                        ImportWhitelistTask(activity, 2).execute()
                    }
                } else {
                    dialog!!.cancel()
                    ImportWhitelistTask(activity, 2).execute()
                }
            }
            action_cancel = dialogView.findViewById(R.id.action_cancel)
            action_cancel.setOnClickListener { dialog!!.cancel() }
            dialog!!.setContentView(dialogView)
            dialog!!.show()
            HelperUnit.setBottomSheetBehavior(dialog!!, dialogView, BottomSheetBehavior.STATE_EXPANDED)
            false
        }

        Objects.requireNonNull<Preference>(findPreference("data_exJava")).setOnPreferenceClickListener {
            val dialogView: View
            val textView: TextView
            val action_ok: Button
            val action_cancel: Button
            dialog = BottomSheetDialog(activity!!)
            dialogView = View.inflate(activity, R.layout.dialog_action, null)
            textView = dialogView.findViewById(R.id.dialog_text)
            textView.setText(R.string.toast_backup)
            action_ok = dialogView.findViewById(R.id.action_ok)
            action_ok.setOnClickListener {
                if (Build.VERSION.SDK_INT < 29) {
                    val hasWRITE_EXTERNAL_STORAGE = activity!!.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                        HelperUnit.grantPermissionsStorage(activity)
                        dialog!!.cancel()
                    } else {
                        dialog!!.cancel()
                        makeBackupDir()
                        ExportWhiteListTask(activity, 1).execute()
                    }
                } else {
                    dialog!!.cancel()
                    makeBackupDir()
                    ExportWhiteListTask(activity, 1).execute()
                }
            }
            action_cancel = dialogView.findViewById(R.id.action_cancel)
            action_cancel.setOnClickListener { dialog!!.cancel() }
            dialog!!.setContentView(dialogView)
            dialog!!.show()
            HelperUnit.setBottomSheetBehavior(dialog!!, dialogView, BottomSheetBehavior.STATE_EXPANDED)
            false
        }

        Objects.requireNonNull<Preference>(findPreference("data_imJava")).setOnPreferenceClickListener {
            val dialogView: View
            val textView: TextView
            val action_ok: Button
            val action_cancel: Button
            dialog = BottomSheetDialog(activity!!)
            dialogView = View.inflate(activity, R.layout.dialog_action, null)
            textView = dialogView.findViewById(R.id.dialog_text)
            textView.setText(R.string.hint_database)
            action_ok = dialogView.findViewById(R.id.action_ok)
            action_ok.setOnClickListener {
                if (Build.VERSION.SDK_INT < 29) {
                    val hasWRITE_EXTERNAL_STORAGE = activity!!.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                        HelperUnit.grantPermissionsStorage(activity)
                        dialog!!.cancel()
                    } else {
                        dialog!!.cancel()
                        ImportWhitelistTask(activity, 1).execute()
                    }
                } else {
                    dialog!!.cancel()
                    ImportWhitelistTask(activity, 1).execute()
                }
            }
            action_cancel = dialogView.findViewById(R.id.action_cancel)
            action_cancel.setOnClickListener { dialog!!.cancel() }
            dialog!!.setContentView(dialogView)
            dialog!!.show()
            HelperUnit.setBottomSheetBehavior(dialog!!, dialogView, BottomSheetBehavior.STATE_EXPANDED)
            false
        }

        Objects.requireNonNull<Preference>(findPreference("data_exDB")).setOnPreferenceClickListener {
            val dialogView: View
            val textView: TextView
            val action_ok: Button
            val action_cancel: Button
            dialog = BottomSheetDialog(activity!!)
            dialogView = View.inflate(activity, R.layout.dialog_action, null)
            textView = dialogView.findViewById(R.id.dialog_text)
            textView.setText(R.string.toast_backup)
            action_ok = dialogView.findViewById(R.id.action_ok)
            action_ok.setOnClickListener {
                dialog!!.cancel()
                try {
                    if (Build.VERSION.SDK_INT < 29) {
                        val hasWRITE_EXTERNAL_STORAGE = activity!!.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                            HelperUnit.grantPermissionsStorage(activity)
                            dialog!!.cancel()
                        } else {
                            makeBackupDir()
                            BrowserUnit.deleteDir(previewsFolder_backup)
                            copyDirectory(previewsFolder_app, previewsFolder_backup)
                            backupUserPrefs(activity)
                            NinjaToast.show(activity, getString(R.string.toast_export_successful) + "browser_backup")
                        }
                    } else {
                        makeBackupDir()
                        BrowserUnit.deleteDir(previewsFolder_backup)
                        copyDirectory(previewsFolder_app, previewsFolder_backup)
                        backupUserPrefs(activity)
                        NinjaToast.show(activity, getString(R.string.toast_export_successful) + "browser_backup")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            action_cancel = dialogView.findViewById(R.id.action_cancel)
            action_cancel.setOnClickListener { dialog!!.cancel() }
            dialog!!.setContentView(dialogView)
            dialog!!.show()
            HelperUnit.setBottomSheetBehavior(dialog!!, dialogView, BottomSheetBehavior.STATE_EXPANDED)
            false
        }

        Objects.requireNonNull<Preference>(findPreference("data_imDB")).setOnPreferenceClickListener {
            val dialogView: View
            val textView: TextView
            val action_ok: Button
            val action_cancel: Button
            dialog = BottomSheetDialog(activity!!)
            dialogView = View.inflate(activity, R.layout.dialog_action, null)
            textView = dialogView.findViewById(R.id.dialog_text)
            textView.setText(R.string.hint_database)
            action_ok = dialogView.findViewById(R.id.action_ok)
            action_ok.setOnClickListener {
                dialog!!.cancel()
                try {
                    if (Build.VERSION.SDK_INT < 29) {
                        val hasWRITE_EXTERNAL_STORAGE = activity!!.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                            HelperUnit.grantPermissionsStorage(activity)
                            dialog!!.cancel()
                        } else {
                            BrowserUnit.deleteDir(previewsFolder_app)
                            copyDirectory(previewsFolder_backup, previewsFolder_app)
                            restoreUserPrefs(activity)
                            dialogRestart()
                        }
                    } else {
                        BrowserUnit.deleteDir(previewsFolder_app)
                        copyDirectory(previewsFolder_backup, previewsFolder_app)
                        restoreUserPrefs(activity)
                        dialogRestart()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            action_cancel = dialogView.findViewById(R.id.action_cancel)
            action_cancel.setOnClickListener { dialog!!.cancel() }
            dialog!!.setContentView(dialogView)
            dialog!!.show()
            HelperUnit.setBottomSheetBehavior(dialog!!, dialogView, BottomSheetBehavior.STATE_EXPANDED)
            false
        }
    }

    private fun makeBackupDir() {
        val backupDir = File(activity!!.getExternalFilesDir(null), "browser_backup//")
        val noMedia = File(backupDir, "//.nomedia")
        if (Build.VERSION.SDK_INT < 29) {
            val hasWRITE_EXTERNAL_STORAGE = activity!!.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                HelperUnit.grantPermissionsStorage(activity)
            } else {
                if (!backupDir.exists()) {
                    try {
                        backupDir.mkdirs()
                        noMedia.createNewFile()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } else {
            if (!backupDir.exists()) {
                try {
                    backupDir.mkdirs()
                    noMedia.createNewFile()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun dialogRestart() {
        val sp = preferenceScreen.sharedPreferences
        sp.edit().putInt("restart_changed", 1).apply()
    }

    // If targetLocation does not exist, it will be created.
    @Throws(IOException::class)
    private fun copyDirectory(sourceLocation: File, targetLocation: File) {
        if (sourceLocation.isDirectory) {
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw IOException("Cannot create dir " + targetLocation.absolutePath)
            }
            val children = sourceLocation.list()
            for (aChildren in children!!) {
                copyDirectory(File(sourceLocation, aChildren),
                        File(targetLocation, aChildren))
            }
        } else { // make sure the directory we plan to store the recording in exists
            val directory = targetLocation.parentFile
            if (directory != null && !directory.exists() && !directory.mkdirs()) {
                throw IOException("Cannot create dir " + directory.absolutePath)
            }
            val `in`: InputStream = FileInputStream(sourceLocation)
            val out: OutputStream = FileOutputStream(targetLocation)
            // Copy the bits from InputStream to OutputStream
            val buf = ByteArray(1024)
            var len: Int
            while (`in`.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
            `in`.close()
            out.close()
        }
    }

    companion object {
        private fun backupUserPrefs(context: Context?) {
            val prefsFile = File(context!!.filesDir, "../shared_prefs/" + context.packageName + "_preferences.xml")
            val backupFile = File(context.getExternalFilesDir(null),
                    "browser_backup/preferenceBackup.xml")
            try {
                val src = FileInputStream(prefsFile).channel
                val dst = FileOutputStream(backupFile).channel
                dst.transferFrom(src, 0, src.size())
                src.close()
                dst.close()
                NinjaToast.show(context, "Backed up user prefs to " + backupFile.absolutePath)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        @SuppressLint("ApplySharedPref")
        private fun restoreUserPrefs(context: Context?) {
            val backupFile = File(context!!.getExternalFilesDir(null),
                    "browser_backup/preferenceBackup.xml")
            val error: String
            try {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                val editor = sharedPreferences.edit()
                val inputStream: InputStream = FileInputStream(backupFile)
                val docFactory = DocumentBuilderFactory.newInstance()
                val docBuilder = docFactory.newDocumentBuilder()
                val doc = docBuilder.parse(inputStream)
                val root = doc.documentElement
                var child = root.firstChild
                while (child != null) {
                    if (child.nodeType == Node.ELEMENT_NODE) {
                        val element = child as Element
                        val type = element.nodeName
                        val name = element.getAttribute("name")
                        // In my app, all prefs seem to get serialized as either "string" or
// "boolean" - this will need expanding if yours uses any other types!
                        if (type == "string") {
                            val value = element.textContent
                            editor.putString(name, value)
                        } else if (type == "boolean") {
                            val value = element.getAttribute("value")
                            editor.putBoolean(name, value == "true")
                        }
                    }
                    child = child.nextSibling
                }
                editor.commit()
                NinjaToast.show(context, "Restored user prefs from " + backupFile.absolutePath)
                return
            } catch (e: IOException) {
                error = e.message!!
                e.printStackTrace()
            } catch (e: SAXException) {
                error = e.message!!
                e.printStackTrace()
            } catch (e: ParserConfigurationException) {
                error = e.message!!
                e.printStackTrace()
            }
            val toast = Toast.makeText(context, "Failed to restore user prefs from " + backupFile.absolutePath + " - " + error, Toast.LENGTH_SHORT)
            toast.show()
        }
    }
}