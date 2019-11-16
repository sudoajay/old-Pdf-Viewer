package com.sudoajay.pdf_viewer.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sudoajay.pdf_viewer.R;

import java.io.File;
import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private ArrayList<String> item;
    private static final String TAG = "GotSomething";

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView nameTextView, pathTextView;
        ImageView coverImageView;

        MyViewHolder(View v) {
            super(v);
            nameTextView = v.findViewById(R.id.nameTextView);
            pathTextView = v.findViewById(R.id.pathTextView);
            coverImageView = v.findViewById(R.id.coverImageView);

        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(final ArrayList<String> item) {
        this.item = item;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.layout_recycler_view, parent, false);

        return new MyViewHolder(listItem);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        File file = new File(item.get(position));

        holder.nameTextView.setText(file.getName());
        holder.pathTextView.setText(file.getAbsolutePath());
        holder.coverImageView.setImageResource(R.drawable.pdf_icon);


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return item.size();
    }
}