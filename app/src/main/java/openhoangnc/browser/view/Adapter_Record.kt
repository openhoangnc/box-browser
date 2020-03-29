package openhoangnc.browser.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import openhoangnc.browser.Ninja.R
import openhoangnc.browser.database.Record
import java.text.SimpleDateFormat
import java.util.*

class Adapter_Record(context: Context?, list: List<Record?>?) : ArrayAdapter<Record?>(context!!, R.layout.list_item, list!!) {
    private val layoutResId: Int
    private val list: List<Record?>?

    private class Holder {
        var title: TextView? = null
        var time: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: Holder
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(layoutResId, parent, false)
            holder = Holder()
            holder.title = view.findViewById(R.id.record_item_title)
            holder.time = view.findViewById(R.id.record_item_time)
            view.tag = holder
        } else {
            holder = view.tag as Holder
        }
        val record = list!![position]
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        holder.title?.text = record?.title
        holder.time?.text = sdf.format(record?.time)
        return view!!
    }

    init {
        layoutResId = R.layout.list_item
        this.list = list
    }
}