package openhoangnc.browser.browser

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import openhoangnc.browser.database.RecordAction
import openhoangnc.browser.unit.RecordUnit
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URI
import java.net.URISyntaxException
import java.util.*

class AdBlock(private val context: Context?) {
    fun isWhite(url: String?): Boolean {
        for (domain in whitelist) {
            if (url != null && url.contains(domain!!)) {
                return true
            }
        }
        return false
    }

    fun isAd(url: String): Boolean {
        val domain: String
        domain = try {
            getDomain(url)
        } catch (u: URISyntaxException) {
            return false
        }
        return hosts.contains(domain.toLowerCase(locale))
    }

    @Synchronized
    fun addDomain(domain: String?) {
        val action = RecordAction(context)
        action.open(true)
        action.addDomain(domain, RecordUnit.TABLE_WHITELIST)
        action.close()
        whitelist.add(domain)
    }

    @Synchronized
    fun removeDomain(domain: String?) {
        val action = RecordAction(context)
        action.open(true)
        action.deleteDomain(domain, RecordUnit.TABLE_WHITELIST)
        action.close()
        whitelist.remove(domain)
    }

    @Synchronized
    fun clearDomains() {
        val action = RecordAction(context)
        action.open(true)
        action.clearDomains()
        action.close()
        whitelist.clear()
    }

    companion object {
        private const val FILE = "hosts.txt"
        private val hosts: MutableSet<String> = HashSet()
        private val whitelist: MutableList<String?> = ArrayList()

        @SuppressLint("ConstantLocale")
        private val locale = Locale.getDefault()

        private fun loadHosts(context: Context?) {
            val thread = Thread(Runnable {
                val manager = context!!.assets
                try {
                    val reader = BufferedReader(InputStreamReader(manager.open(FILE)))
                    var line: String? = null
                    while ({ line = reader.readLine(); line }() != null) {
                        hosts.add(line!!.toLowerCase(locale))
                    }
                } catch (i: IOException) {
                    Log.w("browser", "Error loading hosts", i)
                }
            })
            thread.start()
        }

        @Synchronized
        private fun loadDomains(context: Context?) {
            val action = RecordAction(context)
            action.open(false)
            whitelist.clear()
            whitelist.addAll(action.listDomains(RecordUnit.TABLE_WHITELIST))
            action.close()
        }

        @Throws(URISyntaxException::class)
        private fun getDomain(url: String): String {
            var url = url
            url = url.toLowerCase(locale)
            val index = url.indexOf('/', 8) // -> http://(7) and https://(8)
            if (index != -1) {
                url = url.substring(0, index)
            }
            val uri = URI(url)
            val domain = uri.host ?: return url
            return if (domain.startsWith("www.")) domain.substring(4) else domain
        }
    }

    init {
        if (hosts.isEmpty()) {
            loadHosts(context)
        }
        loadDomains(context)
    }
}