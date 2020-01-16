package com.sudoajay.pdf_viewer.recyclerView

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import com.sudoajay.pdf_viewer.MainActivity
import com.sudoajay.pdf_viewer.R
import com.sudoajay.pdf_viewer.R.layout.layout_recycler_view
import com.sudoajay.pdf_viewer.R.layout.layout_scan_sdcard
import com.sudoajay.pdf_viewer.databaseClasses.Database
import com.sudoajay.pdf_viewer.helperClass.FileSize.convertIt
import com.sudoajay.pdf_viewer.permission.AndroidSdCardPermission
import com.sudoajay.pdf_viewer.recyclerView.MyAdapter.MyViewHolder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MyAdapter(mainActivity: MainActivity, item: ArrayList<String>) : RecyclerView.Adapter<MyViewHolder>(), Filterable {
    var item: ArrayList<String>
    private var database: Database? = null
    private val androidSdCardPermission: AndroidSdCardPermission
    private val mainActivity: MainActivity

    // Provide a reference to the views for each data item
// Complex data items may need more than one view per item, and
// you provide access to all the views for a data item in a view holder
    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        // each data item is just a string in this case
        var nameTextView: TextView? = v.findViewById(R.id.nameTextView)
        var infoTextView: TextView? = v.findViewById(R.id.infoTextView)
        var coverImageView: ImageView? = v.findViewById(R.id.coverImageView)
        var moreOptionImageView: ImageView? = v.findViewById(R.id.moreOption_imageView)
        var scanSdCardButton: Button? = v.findViewById(R.id.scanSdCard_Button)

    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        val listItem: View = if (viewType == layout_recycler_view) {
            LayoutInflater.from(parent.context).inflate(layout_recycler_view, parent, false)
        } else {
            LayoutInflater.from(parent.context).inflate(layout_scan_sdcard, parent, false)
        }
        return MyViewHolder(listItem)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) { // - get element from your dataset at this position
// - replace the contents of the view with that element
        val sdf = SimpleDateFormat(" , d MMM yyyy , h:mm a", Locale.getDefault())
        if (position < item.size) {
            if (!item[position].startsWith("content:")) {
                val file = File(item[position])
                holder.nameTextView!!.text = file.name
                holder.infoTextView!!.text = convertIt(file.length()) + sdf.format(file.lastModified())

            } else {
                val documentFile: DocumentFile? = DocumentFile.fromSingleUri(mainActivity, Uri.parse(item[position]))
                documentFile!!.lastModified()
                holder.nameTextView!!.text = documentFile.name
                holder.infoTextView!!.text = convertIt(documentFile.length()) + sdf.format(documentFile.lastModified())
            }


            holder.coverImageView!!.setImageResource(R.drawable.pdf_icon)


            holder.moreOptionImageView!!.setOnClickListener { mainActivity.openMoreOption(position) }
            holder.nameTextView!!.setOnClickListener { mainActivity.openPdf(position) }
            holder.infoTextView!!.setOnClickListener { mainActivity.openPdf(position) }
            holder.coverImageView!!.setOnClickListener { mainActivity.openPdf(position) }
        } else {
            holder.scanSdCardButton?.setOnClickListener { mainActivity.sendSdCardPermission() }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return if (androidSdCardPermission.isSdStorageWritable) {
            item.size
        } else {
            item.size + 1
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == item.size && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) layout_scan_sdcard else layout_recycler_view
    }

    fun transferItem(value: ArrayList<String>) {
        item.clear()
        item.addAll(value)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return exampleFilter
    }

    private val exampleFilter: Filter = object : Filter() {
        override fun performFiltering(charSequence: CharSequence): FilterResults {
            var filterPattern = charSequence.toString().toLowerCase(Locale.getDefault()).trim { it <= ' ' }
            if (charSequence.isEmpty()) {
                filterPattern = ""
            }
            val results = FilterResults()
            results.values = filterPattern
            return results
        }

        override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
            val text = filterResults.values as String
            if (!(text == "" || database!!.isEmpty)) {
                item.clear()
                updateList(text)
            }
            notifyDataSetChanged()
        }
    }

    private fun updateList(query: String) {
        val cursor = database?.getValue(query)
        if (cursor!!.moveToFirst()) {
            do {
                item.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    init {
        database = Database(mainActivity.applicationContext)
        this.mainActivity = mainActivity
        androidSdCardPermission = AndroidSdCardPermission(mainActivity.applicationContext)
        this.item = item
    }
}