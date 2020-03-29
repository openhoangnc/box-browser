package openhoangnc.browser.fragment

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import openhoangnc.browser.Ninja.R

class Fragment_settings_Gesture : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) {
        setPreferencesFromResource(R.xml.preference_gesture, rootKey)
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

    override fun onSharedPreferenceChanged(sp: SharedPreferences, key: String) {
        if (key == "sp_gestures_use" || key == "sp_gesture_action") {
            sp.edit().putInt("restart_changed", 1).apply()
        }
    }
}