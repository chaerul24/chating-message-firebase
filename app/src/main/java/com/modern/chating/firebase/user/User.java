package com.modern.chating.firebase.user;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.modern.chating.firebase.F2base;
import com.modern.chating.firebase.FirebaseExecute;

public class User {
    Context context;
    public User(){}
    public User(Context context){
        this.context = context;
    }

    public void isStatus(String email, F2base.OnCheckCallback callback){
        DatabaseReference friendsRef = FirebaseDatabase.getInstance()
                .getReference("users/" + email.replace(".", "_") + "/status");

        friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    boolean isOnline = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                    callback.onResult(isOnline);
                } else {
                    Log.e("FriendsList", "Status tidak ditemukan untuk " + email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FriendsList", "Error: " + error.getMessage());
            }
        });
    }

    public void lastOnline(String email) {
        String emailKey = email.replace(".", "_"); // Ubah email jadi key yang valid di Firebase
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users/" + emailKey + "/lastOnline");
        DatabaseReference userStatusRef = FirebaseDatabase.getInstance().getReference("users/" + emailKey + "/status");

        // Ambil timestamp saat ini
        long timestamp = System.currentTimeMillis();

        // Set user online
        userStatusRef.setValue(true);

        // Jika disconnect, set offline & simpan waktu terakhir online
        userStatusRef.onDisconnect().setValue(false);
        userRef.onDisconnect().setValue(System.currentTimeMillis());
    }

    public void getName(String email, OnStatus onStatus) {
        if (email == null || email.isEmpty()) { // ✅ Pastikan email tidak null/empty
            String errorMessage = "Email tidak boleh kosong atau null";
            onStatus.onError(errorMessage);
            Log.e("FriendsList", errorMessage);
            return;
        }

        String emailKey = email.replace(".", "_");
        Log.d("Percakapan", "Mengambil nama untuk: " + emailKey);

        DatabaseReference friendsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(emailKey)
                .child("name");

        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) { // ✅ Cek apakah ada dan tidak null
                    String name = snapshot.getValue(String.class);
                    onStatus.onSuccess(name);
                } else {
                    String errorMessage = "Nama tidak ditemukan untuk " + email;
                    onStatus.onError(errorMessage);
                    Log.e("FriendsList", errorMessage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                String errorMessage = "Error: " + error.getMessage();
                onStatus.onError(errorMessage);
                Log.e("FriendsList", errorMessage);
            }
        });
    }


    public void getNameChatBot(String email, String key, OnStatus onStatus) {
        String emailKey = email.replace(".", "_");
        Log.d("Percakapan", emailKey);

        DatabaseReference friendsRef = FirebaseDatabase.getInstance()
                .getReference("users").child("chatbot").child(emailKey).child(key).child("name");

        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.getValue(String.class); // ✅ Langsung ambil nilai tanpa .child("name")
                    onStatus.onSuccess(name);
                } else {
                    String errorMessage = "Nama tidak ditemukan untuk " + email;
                    onStatus.onError(errorMessage);
                    Log.e("FriendsList", errorMessage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                String errorMessage = "Error: " + error.getMessage();
                onStatus.onError(errorMessage);
                Log.e("FriendsList", errorMessage);
            }
        });
    }

    public interface OnStatus {
        void onSuccess(String message);
        void onError(String errorMessage);
    }


}
