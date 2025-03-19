package com.modern.chating.firebase;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.*;
import com.modern.chating.adapter.ChatAdapter;
import com.modern.chating.file.FileExecute;
import com.modern.chating.modal.Chat;

import java.util.ArrayList;

public class F2base {


    public void isCheck(String userSender, String userReceiver, OnCheckCallback callback) {
        Log.d("getCheckData", "================== IS CHECK ==================");
        Log.d("getCheckData", "userSender: " + userSender);
        Log.d("getCheckData", "userReceiver: " + userReceiver);
        String query = userReceiver.replace(".", "_") + "_" + userSender.replace(".", "_");
        Log.d("getCheckData", "query: " + query);
        if (callback == null) {
            Log.e("getCheckData", "Callback is null, cannot proceed.");
            return;
        }

        if (userReceiver.isEmpty()) {
            Log.e("getCheckData", "userReceiver is empty");
            callback.onResult(false);
            return; // Hentikan eksekusi
        }

        if (userSender.isEmpty()) {
            Log.e("getCheckData", "userSender is empty");
            callback.onResult(false);
            return; // Hentikan eksekusi
        }

        // Format query

        // Debug log
        System.out.println("messages/" + query);
        System.out.println("==============================================");

        // Referensi ke database
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("messages").child(query);

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean exists = dataSnapshot.exists();
                Log.d("getCheckData", exists ? "Data ditemukan "+query : "Data tidak ditemukan");
                callback.onResult(exists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("getCheckData", "Error getting data", databaseError.toException());
                callback.onResult(false);
            }
        });
    }

    public void isCheckChatbot(String userReceiver, OnCheckCallback callback) {
        Log.d("FrgamentActivity", "================== IS CHECK ==================");
        Log.d("FrgamentActivity", "Chatbot: " + userReceiver);
        String query = userReceiver.replace(".", "_");
        Log.d("FrgamentActivity", "query: " + query);
        if (callback == null) {
            Log.e("FrgamentActivity", "Callback is null, cannot proceed.");
            return;
        }

        if (userReceiver.isEmpty()) {
            Log.e("FrgamentActivity", "userReceiver is empty");
            callback.onResult(false);
            return; // Hentikan eksekusi
        }

        // Format query

        // Debug log
        System.out.println("messages/" + query);
        System.out.println("==============================================");

        // Referensi ke database
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("messages").child(query);

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean exists = dataSnapshot.exists();
                Log.d("FrgamentActivity", exists ? "Data ditemukan "+query : "Data tidak ditemukan");
                callback.onResult(exists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FrgamentActivity", "Error getting data", databaseError.toException());
                callback.onResult(false);
            }
        });
    }

    public interface OnCheckCallback {
        void onResult(boolean isRegistered);
    }



}
