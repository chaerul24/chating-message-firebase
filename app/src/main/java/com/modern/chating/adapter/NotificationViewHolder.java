package com.modern.chating.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.modern.chating.R;
import com.modern.chating.modal.Notification;

public class NotificationViewHolder extends RecyclerView.ViewHolder {

    View itemView;
    public NotificationViewHolder(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
    }

    public void setCategory(String category){
        if ("friends".equals(category)) {
            itemView.findViewById(R.id.icon_friends).setVisibility(View.VISIBLE);
            itemView.findViewById(R.id.text_notification_friends).setVisibility(View.VISIBLE);
            itemView.findViewById(R.id.text_notification_message_friends).setVisibility(View.VISIBLE);
            itemView.findViewById(R.id.btn_confir).setVisibility(View.VISIBLE);
            itemView.findViewById(R.id.btn_cancel).setVisibility(View.VISIBLE);
            itemView.findViewById(R.id.btn_time).setVisibility(View.VISIBLE);
        }else{
            itemView.findViewById(R.id.text_notification);
            itemView.findViewById(R.id.text_message);
        }

    }
}
