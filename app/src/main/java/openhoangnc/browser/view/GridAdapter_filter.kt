package openhoangnc.browser.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import openhoangnc.browser.Ninja.R

class GridAdapter_filter(private val context: Context?, private val list: List<GridItem_filter>) : BaseAdapter() {
    private class Holder {
        var title: TextView? = null
        var icon: ImageView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: Holder
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_bookmark, parent, false)
            holder = Holder()
            holder.title = view.findViewById(R.id.record_item_title)
            holder.icon = view.findViewById(R.id.ib_icon)
            view.tag = holder
        } else {
            holder = view.tag as Holder
        }
        val item = list[position]
        holder.title?.text = item.title
        holder.icon?.setImageDrawable(item.icon)
        return view!!
    }

    override fun getCount(): Int { // TODO Auto-generated method stub
        return list.size
        //return 0;
    }

    override fun getItem(arg0: Int): Any { // TODO Auto-generated method stub
        return list[arg0]
    }

    override fun getItemId(arg0: Int): Long { // TODO Auto-generated method stub
        return arg0.toLong()
    }

}