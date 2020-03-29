package openhoangnc.browser.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import openhoangnc.browser.Ninja.R
import openhoangnc.browser.activity.Settings_ClearActivity
import openhoangnc.browser.fragment.Fragment_clear
import openhoangnc.browser.service.ClearService
import openhoangnc.browser.unit.HelperUnit

class Settings_ClearActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        HelperUnit.applyTheme(this)
        setContentView(R.layout.activity_settings)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.content_frame, Fragment_clear())
                .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_clear, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> finish()
            R.id.menu_clear -> {
                val dialog = BottomSheetDialog(this@Settings_ClearActivity)
                val dialogView = View.inflate(this@Settings_ClearActivity, R.layout.dialog_action, null)
                val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
                textView.setText(R.string.hint_database)
                val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
                action_ok.setOnClickListener {
                    val toClearService = Intent(this@Settings_ClearActivity, ClearService::class.java)
                    startService(toClearService)
                    dialog.cancel()
                }
                val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
                action_cancel.setOnClickListener { dialog.cancel() }
                dialog.setContentView(dialogView)
                dialog.show()
                HelperUnit.setBottomSheetBehavior(dialog, dialogView, BottomSheetBehavior.STATE_EXPANDED)
            }
            else -> {
            }
        }
        return true
    }
}