package openhoangnc.browser.fragment

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import openhoangnc.browser.Ninja.R
import openhoangnc.browser.unit.HelperUnit
import java.util.*

class Fragment_clear : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) {
        setPreferencesFromResource(R.xml.preference_clear, rootKey)
        Objects.requireNonNull<Preference>(findPreference("sp_deleteDatabase")).onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val sp = preferenceScreen.sharedPreferences
            val dialog = BottomSheetDialog(activity!!)
            val dialogView = View.inflate(activity, R.layout.dialog_action, null)
            val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
            textView.setText(R.string.hint_database)
            val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
            action_ok.setOnClickListener {
                dialog.cancel()
                activity!!.deleteDatabase("Ninja4.db")
                activity!!.deleteDatabase("pass_DB_v01.db")
                sp.edit().putInt("restart_changed", 1).apply()
                activity!!.finish()
            }
            val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
            action_cancel.setOnClickListener { dialog.cancel() }
            dialog.setContentView(dialogView)
            dialog.show()
            HelperUnit.setBottomSheetBehavior(dialog, dialogView, BottomSheetBehavior.STATE_EXPANDED)
            false
        }
    }
}