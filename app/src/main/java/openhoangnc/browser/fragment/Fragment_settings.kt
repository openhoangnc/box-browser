package openhoangnc.browser.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import openhoangnc.browser.Ninja.R
import openhoangnc.browser.activity.*
import openhoangnc.browser.unit.HelperUnit
import java.util.*

class Fragment_settings : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {
    private var showContributors = false
    override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) {
        setPreferencesFromResource(R.xml.preference_setting, rootKey)
        Objects.requireNonNull<Preference>(findPreference("settings_filter")).setOnPreferenceClickListener {
            val intent = Intent(activity, Settings_FilterActivity::class.java)
            activity!!.startActivity(intent)
            false
        }
        Objects.requireNonNull<Preference>(findPreference("settings_data")).setOnPreferenceClickListener {
            val intent = Intent(activity, SettingsDataActivity::class.java)
            activity!!.startActivity(intent)
            false
        }
        Objects.requireNonNull<Preference>(findPreference("settings_ui")).setOnPreferenceClickListener {
            val intent = Intent(activity, Settings_UIActivity::class.java)
            activity!!.startActivity(intent)
            false
        }
        Objects.requireNonNull<Preference>(findPreference("settings_gesture")).setOnPreferenceClickListener {
            val intent = Intent(activity, SettingsGestureActivity::class.java)
            activity!!.startActivity(intent)
            false
        }
        Objects.requireNonNull<Preference>(findPreference("settings_start")).setOnPreferenceClickListener {
            val intent = Intent(activity, SettingsStartActivity::class.java)
            activity!!.startActivity(intent)
            false
        }
        Objects.requireNonNull<Preference>(findPreference("settings_clear")).setOnPreferenceClickListener {
            val intent = Intent(activity, Settings_ClearActivity::class.java)
            activity!!.startActivity(intent)
            false
        }
        Objects.requireNonNull<Preference>(findPreference("settings_license")).setOnPreferenceClickListener {
            showContributors = false
            showLicenseDialog(getString(R.string.license_title), getString(R.string.license_dialog))
            false
        }
        Objects.requireNonNull<Preference>(findPreference("settings_community")).setOnPreferenceClickListener {
            showContributors = true
            showLicenseDialog(getString(R.string.setting_title_community), getString(R.string.cont_dialog))
            false
        }
        Objects.requireNonNull<Preference>(findPreference("settings_license")).setOnPreferenceClickListener {
            showContributors = false
            showLicenseDialog(getString(R.string.license_title), getString(R.string.license_dialog))
            false
        }
        Objects.requireNonNull<Preference>(findPreference("settings_info")).setOnPreferenceClickListener {
            showContributors = false
            showLicenseDialog(getString(R.string.menu_other_info), getString(R.string.changelog_dialog))
            false
        }
        Objects.requireNonNull<Preference>(findPreference("settings_help")).setOnPreferenceClickListener {
            HelperUnit.show_dialogHelp(activity)
            false
        }
        Objects.requireNonNull<Preference>(findPreference("settings_appSettings")).setOnPreferenceClickListener {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", activity!!.packageName, null)
            intent.data = uri
            activity!!.startActivity(intent)
            false
        }
    }

    override fun onSharedPreferenceChanged(sp: SharedPreferences, key: String) {
        if (key == "userAgent" || key == "sp_search_engine_custom" || key == "@string/sp_search_engine") {
            sp.edit().putInt("restart_changed", 1).apply()
        }
    }

    private fun showLicenseDialog(title: String, text: String) {
        val dialog = BottomSheetDialog(activity!!)
        val dialogView = View.inflate(activity, R.layout.dialog_text, null)
        val dialog_title = dialogView.findViewById<TextView>(R.id.dialog_title)
        dialog_title.text = title
        val dialog_text = dialogView.findViewById<TextView>(R.id.dialog_text)
        dialog_text.text = HelperUnit.textSpannable(text)
        if (showContributors) {
            dialog_text.append("\n\nGaukler Faun\n" +
                    "\u25AA Main developer and initiator of this project\n" +
                    "https://github.com/scoute-dich")
            dialog_text.append("\n\nAli Demirtas\n" +
                    "\u25AA Turkish Translation\n" +
                    "https://github.com/ali-demirtas")
            dialog_text.append("\n\nCGSLURP LLC\n" +
                    "\u25AA Russian translation\n" +
                    "https://crowdin.com/profile/gaich")
            dialog_text.append("\n\nDmitry Gaich\n" +
                    "\u25AA Helped to implement AdBlock and \"request desktop site\" in the previous version of \"BOX Browser\".\n" +
                    "https://github.com/futrDevelopment")
            dialog_text.append("\n\nelement54\n" +
                    "\u25AA fix: keyboard problems (issue #105)\n" +
                    "\u25AA new: option to disable confirmation dialogs on exit\n" +
                    "https://github.com/element54")
            dialog_text.append("\n\nelmru\n" +
                    "\u25AA Taiwan Trad. Chinese Translation\n" +
                    "https://github.com/kogiokka")
            dialog_text.append("\n\nEnrico Monese\n" +
                    "\u25AA Italian Translation\n" +
                    "https://github.com/EnricoMonese")
            dialog_text.append("\n\nFrancois\n" +
                    "\u25AA French Translation\n" +
                    "https://github.com/franco27")
            dialog_text.append("\n\ngh-pmjm\n" +
                    "\u25AA Polish translation\n" +
                    "https://github.com/gh-pmjm")
            dialog_text.append("\n\ngr1sh\n" +
                    "\u25AA fix: some German strings (issues #124, #131)\n" +
                    "https://github.com/gr1sh")
            dialog_text.append("\n\nHarry Heights\n" +
                    "\u25AA Documentation of BOX Browser\n" +
                    " https://github.com/HarryHeights")
            dialog_text.append("\n\nHeimen Stoffels\n" +
                    " \u25AA Dutch translation\n" +
                    "https://github.com/Vistaus")
            dialog_text.append("\n\nHellohat\n" +
                    "\u25AA French translation\n" +
                    "https://github.com/Hellohat")
            dialog_text.append("\n\nHerman Nunez\n" +
                    "\u25AA Spanish translation\n" +
                    "https://github.com/junior012")
            dialog_text.append("\n\nJumping Yang\n" +
                    "\u25AA Chinese translation in the previous version of \"BOX Browser\"\n" +
                    "https://github.com/JumpingYang001")
            dialog_text.append("\n\nlishoujun\n" +
                    "\u25AA Chinese translation\n" +
                    "\u25AA bug hunting\n" +
                    "https://github.com/lishoujun")
            dialog_text.append("\n\nLukas Novotny\n" +
                    "\u25AA Czech translation\n" +
                    "https://crowdin.com/profile/novas78")
            dialog_text.append("\n\nOguz Ersen\n" +
                    "\u25AA Turkish translation\n" +
                    "https://crowdin.com/profile/oersen")
            dialog_text.append("\n\nPeter Bui\n" +
                    "\u25AA more font sizes to choose\n" +
                    "https://github.com/pbui")
            dialog_text.append("\n\nRodolfoCandidoB\n" +
                    "\u25AA Portuguese, Brazilian translation\n" +
                    "https://crowdin.com/profile/RodolfoCandidoB")
            dialog_text.append("\n\nSecangkir Kopi\n" +
                    "\u25AA Indonesian translation\n" +
                    "https://github.com/Secangkir-Kopi")
            dialog_text.append("\n\nSérgio Marques\n" +
                    "\u25AA Portuguese translation\n" +
                    "https://github.com/smarquespt")
            dialog_text.append("\n\nsplinet\n" +
                    "\u25AA Russian translation in the previous version of \"BOX Browser\"\n" +
                    "https://github.com/splinet")
            dialog_text.append("\n\nSkewedZeppelin\n" +
                    "\u25AA Add option to enable Save-Data header\n" +
                    "https://github.com/SkewedZeppelin")
            dialog_text.append("\n\nTobiplayer\n" +
                    "\u25AA added Qwant search engine\n" +
                    "\u25AA option to open new tab instead of exiting\n" +
                    "https://github.com/Tobiplayer")
            dialog_text.append("\n\nVladimir Kosolapov\n" +
                    "\u25AA Russian translation\n" +
                    "https://github.com/0x264f")
            dialog_text.append("\n\nYC L\n" +
                    "\u25AA Chinese Translation\n" +
                    "https://github.com/smallg0at")
        }
        dialog_text.movementMethod = LinkMovementMethod.getInstance()
        dialog.setContentView(dialogView)
        dialog.show()
        HelperUnit.setBottomSheetBehavior(dialog, dialogView, BottomSheetBehavior.STATE_EXPANDED)
    }
}