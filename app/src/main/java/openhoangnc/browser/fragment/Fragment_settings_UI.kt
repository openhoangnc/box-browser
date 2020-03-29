package openhoangnc.browser.fragment

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import openhoangnc.browser.Ninja.R

class Fragment_settings_UI : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_ui, rootKey)
    }

    override fun onResume() {
        super.onResume()
        val sp = preferenceScreen.sharedPreferences
        sp.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sp: SharedPreferences?, key: String?) {
        if (key == "sp_exit" || key == "sp_toggle" || key == "sp_add" || key == "sp_theme" || key == "nav_position" || key == "sp_hideOmni" || key == "start_tab" || key == "sp_hideSB" || key == "overView_place" || key == "overView_hide") {
            sp?.edit()?.putInt("restart_changed", 1)?.apply()
        }
    }
}