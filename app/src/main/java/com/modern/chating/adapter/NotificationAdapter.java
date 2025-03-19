package com.modern.chating.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.modern.chating.R;
import com.modern.chating.modal.Notification;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationViewHolder> {

    private ArrayList<Notification> array;
    private Context context;

    public NotificationAdapter(Context context, ArrayList<Notification> array) {
        this.array = array;
        this.context = context;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view;
        if (viewType == 1) { // "friends" category
            view = LayoutInflater.from(context).inflate(R.layout.item_notification_confirm, viewGroup, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_notification, viewGroup, false);
        }
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = array.get(position);
        holder.setCategory(notification.getCategory());

        if ("friends".equals(notification.getCategory())) {
            TextView title = holder.itemView.findViewById(R.id.text_notification_friends);
            TextView message = holder.itemView.findViewById(R.id.text_notification_message_friends);
            AppCompatButton btnConfirm = holder.itemView.findViewById(R.id.btn_confir);
            AppCompatButton btnCancel = holder.itemView.findViewById(R.id.btn_cancel);
            AppCompatButton time = holder.itemView.findViewById(R.id.btn_time);
            ImageView imageView = holder.itemView.findViewById(R.id.icon_friends);

            if (title != null && message != null && btnConfirm != null && btnCancel != null && time != null && imageView != null) {
                title.setText(notification.getTitle());
                message.setText(notification.getMessage());
                time.setText(notification.getTime());

                btnConfirm.setOnClickListener(view -> {
                    // Handle confirm button click
                });

                btnCancel.setOnClickListener(view -> {
                    // Handle cancel button click
                });

                Glide.with(context)
                        .load(notification.getAvatar())
                        .circleCrop()
                        .into(imageView);
            } else {
                Log.d("NotificationAdapter", "Friends layout views not found");
            }
        } else { // "info" category
            TextView title = holder.itemView.findViewById(R.id.text_notification);
            TextView message = holder.itemView.findViewById(R.id.text_notification_message);
            TextView time = holder.itemView.findViewById(R.id.text_notification_time);

            if (title != null && message != null && time != null) {
                title.setText(notification.getTitle());
                message.setText(notification.getMessage());
                time.setText(notification.getTime());
            } else {
                Log.d("NotificationAdapter", "Info layout views not found");
            }
        }
    }

    @Override
    public int getItemCount() {
        return array.size();
    }

    @Override
    public int getItemViewType(int position) {
        return "friends".equals(array.get(position).getCategory()) ? 1 : 0;
    }
}
