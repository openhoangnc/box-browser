package openhoangnc.browser.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class HolderActivity : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.data?.toString()
        val toActivity = Intent(this@HolderActivity, BrowserActivity::class.java)
        toActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        toActivity.action = Intent.ACTION_SEND
        toActivity.putExtra(Intent.EXTRA_TEXT, url)
        startActivity(toActivity)
        finish()
    }
}