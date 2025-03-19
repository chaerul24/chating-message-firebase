package com.modern.chating.firebase.send;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.modern.chating.adapter.ChatAdapter;
import com.modern.chating.firebase.F2base;
import com.modern.chating.firebase.FirebaseExecute;
import com.modern.chating.modal.Chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Messages {
    Context context;
    String sender, receiver;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("messages");
    private Handler handler;
    private Runnable locationRunnable;
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private String category;

    public Messages(Context context) {
        this.context = context;
    }
    public void setCategory(String chatbot) {
        this.category = chatbot;
    }
    public void setSender(String userSender) {
        this.sender = userSender.replace(".", "_");
    }

    public void setReceiver(String userReceiver) {
        this.receiver = userReceiver.replace(".", "_");
    }

    public String getChatId(String sender, String receiver) {
        // Urutkan sender dan receiver agar ID selalu konsisten
        if (sender.compareTo(receiver) < 0) {
            return sender.replace(".", "_") + "_" + receiver.replace(".", "_");
        } else {
            return receiver.replace(".", "_") + "_" + sender.replace(".", "_");
        }
    }

    public void sendMessages(String messages) {
        getCheck(new F2base.OnCheckCallback() {
            @Override
            public void onResult(boolean isRegistered) {
                Log.d("Messages", "isRegistered: " + isRegistered);
                if (isRegistered) {
                    setMessages(messages);
                } else {
                    createNewMessages(messages);
                }
            }
        });
    }

    public void createNewMessages(String messages) {
        String chatId = getChatId(sender, receiver);

        if (messages.isEmpty()) {
            Log.d("Messages", "messages is empty");
            return;
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("id", System.currentTimeMillis() + "");
        data.put("message", messages);
        data.put("foto", "null");
        data.put("vn", "null");
        data.put("sender", sender.replace("_", "."));
        data.put("receive", receiver.replace("_", "."));
        data.put("latitude", 0.0);
        data.put("longitude", 0.0);
        data.put("width", 0);
        data.put("height", 0);
        data.put("file_size", "null");
        data.put("time", FirebaseExecute.Message.getJamMenit());
        data.put("file", "null");
        data.put("read", false);

        if(category.isEmpty()){
            databaseReference.child(chatId).push().setValue(data)
                    .addOnSuccessListener(unused -> Log.d("Messages", "Berhasil menambahkan message ke chat yang ada"));
        }else{
            databaseReference.child(category).push().setValue(data)
                    .addOnSuccessListener(unused -> Log.d("Messages", "Berhasil menambahkan message ke chat yang ada"));
        }
    }

    public void setMessagesImage(String imageUrl) {
        Log.d("Upload", "imageUrl: " + imageUrl);
        Log.d("Upload", "Sender: " + sender);
        Log.d("Upload", "Receiver: " + receiver);
        String chatId = getChatId(sender, receiver);

        if (imageUrl.isEmpty()) {
            Log.d("Messages", "messages is empty");
            return;
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("id", System.currentTimeMillis() + "");
        data.put("message", "Mengirim gambar.");
        data.put("foto", imageUrl);
        data.put("vn", "null");
        data.put("sender", sender.replace("_", "."));
        data.put("receive", receiver.replace("_", "."));
        data.put("latitude", 0.0);
        data.put("longitude", 0.0);
        data.put("time", FirebaseExecute.Message.getJamMenit());
        data.put("file", "null");
        data.put("read", false);

        if(category.isEmpty()){
            databaseReference.child(chatId).push().setValue(data)
                    .addOnSuccessListener(unused -> Log.d("Messages", "Berhasil menambahkan message ke chat yang ada"));
        }else{
            databaseReference.child(category).push().setValue(data)
                    .addOnSuccessListener(unused -> Log.d("Messages", "Berhasil menambahkan message ke chat yang ada"));
        }
    }

    public void setMessages(String messages) {
        String chatId = getChatId(sender, receiver);

        if (messages.isEmpty()) {
            Log.d("Messages", "messages is empty");
            return;
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("id", System.currentTimeMillis() + "");
        data.put("message", messages);
        data.put("foto", "null");
        data.put("vn", "null");
        data.put("sender", sender.replace("_", "."));
        data.put("receive", receiver.replace("_", "."));
        data.put("latitude", 0.0);
        data.put("longitude", 0.0);
        data.put("width", 0);
        data.put("height", 0);
        data.put("file_size", "null");
        data.put("time", FirebaseExecute.Message.getJamMenit());
        data.put("file", "null");
        data.put("read", false);

        if(category.isEmpty()){
            databaseReference.child(chatId).push().setValue(data)
                    .addOnSuccessListener(unused -> Log.d("Messages", "Berhasil menambahkan message ke chat yang ada"));
        }else{
            databaseReference.child(category).push().setValue(data)
                    .addOnSuccessListener(unused -> Log.d("Messages", "Berhasil menambahkan message ke chat yang ada"));
        }
    }



    public void stopLocationUpdates() {
        handler.removeCallbacks(locationRunnable);
    }



    public void getCheck(F2base.OnCheckCallback callback) {
        new F2base().isCheck(sender, receiver, isRegistered -> callback.onResult(isRegistered));
    }

    public void getMessageBottom(OnMessageBottom onMessageBottom) {
        String childId = getChatId(sender, receiver);
        Log.d("Messages", "Bottom> childId: " + childId);

        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("messages/" + childId);

        chatRef.orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String lastMessage = snapshot.child("message").getValue(String.class);
                        if (lastMessage != null) {
                            Log.d("LastMessage", "Pesan terakhir: " + lastMessage);
                            onMessageBottom.onMessageBottom(lastMessage);
                        } else {
                            onMessageBottom.onError("Pesan terakhir kosong");
                        }
                    }
                } else {
                    onMessageBottom.onError("Tidak ada pesan dalam chat ini");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                onMessageBottom.onError("Gagal mengambil data: " + databaseError.getMessage());
                Log.e("FirebaseError", "Gagal mengambil data: " + databaseError.getMessage());
            }
        });
    }

    public void updateMessageReadStatus(String sender, String receiver) {
        String chatId = getChatId(sender, receiver);
        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId);

        messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    String messageReceiver = messageSnapshot.child("receive").getValue(String.class);
                    Boolean isRead = messageSnapshot.child("read").getValue(Boolean.class);

                    // Hanya update jika penerima adalah user yang sedang login dan pesan belum dibaca
                    if (messageReceiver != null && messageReceiver.equals(sender) && (isRead == null || !isRead)) {
                        messageSnapshot.getRef().child("read").setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error updating read status: " + error.getMessage());
            }
        });
    }

    public void markMessagesAsRead(String sender, String receiver) {
        String childId = getChatId(sender, receiver);
        Log.d("Messages", "Marking messages as read for chatId: " + childId);

        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("messages").child(childId);

        messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    String penerima = messageSnapshot.child("receive").getValue(String.class);
                    Boolean isRead = messageSnapshot.child("read").getValue(Boolean.class);

                    // Jika pesan diterima oleh user yang sedang membuka chat dan belum dibaca
                    if (penerima != null && penerima.equals(receiver) && (isRead == null || !isRead)) {
                        messageSnapshot.getRef().child("read").setValue(true);
                        Log.d("Messages", "Pesan ditandai sebagai dibaca: " + messageSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Gagal memperbarui pesan: " + error.getMessage());
            }
        });
    }


    public void isCheckReceiveReadMessages(String sender, String receiver, F2base.OnCheckCallback callback) {
        String childId = getChatId(sender, receiver);
        Log.d("Messages", "isCheckReceiveReadMessages > childId: " + childId);

        DatabaseReference mdatabase = FirebaseDatabase.getInstance().getReference("messages").child(childId);
        mdatabase.orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String penerima = snapshot.child("receive").getValue(String.class);
                        Boolean dibaca = snapshot.child("read").getValue(Boolean.class);

                        if (penerima != null && penerima.equals(receiver)) {
                            boolean isRead = dibaca != null && dibaca;
                            callback.onResult(isRead); // Callback ke UI
                        }
                    }
                } else {
                    Log.e("LastMessage", "Pesan terakhir kosong");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Gagal mengambil data: " + databaseError.getMessage());
            }
        });
    }

    public void updateRead(String sender, String receiver) {
        String childId = getChatId(sender, receiver);
        Log.d("Messages", "Update > childId: " + childId);

        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("messages")
                .child(childId); // Chat ID

        messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean updated = false; // Flag untuk mengecek apakah ada perubahan

                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    String messageReceiver = messageSnapshot.child("receive").getValue(String.class);
                    Boolean isRead = messageSnapshot.child("read").getValue(Boolean.class);

                    // Cek apakah pesan untuk user saat ini dan belum dibaca
                    if (messageReceiver != null && messageReceiver.equals(receiver) && (isRead == null || !isRead)) {
                        // Update read jadi true
                        messageSnapshot.getRef().child("read").setValue(true);
                        updated = true; // Set flag menjadi true
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error: " + error.getMessage());
            }
        });
    }



    public void isRead(F2base.OnCheckCallback callback){
        String childId = getChatId(sender, receiver);
        Log.d("Messages", "Is Read > childId: " + childId);

        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("messages/" + childId);

        chatRef.orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        boolean read = snapshot.child("read").getValue(Boolean.class);
                        if (read == true) {
                            Log.d("LastMessage", "Is Read >  " + read);
                            callback.onResult(true);
                        } else {
                            Log.d("LastMessage", "Is Read >  " + read);
                            callback.onResult(false);
                        }

                    }
                } else {
                    Log.e("LastMessage", "Pesan terakhir kosong");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onResult(false);
                Log.e("FirebaseError", "Gagal mengambil data: " + databaseError.getMessage());
            }
        });
    }



    public interface OnMessageBottom {
        void onMessageBottom(String lastMessage);
        void onError(String errorMessage);
    }
}