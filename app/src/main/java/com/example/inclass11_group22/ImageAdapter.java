package com.example.inclass11_group22;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    List<Bitmap> bitmapList;
    private final OnItemLongClickListener listener;

    public ImageAdapter(List<Bitmap> bitmapList, OnItemLongClickListener listener) {
        this.bitmapList = bitmapList;
        this.listener = listener;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Bitmap item);
    }

    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_image, parent, false);
        ViewHolder viewHolder = new ViewHolder(convertView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder holder, int position) {
        holder.bind(bitmapList.get(position),listener);
        Bitmap bitmap = bitmapList.get(position);
        holder.iv_image.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView iv_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_image = itemView.findViewById(R.id.iv_image);
        }

        public void bind(final Bitmap bitmap, final OnItemLongClickListener listener) {
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    listener.onItemLongClick(bitmap);
                    return true;
                }
            });
        }
    }
}
