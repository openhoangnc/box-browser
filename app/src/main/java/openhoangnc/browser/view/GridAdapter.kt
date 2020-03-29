package openhoangnc.browser.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import openhoangnc.browser.Ninja.R
import openhoangnc.browser.unit.BrowserUnit

class GridAdapter(private val context: Context?, private val list: List<GridItem>?) : BaseAdapter() {
    private class Holder {
        var title: TextView? = null
        var cover: ImageView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: Holder
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false)
            holder = Holder()
            holder.title = view.findViewById(R.id.grid_item_title)
            holder.cover = view.findViewById(R.id.grid_item_cover)
            view.tag = holder
        } else {
            holder = view.tag as Holder
        }
        val item = list!![position]
        holder.title?.text = item.title
        holder.cover?.setImageBitmap(BrowserUnit.file2Bitmap(context, item.filename))
        return view!!
    }

    override fun getCount(): Int { // TODO Auto-generated method stub
        return list?.size!!
    }

    override fun getItem(arg0: Int): Any { // TODO Auto-generated method stub
        return list!![arg0]
    }

    override fun getItemId(arg0: Int): Long { // TODO Auto-generated method stub
        return arg0.toLong()
    }

}