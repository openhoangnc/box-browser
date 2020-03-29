package openhoangnc.browser.view

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources.Theme
import openhoangnc.browser.unit.HelperUnit

class NinjaContextWrapper(private val context: Context) : ContextWrapper(context) {
    override fun getTheme(): Theme {
        return context.theme
    }

    init {
        HelperUnit.applyTheme(context)
    }
}