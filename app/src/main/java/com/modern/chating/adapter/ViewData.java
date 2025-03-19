package com.modern.chating.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.modern.chating.R;
import com.modern.chating.file.AudioPlayer;
import com.modern.chating.file.FileExecute;
import com.modern.chating.file.FileNameExtractor;
import com.modern.chating.filter.FilterChat;
import com.modern.chating.modal.Chat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wtc.material.CardFileCustom;
import wtc.material.ExoPlayerCustom;
import wtc.material.image.ImageWtcView;

public class ViewData {
    public static void chat(ChatAdapter.ViewHolder viewHolder, ArrayList<Chat> chats, String email, String email2, int position, boolean isChat) {
        Chat data = chats.get(position);
        String filterChat = filterChat(data.message);

        Log.d("ChatAdapter", "filterChat = "+filterChat);
        Log.d("ChatAdapter", "isChat = "+isChat);
        Log.d("ChatAdapter", "email = "+email);
        Log.d("ChatAdapter", "email2 = "+email2);

        // Cek apakah pesan sebelumnya dari pengirim yang sama
        boolean hideEkorPengirim = false, hideEkorPenerima = false;
        if (position > 0) {
            Chat previousChat = chats.get(position - 1);
            hideEkorPengirim = previousChat.user.email.equals(email);  // Jika pesan sebelumnya dari pengirim yang sama
        }


        // Cek apakah pesan sebelumnya dari penerima yang sama
        if (position > 0) {
            Chat previousChat = chats.get(position - 1);
            hideEkorPenerima = previousChat.user.email.equals(email2);
        }

        if (isChat) {
            Log.d("ChatAdapter", "===========================================");
            Log.d("ChatAdapter", "masuk ke if");
            viewHolder.buble.removeAllViews();

            // LayoutParams untuk bubble chat
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT); // Biar bubble ada di kanan
            params.setMargins(0, 0, 20, 0);
            viewHolder.rl_chat.setLayoutParams(params);

            // LayoutParams untuk ekor chat
            RelativeLayout.LayoutParams ekorparams = new RelativeLayout.LayoutParams(90, 40);
            ekorparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            // Text pesan utama
            TextView text_chat = new TextView(viewHolder.buble.getContext());
            text_chat.setText(filterChat);
            text_chat.setTextColor(Color.WHITE);
            text_chat.setTextSize(15);
            text_chat.setGravity(Gravity.END);
            text_chat.setPadding(0,0,5,0);

            // Text jam
            TextView jam = new TextView(viewHolder.buble.getContext());
            jam.setText(data.time);
            jam.setTextColor(Color.WHITE);
            jam.setTextSize(11);
            jam.setGravity(Gravity.END);
            jam.setPadding(0,0,5,0);

            // Hilangkan ekor jika pesan sebelumnya dari pengirim yang sama
            if (hideEkorPengirim) {
                viewHolder.ekor.setVisibility(View.GONE);
                RelativeLayout.LayoutParams bubleparams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                bubleparams.setMargins(0, 14, 20, 0);
                viewHolder.buble.setLayoutParams(bubleparams);
            } else {
                viewHolder.ekor.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams bubleparams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                bubleparams.setMargins(0, 14, -40, 0);
                bubleparams.addRule(RelativeLayout.LEFT_OF, R.id.ekor);
                ekorparams.setMargins(0, 0, -33, 0);
                viewHolder.ekor.setLayoutParams(ekorparams);
                viewHolder.buble.setLayoutParams(bubleparams);
            }

            // Handle jika file berupa video
            if (data.file != null && data.file.fileUrl.endsWith(".mp4")) {
                ExoPlayerCustom player = new ExoPlayerCustom(viewHolder.buble.getContext());
                player.setVideo(data.file.fileUrl);
                LinearLayout.LayoutParams playerParams = new LinearLayout.LayoutParams(500, 500);
                player.setLayoutParams(playerParams);
                viewHolder.buble.addView(player);
            }
            // Handle jika ada gambar
            else if (data.images != null && !data.images.getImageUrl().isEmpty()) {
                ImageWtcView imageWtcView = new ImageWtcView(viewHolder.buble.getContext());
                imageWtcView.scaleType(ImageView.ScaleType.CENTER_CROP);
                imageWtcView.loadImageWithBlur(true, data.images.getImageUrl(), 10);
                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(500, 500);
                imageWtcView.setLayoutParams(imageParams);
                viewHolder.buble.addView(imageWtcView);
            }
            // Handle jika pesan adalah dokumen
            else if (data.message.equals("Mengirim file dokumen.")) {
                CardFileCustom cardFileCustom = new CardFileCustom(viewHolder.buble.getContext());
                cardFileCustom.fileName(FileNameExtractor.getFileNameFromUrl(data.file.fileUrl));
                cardFileCustom.formatFile(data.file.fileUrl);
                cardFileCustom.setSize(data.images.size.getFileSize());
                LinearLayout.LayoutParams fileParams = new LinearLayout.LayoutParams(500, 300);
                cardFileCustom.setLayoutParams(fileParams);
                viewHolder.buble.addView(cardFileCustom);
            }
            // Jika tidak ada media, tambahkan teks chat
            else {
                viewHolder.buble.addView(text_chat);
            }

            // Tambahkan jam setelah semua elemen agar tidak duplikat
            viewHolder.buble.addView(jam);
            Log.d("ChatAdapter", "===========================================");
        }else{
            Log.d("ChatAdapter", "===========================================");
            Log.d("ChatAdapter", "Else");
            Log.d("ChatAdapter", "email = "+data.user.email);
            Log.d("ChatAdapter", "email2 = "+email2);

            viewHolder.buble.removeAllViews();

            // LayoutParams untuk bubble chat
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 20, 0);
            viewHolder.rl_chat.setLayoutParams(params);

            // LayoutParams untuk ekor chat
            RelativeLayout.LayoutParams ekorparams = new RelativeLayout.LayoutParams(90, 40);

            // Text pesan utama
            TextView text_chat = new TextView(viewHolder.buble.getContext());
            text_chat.setText(filterChat);
            text_chat.setTextColor(Color.WHITE);
            text_chat.setTextSize(15);
            text_chat.setGravity(Gravity.END);
            text_chat.setPadding(0,0,5,0);

            // Text jam
            TextView jam = new TextView(viewHolder.buble.getContext());
            jam.setText(data.time);
            jam.setTextColor(Color.WHITE);
            jam.setTextSize(11);
            jam.setGravity(Gravity.END);
            jam.setPadding(0,0,5,0);

            Log.d("ChatAdapter", "hideEkorPenerima = "+hideEkorPenerima);

            // Hilangkan ekor jika pesan sebelumnya dari pengirim yang sama
            if (hideEkorPenerima) {
                viewHolder.ekor.setVisibility(View.GONE);
                RelativeLayout.LayoutParams bubleparams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                bubleparams.setMargins(30, 14, 0, 0);
                viewHolder.buble.setLayoutParams(bubleparams);
            } else {
                viewHolder.ekor.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams bubleparams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                bubleparams.setMargins(-20, 14, 0, 0);
                bubleparams.addRule(RelativeLayout.RIGHT_OF, R.id.ekor);
                ekorparams.setMargins(0, 0, -33, 0);
                viewHolder.ekor.setLayoutParams(ekorparams);
                viewHolder.buble.setLayoutParams(bubleparams);
            }

            // Handle jika file berupa video
            if (data.file != null && data.file.fileUrl.endsWith(".mp4")) {
                ExoPlayerCustom player = new ExoPlayerCustom(viewHolder.buble.getContext());
                player.setVideo(data.file.fileUrl);
                LinearLayout.LayoutParams playerParams = new LinearLayout.LayoutParams(500, 500);
                player.setLayoutParams(playerParams);
                viewHolder.buble.addView(player);
            }
            // Handle jika ada gambar
            else if (data.images != null && !data.images.getImageUrl().isEmpty()) {
                ImageWtcView imageWtcView = new ImageWtcView(viewHolder.buble.getContext());
                imageWtcView.scaleType(ImageView.ScaleType.CENTER_CROP);
                imageWtcView.loadImageWithBlur(true, data.images.getImageUrl(), 10);
                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(500, 500);
                imageWtcView.setLayoutParams(imageParams);
                viewHolder.buble.addView(imageWtcView);
            }
            // Handle jika pesan adalah dokumen
            else if (data.message.equals("Mengirim file dokumen.")) {
                CardFileCustom cardFileCustom = new CardFileCustom(viewHolder.buble.getContext());
                cardFileCustom.fileName(FileNameExtractor.getFileNameFromUrl(data.file.fileUrl));
                cardFileCustom.formatFile(data.file.fileUrl);
                cardFileCustom.setSize(data.images.size.getFileSize());
                LinearLayout.LayoutParams fileParams = new LinearLayout.LayoutParams(500, 300);
                cardFileCustom.setLayoutParams(fileParams);
                viewHolder.buble.addView(cardFileCustom);
            }
            // Jika tidak ada media, tambahkan teks chat
            else {
                viewHolder.buble.addView(text_chat);
            }

            // Tambahkan jam setelah semua elemen agar tidak duplikat
            viewHolder.buble.addView(jam);
            Log.d("ChatAdapter", "===========================================");
        }



    }




    private static void log(String message, String type){
        if(type.equals("e")){
            Log.e("ViewData", message);
        }else{
            Log.d("ViewData", message);
        }
    }

    private static String filterChat(String text) {
        if (text.startsWith("bs64_encode=")) {
            String originalMessage = text.replace("bs64_encode=", "");
            FilterChat filter = new FilterChat(originalMessage);
            return filter.getEncryptedMessage();
        }
        if (text.startsWith("bs64_decode=")) {
            String originalMessage = text.replace("bs64_decode=", "");
            FilterChat filter = new FilterChat(originalMessage);
            return filter.getDecryptedMessage();
        }

        return text;
    }

    private SpannableStringBuilder formatBoldText(String text) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(text);
        String pattern = "_(.*?)_"; // Pola untuk teks di antara underscore

        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(text);

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            // Ambil teks di antara underscore tanpa underscore itu sendiri
            String boldText = matcher.group(1);

            // Ganti teks yang termasuk underscore dengan teks tanpa underscore
            spannable.replace(start, end, boldText);

            // Set teks tersebut menjadi Bold
            spannable.setSpan(new StyleSpan(Typeface.BOLD), start, start + boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannable;
    }
}
