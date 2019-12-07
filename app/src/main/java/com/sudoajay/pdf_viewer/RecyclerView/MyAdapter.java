package com.sudoajay.pdf_viewer.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sudoajay.pdf_viewer.Database_Classes.Database;
import com.sudoajay.pdf_viewer.HelperClass.FileSize;
import com.sudoajay.pdf_viewer.MainActivity;
import com.sudoajay.pdf_viewer.Permission.AndroidSdCardPermission;
import com.sudoajay.pdf_viewer.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> implements Filterable, View.OnClickListener {
    private ArrayList<String> item;
    private static final String TAG = "GotSomething";
    private Database database;
    private AndroidSdCardPermission androidSdCardPermission;
    private MainActivity mainActivity;
    private String currentPath;



    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView nameTextView, infoTextView;
        ImageView coverImageView, moreOption_imageView;
        Button scanSdCard_Button;

        MyViewHolder(View v) {
            super(v);
            nameTextView = v.findViewById(R.id.nameTextView);
            infoTextView = v.findViewById(R.id.infoTextView);
            coverImageView = v.findViewById(R.id.coverImageView);
            moreOption_imageView = v.findViewById(R.id.moreOption_imageView);
            scanSdCard_Button = v.findViewById(R.id.scanSdCard_Button);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(final Context context, final MainActivity mainActivity, final ArrayList<String> item) {
        database = new Database(context);
        this.mainActivity = mainActivity;
        androidSdCardPermission = new AndroidSdCardPermission(context);
        this.item = item;

    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        View listItem;

        if (viewType == R.layout.layout_recycler_view) {
            listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_recycler_view, parent, false);
        } else {
            listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_scan_sdcard, parent, false);
        }

        return new MyViewHolder(listItem);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if (position < item.size()) {
            File file = new File(item.get(position));

            holder.nameTextView.setText(file.getName());

            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(" , d MMM yyyy , h:mm a");


            holder.infoTextView.setText(FileSize.Convert_It(file.length()) + sdf.format(file.lastModified()));

            holder.coverImageView.setImageResource(R.drawable.pdf_icon);


            currentPath = item.get(position);

            holder.moreOption_imageView.setOnClickListener(this);
            holder.nameTextView.setOnClickListener(this);
            holder.infoTextView.setOnClickListener(this);
            holder.coverImageView.setOnClickListener(this);

        } else {
            holder.scanSdCard_Button.setOnClickListener(this);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (androidSdCardPermission.isSdStorageWritable() || (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
            return item.size();
        } else {
            return item.size() + 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == item.size() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ? R.layout.layout_scan_sdcard : R.layout.layout_recycler_view;
    }

    public void transferItem(final ArrayList<String> value) {
        item.clear();
        item.addAll(value);
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.infoTextView:
            case R.id.nameTextView:
            case R.id.coverImageView:
                mainActivity.openPdf(currentPath);
                break;
            case R.id.moreOption_imageView:
                mainActivity.openMoreOption(currentPath);
                break;
            case R.id.scanSdCard_Button:
                mainActivity.sendSdCardPermission();
                break;
        }

    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            String filterPattern = charSequence.toString().toLowerCase().trim();
            if (charSequence.length() == 0) {
                filterPattern = "";
            }

            FilterResults results = new FilterResults();
            results.values = filterPattern;

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            String text = (String) filterResults.values;
            if (!text.equals("") && !database.isEmpty()) {
                item.clear();
                updateList(text);
            }
            notifyDataSetChanged();
        }
    };

    private void updateList(final String query) {
        Cursor cursor = database.getValue(query);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                item.add(cursor.getString(0));

            } while (cursor.moveToNext());
        }
    }

    public ArrayList<String> getItem() {
        return item;
    }

    public void setItem(ArrayList<String> item) {
        this.item = item;
    }
}