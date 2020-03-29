package openhoangnc.browser.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import openhoangnc.browser.Ninja.R
import openhoangnc.browser.database.Record
import java.util.*

class CompleteAdapter(private val context: Context, private val layoutResId: Int, recordList: List<Record?>?) : BaseAdapter(), Filterable {
    private inner class CompleteFilter : Filter() {
        override fun performFiltering(prefix: CharSequence?): FilterResults {
            if (prefix == null) {
                return FilterResults()
            }
            resultList.clear()
            for (item in originalList) {
                if (item.title?.contains(prefix)!! || item.url?.contains(prefix)!!) {
                    if (item.title.contains(prefix)) {
                        item.index = item.title.indexOf(prefix.toString())
                    } else if (item.url?.contains(prefix)!!) {
                        item.index = item.url.indexOf(prefix.toString())
                    }
                    resultList.add(item)
                }
            }
            resultList.sortWith(Comparator { first, second -> first.index.compareTo(second.index) })
            val results = FilterResults()
            results.values = resultList
            results.count = resultList.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            notifyDataSetChanged()
        }
    }

    private inner class CompleteItem(val title: String?, val url: String?) {

        var index = Int.MAX_VALUE

        override fun equals(other: Any?): Boolean {
            if (other !is CompleteItem) {
                return false
            }
            return other.title == title && other.url == url
        }

        override fun hashCode(): Int {
            return if (title == null || url == null) {
                0
            } else title.hashCode() and url.hashCode()
        }

    }

    private class Holder {
        var titleView: TextView? = null
        var urlView: TextView? = null

    }

    private val originalList: MutableList<CompleteItem>
    private val resultList: MutableList<CompleteItem>
    private val filter = CompleteFilter()
    private fun dedup(recordList: List<Record?>?) {
        for (record in recordList!!) {
            if (record?.title != null && !record.title?.isEmpty()!!
                && record.url != null && !record.url?.isEmpty()!!
            ) {
                originalList.add(CompleteItem(record.title, record.url))
            }
        }
        val set: Set<CompleteItem> = HashSet(originalList)
        originalList.clear()
        originalList.addAll(set)
    }

    override fun getCount(): Int {
        return resultList.size
    }

    override fun getFilter(): Filter {
        return filter
    }

    override fun getItem(position: Int): Any {
        return resultList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val holder: Holder
        if (view == null) {
            view = LayoutInflater.from(context).inflate(layoutResId, null, false)
            holder = Holder()
            holder.titleView = view.findViewById(R.id.complete_item_title)
            holder.urlView = view.findViewById(R.id.complete_item_url)
            view.tag = holder
        } else {
            holder = view.tag as Holder
        }

        val item = resultList[position]
        holder.titleView?.text = item.title
        holder.urlView?.text = item.url
        return view!!
    }

    init {
        originalList = ArrayList()
        resultList = ArrayList()
        dedup(recordList)
    }
}