package openhoangnc.browser.fragment

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import openhoangnc.browser.Ninja.R
import openhoangnc.browser.activity.Whitelist_AdBlock
import openhoangnc.browser.activity.Whitelist_Cookie
import openhoangnc.browser.activity.Whitelist_Javascript
import java.util.*

class Fragment_settings_start : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_start, rootKey)
        Objects.requireNonNull<Preference>(findPreference("start_AdBlock")).setOnPreferenceClickListener {
            val intent = Intent(activity, Whitelist_AdBlock::class.java)
            activity?.startActivity(intent)
            false
        }
        Objects.requireNonNull<Preference>(findPreference("start_java")).setOnPreferenceClickListener {
            val intent = Intent(activity, Whitelist_Javascript::class.java)
            activity?.startActivity(intent)
            false
        }
        Objects.requireNonNull<Preference>(findPreference("start_cookie")).setOnPreferenceClickListener {
            val intent = Intent(activity, Whitelist_Cookie::class.java)
            activity?.startActivity(intent)
            false
        }
    }
}