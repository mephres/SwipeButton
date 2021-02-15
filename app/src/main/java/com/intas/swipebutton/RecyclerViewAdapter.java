package com.intas.swipebutton;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>  {

    Context context;
    List<String> data;

    public RecyclerViewAdapter(Context context, List<String> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.itemNameTextView.setText(data.get(position));
        holder.itemNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String item = data.get(position);
                Toast.makeText(context, "You clicked " + item, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void removeItem(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(String item, int position) {
        data.add(position, item);
        notifyItemInserted(position);
    }

    public List<String> getData() {
        return data;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        View container;
        TextView itemNameTextView;

        public ViewHolder(View view) {
            super(view);
            container = view;
            itemNameTextView = view.findViewById(R.id.itemNameTextView);
        }

    }
}
