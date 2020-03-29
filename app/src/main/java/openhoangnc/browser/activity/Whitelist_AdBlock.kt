package openhoangnc.browser.activity

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import openhoangnc.browser.Ninja.R
import openhoangnc.browser.browser.AdBlock
import openhoangnc.browser.database.RecordAction
import openhoangnc.browser.unit.BrowserUnit
import openhoangnc.browser.unit.HelperUnit
import openhoangnc.browser.unit.RecordUnit
import openhoangnc.browser.view.Adapter_Whitelist
import openhoangnc.browser.view.NinjaToast

class Whitelist_AdBlock : AppCompatActivity() {
    private var adapter: Adapter_Whitelist? = null
    private var list: MutableList<String>? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        HelperUnit.applyTheme(this)
        setContentView(R.layout.activity_whitelist)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val action = RecordAction(this)
        action.open(false)
        list = action.listDomains(RecordUnit.TABLE_WHITELIST)
        action.close()
        val listView = findViewById<ListView>(R.id.whitelist)
        listView.emptyView = findViewById(R.id.whitelist_empty)
        adapter = Adapter_Whitelist(this, list)
        listView.adapter = adapter
        adapter?.notifyDataSetChanged()
        val button = findViewById<Button>(R.id.whitelist_add)
        button.setOnClickListener {
            val editText = findViewById<EditText>(R.id.whitelist_edit)
            val domain = editText.text.toString().trim { it <= ' ' }
            if (domain.isEmpty()) {
                NinjaToast.show(this@Whitelist_AdBlock, R.string.toast_input_empty)
            } else if (!BrowserUnit.isURL(domain)) {
                NinjaToast.show(this@Whitelist_AdBlock, R.string.toast_invalid_domain)
            } else {
                val action = RecordAction(this@Whitelist_AdBlock)
                action.open(true)
                if (action.checkDomain(domain, RecordUnit.TABLE_WHITELIST)) {
                    NinjaToast.show(this@Whitelist_AdBlock, R.string.toast_domain_already_exists)
                } else {
                    val adBlock = AdBlock(this@Whitelist_AdBlock)
                    adBlock.addDomain(domain.trim { it <= ' ' })
                    list?.add(0, domain.trim { it <= ' ' })
                    adapter?.notifyDataSetChanged()
                    NinjaToast.show(this@Whitelist_AdBlock, R.string.toast_add_whitelist_successful)
                }
                action.close()
            }
        }
    }

    public override fun onPause() {
        hideSoftInput(findViewById(R.id.whitelist_edit))
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_clear, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> finish()
            R.id.menu_clear -> {
                val dialog = BottomSheetDialog(this@Whitelist_AdBlock)
                val dialogView = View.inflate(this@Whitelist_AdBlock, R.layout.dialog_action, null)
                val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
                textView.setText(R.string.hint_database)
                val action_ok = dialogView.findViewById<Button>(R.id.action_ok)
                action_ok.setOnClickListener {
                    val adBlock = AdBlock(this@Whitelist_AdBlock)
                    adBlock.clearDomains()
                    list?.clear()
                    adapter?.notifyDataSetChanged()
                    dialog.cancel()
                }
                val action_cancel = dialogView.findViewById<Button>(R.id.action_cancel)
                action_cancel.setOnClickListener { dialog.cancel() }
                dialog.setContentView(dialogView)
                dialog.show()
            }
            else -> {
            }
        }
        return true
    }

    private fun hideSoftInput(view: View) {
        view.clearFocus()
        val imm = (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}