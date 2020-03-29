package openhoangnc.browser.browser

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import openhoangnc.browser.database.RecordAction
import openhoangnc.browser.unit.RecordUnit
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class Cookie(private val context: Context?) {
    fun isWhite(url: String?): Boolean {
        for (domain in whitelistCookie) {
            if (url != null && url.contains(domain!!)) {
                return true
            }
        }
        return false
    }

    @Synchronized
    fun addDomain(domain: String?) {
        val action = RecordAction(context)
        action.open(true)
        action.addDomain(domain, RecordUnit.TABLE_COOKIE)
        action.close()
        whitelistCookie.add(domain)
    }

    @Synchronized
    fun removeDomain(domain: String?) {
        val action = RecordAction(context)
        action.open(true)
        action.deleteDomain(domain, RecordUnit.TABLE_COOKIE)
        action.close()
        whitelistCookie.remove(domain)
    }

    @Synchronized
    fun clearDomains() {
        val action = RecordAction(context)
        action.open(true)
        action.clearDomainsCookie()
        action.close()
        whitelistCookie.clear()
    }

    companion object {
        private const val FILE = "cookieHosts.txt"
        private val hostsCookie: MutableSet<String> = HashSet()
        private val whitelistCookie: MutableList<String?> = ArrayList()

        @SuppressLint("ConstantLocale")
        private val locale = Locale.getDefault()

        private fun loadHosts(context: Context?) {
            val thread = Thread(Runnable {
                val manager = context!!.assets
                try {
                    val reader = BufferedReader(InputStreamReader(manager.open(FILE)))
                    var line: String
                    while (reader.readLine().also { line = it } != null) {
                        hostsCookie.add(line.toLowerCase(locale))
                    }
                } catch (i: IOException) {
                    Log.w("browser", "Error loading hosts")
                }
            })
            thread.start()
        }

        @Synchronized
        private fun loadDomains(context: Context?) {
            val action = RecordAction(context)
            action.open(false)
            whitelistCookie.clear()
            whitelistCookie.addAll(action.listDomains(RecordUnit.TABLE_COOKIE))
            action.close()
        }
    }

    init {
        if (hostsCookie.isEmpty()) {
            loadHosts(context)
        }
        loadDomains(context)
    }
}