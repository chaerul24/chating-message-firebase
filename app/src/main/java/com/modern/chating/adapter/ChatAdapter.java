package com.modern.chating.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.modern.chating.BuildConfig;
import com.modern.chating.ChatActivity;
import com.modern.chating.R;
import com.modern.chating.ViewImageActivity;
import com.modern.chating.file.AudioPlayer;
import com.modern.chating.file.FileExecute;
import com.modern.chating.file.FileNameExtractor;
import com.modern.chating.file.FileSizeTask;
import com.modern.chating.file.Image;
import com.modern.chating.filter.FilterChat;
import com.modern.chating.modal.Chat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.wasabeef.glide.transformations.BlurTransformation;
import wtc.material.CardFileCustom;
import wtc.material.ExoPlayerCustom;
import wtc.material.image.ImageWtcView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    public static Context context;
    ArrayList<Chat> chats;
    String email;
    OnLongClickListener listener;
    public ChatAdapter(Context context, String email, ArrayList<Chat> array, OnLongClickListener listener) {
        this.email= email;
        this.context = context;
        this.chats = array;
        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chating, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Chat data = chats.get(i);
        boolean check = isCheck(email, data.user.email);
        Log.d("ChatAdapter", "isCheck = "+check);
        Log.d("ChatAdapter", "Email 1 = "+email);
        Log.d("ChatAdapter", "Email 2 = "+data.user.email);
        ViewData.chat(viewHolder, chats, email, data.user.email,i , check);
        viewHolder.rl_chat.setOnLongClickListener(view -> {
            listener.onLongClick(data, view, viewHolder.rl_bg);
            return false;
        });
    }

    public interface OnLongClickListener {
        void onLongClick(Chat chat, View view, RelativeLayout rl_bg);
    }
    private void showPopup(View anchorView) {
        // Inflate layout popup
        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_window, null);

        // Buat PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);


        // Tampilkan di posisi tertentu (bisa diganti)
        popupWindow.showAsDropDown(anchorView, 0, -anchorView.getHeight());
    }



    private boolean isCheck(String email1, String email2){
        if(email1.equals(email2)){
            return true;
        }else{
            return false;
        }
    }



    @Override
    public int getItemCount() {
        return chats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        final RelativeLayout rl_chat, rl_bg;
        final View ekor;
        final LinearLayout buble;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rl_bg = itemView.findViewById(R.id.rl_all);
            rl_chat = itemView.findViewById(R.id.rl_chat);
            ekor = itemView.findViewById(R.id.ekor);
            buble = itemView.findViewById(R.id.buble);
        }
    }

}
