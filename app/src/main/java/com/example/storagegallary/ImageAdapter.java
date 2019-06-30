package com.example.storagegallary;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageHolder> {
    private List<Image> images;
    private Context context;
    private OnClickListener onClickListener;

    public ImageAdapter(List<Image> images, Context context, OnClickListener onClickListener) {
        this.images = images;
        this.context = context;
        this.onClickListener = onClickListener;
    }

    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false);
        return new ImageHolder(row);
    }

    @Override
    public void onBindViewHolder(final ImageHolder holder, int position) {
        Image image = images.get(position);
        Picasso.get().load(image.getImageLink()).into(holder.imageThumbnail);
        holder.tvTime.setText(image.getFormattedTime());

        holder.imageThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null)
                    onClickListener.onClick(/* do NOT PASS position from the Params because it will be final! ,use get adapter position instead*/holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    class ImageHolder extends RecyclerView.ViewHolder {
        private ImageView imageThumbnail;
        private TextView tvTime;


        public ImageHolder(View itemView) {
            super(itemView);
            imageThumbnail = itemView.findViewById(R.id.image_thumbnail);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }

    public interface OnClickListener {
        void onClick(int index);
    }
}