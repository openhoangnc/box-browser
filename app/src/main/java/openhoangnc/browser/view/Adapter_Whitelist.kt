package openhoangnc.browser.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import openhoangnc.browser.Ninja.R
import openhoangnc.browser.browser.Javascript

class Adapter_Whitelist(context: Context, private val list: MutableList<String>?) :
    ArrayAdapter<String>(context, R.layout.whitelist_item, list!!) {
    private val layoutResId: Int = R.layout.whitelist_item

    private class Holder {
        var domain: TextView? = null
        var cancel: ImageButton? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: Holder
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(layoutResId, parent, false)
            holder = Holder()
            holder.domain = view.findViewById(R.id.whitelist_item_domain)
            holder.cancel = view.findViewById(R.id.whitelist_item_cancel)
            view.tag = holder
        } else {
            holder = view.tag as Holder
        }
        holder.domain?.text = list!![position]
        holder.cancel?.setOnClickListener {
            val javascript = Javascript(context)
            javascript.removeDomain(list[position])
            list.removeAt(position)
            notifyDataSetChanged()
            NinjaToast.show(context, R.string.toast_delete_successful)
        }
        return view!!
    }

}