package com.modern.chating.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.modern.chating.R;
import com.modern.chating.modal.Status;

import java.util.ArrayList;

import wtc.material.image.ImageWtcView;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.ViewHolder> {

    Context context;
    ArrayList<Status> list;

    public StatusAdapter(Context context, ArrayList<Status> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(View.inflate(context, R.layout.item_status, null));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Status status = list.get(i);
        viewHolder.imageView.scaleType(ImageView.ScaleType.CENTER_CROP);
        viewHolder.imageView.loadImageWithBlur(true, status.fileUrl, 10);
        if(status.isView){
            viewHolder.avatar.setBackgroundResource(R.drawable.border_status_true);
        }else{
            viewHolder.avatar.setBackgroundResource(R.drawable.border_status);
        }
        Glide.with(context)
                .load(status.avatar)
                .circleCrop()
                .into(viewHolder.avatar);
        viewHolder.user.setText(status.user);
        viewHolder.user.setTextColor(Color.WHITE);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageWtcView imageView;
        private final ImageView avatar;
        private final TextView user;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            
            imageView = itemView.findViewById(R.id.image_wtc);
            avatar = itemView.findViewById(R.id.image_profile);
            user = itemView.findViewById(R.id.text_user);
            
        }
    }
}
