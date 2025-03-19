package com.modern.chating.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.modern.chating.R;
import com.modern.chating.firebase.F2base;
import com.modern.chating.firebase.send.Messages;
import com.modern.chating.firebase.user.User;
import com.modern.chating.modal.ModalArray;

import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<ModalArray> array;
    private final OnItem onItem;
    private final String user, email;
    private final Messages messages;
    private final User users;

    // Constructor
    public ItemAdapter(Context context, String user, String email, ArrayList<ModalArray> array, OnItem onItem) {
        this.context = context;
        this.user = user;
        this.array = array;
        this.email = email;
        this.onItem = onItem;
        messages = new Messages(context);
        users = new User(context);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_list, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        ModalArray data = array.get(position);

        // Set profile image with Glide
        if (data.urlImage != null && !data.urlImage.isEmpty()) {
            Glide.with(context)
                    .load(data.urlImage)
                    .circleCrop()
                    .into(viewHolder.profile);
        }

        System.out.println("Title: " + data.title);
        System.out.println("User: " + user);
        System.out.println("sender: " + email);
        System.out.println("receiver: " + data.message);

        if (data.title.equals(user)) {
            new android.os.Handler().post(() -> removeItem(position)); // Hapus dengan aman setelah layout selesai
            return; // Stop eksekusi lebih lanjut
        }
        messages.setReceiver(data.message);
        messages.setSender(email);
        messages.isRead(new F2base.OnCheckCallback() {
            @Override
            public void onResult(boolean isRegistered) {
                if(isRegistered){
                    viewHolder.icon.setImageResource(R.drawable.baseline_check_circle_24);
                }else{
                    viewHolder.icon.setImageResource(R.drawable.baseline_check_circle_outline_24);
                }
            }
        });
        messages.getMessageBottom(new Messages.OnMessageBottom() {
            @Override
            public void onMessageBottom(String lastMessage) {
                viewHolder.message.setText(potong(lastMessage, 20));
                Log.d("ItemAdapter", "Last Message: " + lastMessage);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("ItemAdapter", "Error getting last message: " + errorMessage);
            }
        });

        users.isStatus(data.message, new F2base.OnCheckCallback() {
            @Override
            public void onResult(boolean isRegistered) {
                if(isRegistered){
                    viewHolder.ll_status.setBackgroundResource(R.drawable.radius_online);
                }else{
                    viewHolder.ll_status.setBackgroundResource(R.drawable.radius_offline);
                }
            }
        });

        viewHolder.title.setText(data.title);


        // Handle item click
        viewHolder.itemView.setOnClickListener(view -> onItem.onItemClick(position, data));
    }


    public void removeItem(int position) {
        if (position >= 0 && position < array.size()) {
            array.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, array.size()); // Pastikan RecyclerView tetap rapi
        }
    }



    @Override
    public int getItemCount() {
        return array.size(); // Return the filtered list size
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView profile, icon;
        private final TextView title, message;
        LinearLayout ll_status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profile = itemView.findViewById(R.id.picture_profile_image);
            title = itemView.findViewById(R.id.text_title);
            message = itemView.findViewById(R.id.text_message);
            icon = itemView.findViewById(R.id.image_icon);
            ll_status = itemView.findViewById(R.id.ll_status);
        }
    }

    // Interface to handle item click
    public interface OnItem {
        void onItemClick(int position, ModalArray data);
    }

    public String potong(String text, int maxLength) {
        if (text == null || text.isEmpty()) {
            return ""; // Kalau teks kosong/null, langsung return kosong
        }
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

}
