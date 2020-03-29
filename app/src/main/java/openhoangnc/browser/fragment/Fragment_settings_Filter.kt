package openhoangnc.browser.fragment

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import openhoangnc.browser.Ninja.R
import openhoangnc.browser.unit.HelperUnit

class Fragment_settings_Filter : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) {
        setPreferencesFromResource(R.xml.preference_filter, rootKey)
        val sp = preferenceScreen.sharedPreferences
        val filter_01 = findPreference<Preference>("filter_01")
        filter_01!!.title = sp.getString("icon_01", activity!!.resources.getString(R.string.color_red))
        val filter_02 = findPreference<Preference>("filter_02")
        filter_02!!.title = sp.getString("icon_02", activity!!.resources.getString(R.string.color_pink))
        val filter_03 = findPreference<Preference>("filter_03")
        filter_03!!.title = sp.getString("icon_03", activity!!.resources.getString(R.string.color_purple))
        val filter_04 = findPreference<Preference>("filter_04")
        filter_04!!.title = sp.getString("icon_04", activity!!.resources.getString(R.string.color_blue))
        val filter_05 = findPreference<Preference>("filter_05")
        filter_05!!.title = sp.getString("icon_05", activity!!.resources.getString(R.string.color_teal))
        val filter_06 = findPreference<Preference>("filter_06")
        filter_06!!.title = sp.getString("icon_06", activity!!.resources.getString(R.string.color_green))
        val filter_07 = findPreference<Preference>("filter_07")
        filter_07!!.title = sp.getString("icon_07", activity!!.resources.getString(R.string.color_lime))
        val filter_08 = findPreference<Preference>("filter_08")
        filter_08!!.title = sp.getString("icon_08", activity!!.resources.getString(R.string.color_yellow))
        val filter_09 = findPreference<Preference>("filter_09")
        filter_09!!.title = sp.getString("icon_09", activity!!.resources.getString(R.string.color_orange))
        val filter_10 = findPreference<Preference>("filter_10")
        filter_10!!.title = sp.getString("icon_10", activity!!.resources.getString(R.string.color_brown))
        val filter_11 = findPreference<Preference>("filter_11")
        filter_11!!.title = sp.getString("icon_11", activity!!.resources.getString(R.string.color_grey))
        filter_01!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            if (!sp.getBoolean("filter_01", true)) {
                editFilterNames("icon_01", getString(R.string.color_red), filter_01)
                return@OnPreferenceChangeListener true
            }
            true
        }
        filter_02!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            if (!sp.getBoolean("filter_02", true)) {
                editFilterNames("icon_02", getString(R.string.color_pink), filter_02)
                return@OnPreferenceChangeListener true
            }
            true
        }
        filter_03!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            if (!sp.getBoolean("filter_03", true)) {
                editFilterNames("icon_03", getString(R.string.color_purple), filter_03)
                return@OnPreferenceChangeListener true
            }
            true
        }
        filter_04!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            if (!sp.getBoolean("filter_04", true)) {
                editFilterNames("icon_04", getString(R.string.color_blue), filter_04)
                return@OnPreferenceChangeListener true
            }
            true
        }
        filter_05!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            if (!sp.getBoolean("filter_05", true)) {
                editFilterNames("icon_05", getString(R.string.color_teal), filter_05)
                return@OnPreferenceChangeListener true
            }
            true
        }
        filter_06!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            if (!sp.getBoolean("filter_06", true)) {
                editFilterNames("icon_06", getString(R.string.color_green), filter_06)
                return@OnPreferenceChangeListener true
            }
            true
        }
        filter_07!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            if (!sp.getBoolean("filter_07", true)) {
                editFilterNames("icon_07", getString(R.string.color_lime), filter_07)
                return@OnPreferenceChangeListener true
            }
            true
        }
        filter_08!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            if (!sp.getBoolean("filter_08", true)) {
                editFilterNames("icon_08", getString(R.string.color_yellow), filter_08)
                return@OnPreferenceChangeListener true
            }
            true
        }
        filter_09!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            if (!sp.getBoolean("filter_09", true)) {
                editFilterNames("icon_09", getString(R.string.color_orange), filter_09)
                return@OnPreferenceChangeListener true
            }
            true
        }
        filter_10!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            if (!sp.getBoolean("filter_10", true)) {
                editFilterNames("icon_10", getString(R.string.color_brown), filter_10)
                return@OnPreferenceChangeListener true
            }
            true
        }
        filter_11!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            if (!sp.getBoolean("filter_11", true)) {
                editFilterNames("icon_11", getString(R.string.color_grey), filter_11)
                return@OnPreferenceChangeListener true
            }
            true
        }
    }

    private fun editFilterNames(string_spName_icon_01: String, string_spNameDefault: String, preference: Preference?) {
        val bottomSheetDialog = BottomSheetDialog(activity!!)
        val dialogView = View.inflate(activity, R.layout.dialog_edit_title, null)
        val editText = dialogView.findViewById<EditText>(R.id.dialog_edit)
        val sp = preferenceScreen.sharedPreferences
        editText.setHint(R.string.dialog_title_hint)
        editText.setText(sp.getString(string_spName_icon_01, string_spNameDefault))
        val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
        action_ok.setOnClickListener {
            val text = editText.text.toString().trim { it <= ' ' }
            sp.edit().putString(string_spName_icon_01, text).apply()
            preference!!.title = sp.getString(string_spName_icon_01, string_spNameDefault)
            bottomSheetDialog.cancel()
        }
        val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
        action_cancel.setOnClickListener { bottomSheetDialog.cancel() }
        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.show()
        HelperUnit.setBottomSheetBehavior(bottomSheetDialog, dialogView, BottomSheetBehavior.STATE_EXPANDED)
    }
}