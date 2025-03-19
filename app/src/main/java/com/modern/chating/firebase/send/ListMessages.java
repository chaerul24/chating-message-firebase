package com.modern.chating.firebase.send;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.modern.chating.BuildConfig;
import com.modern.chating.adapter.ChatAdapter;
import com.modern.chating.file.FileExecute;
import com.modern.chating.firebase.FirebaseExecute;
import com.modern.chating.modal.Chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ListMessages {
    private ArrayList<Chat> chats;
    private ChatAdapter adapter;
    private Context context;
    private ValueEventListener messageListener;
    private DatabaseReference databaseReference;

    public ListMessages(Context context, ArrayList<Chat> chats, ChatAdapter adapter) {
        this.context = context;
        this.chats = chats;
        this.adapter = adapter;
    }

    public void setData(String name, String avatar, String userSender, String userReceiver) {
        if (userReceiver.isEmpty() || userSender.isEmpty()) {
            Log.d("getCheckData", "userReceiver or userSender is empty");
            return;
        }

        // Pastikan ID chat tetap konsisten dengan mengurutkan sender & receiver
        String chatId = userSender.compareTo(userReceiver) < 0 ?
                userSender.replace(".", "_") + "_" + userReceiver.replace(".", "_") :
                userReceiver.replace(".", "_") + "_" + userSender.replace(".", "_");

        Log.d("ChatID", "Chat ID: " + chatId);

        if (databaseReference != null && messageListener != null) {
            databaseReference.removeEventListener(messageListener);
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("messages/" + chatId);

        messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ArrayList<Chat> newChats = new ArrayList<>();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String id = FirebaseExecute.Message.replaceNullWithEmpty(snapshot.child("id").getValue(String.class));
                        if (id.isEmpty()) {
                            id = "0"; // Atur ID default untuk mencegah null
                        }
                        String message = FirebaseExecute.Message.replaceNullWithEmpty(snapshot.child("message").getValue(String.class));
                        String file = FirebaseExecute.Message.replaceNullWithEmpty(snapshot.child("file").getValue(String.class));
                        String foto = FirebaseExecute.Message.replaceNullWithEmpty(snapshot.child("foto").getValue(String.class));
                        String vn = FirebaseExecute.Message.replaceNullWithEmpty(snapshot.child("vn").getValue(String.class));
                        String email = FirebaseExecute.Message.replaceNullWithEmpty(snapshot.child("receive").getValue(String.class));
                        String time = FirebaseExecute.Message.replaceNullWithEmpty(snapshot.child("time").getValue(String.class));
                        String fileSize = FirebaseExecute.Message.replaceNullWithEmpty(snapshot.child("file_size").getValue(String.class));
                        Integer widthValue = snapshot.child("width").getValue(Integer.class);
                        Integer heightValue = snapshot.child("height").getValue(Integer.class);

                        int width = (widthValue != null) ? widthValue : 0;
                        int height = (heightValue != null) ? heightValue : 0;

                        newChats.add(new Chat(
                                id,
                                message,
                                new Chat.Map(0, 0),
                                new Chat.Images(foto, "success",new Chat.Size(fileSize, width, height) , 10, 0),
                                vn,
                                time,
                                new Chat.File(file, "", ""),
                                new Chat.User(id, name, email, avatar, true)
                        ));
                    }

                    // Urutkan pesan berdasarkan timestamp ID
                    Collections.sort(newChats, Comparator.comparing(Chat::getId));

                    // Gunakan DiffUtil atau cara lain untuk memperbarui hanya jika ada perubahan
                    if (!chats.equals(newChats)) {
                        chats.clear();
                        chats.addAll(newChats);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Log.d("getCheckData", "setData Data tidak ditemukan");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("getCheckData", "Error getting data", databaseError.toException());
            }
        };

        databaseReference.addValueEventListener(messageListener);
    }

    public void setDataChatbot(String name, String avatar, String email, String userSender, String userReceiver) {
        if (userReceiver.isEmpty() || userSender.isEmpty()) {
            Log.d("getCheckData", "userReceiver or userSender is empty");
            return;
        }

        Log.d("ChatID", "Chat ID: " + email);

        if (databaseReference != null && messageListener != null) {
            databaseReference.removeEventListener(messageListener);
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("messages/"+email);

        messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ArrayList<Chat> newChats = new ArrayList<>();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Log.d("FrgamentActivity", "id "+snapshot.child("id").getValue(String.class));
                        Log.d("FrgamentActivity", "message "+snapshot.child("message").getValue(String.class));
                        Log.d("FrgamentActivity", "file "+snapshot.child("file").getValue(String.class));
                        Log.d("FrgamentActivity", "foto "+snapshot.child("foto").getValue(String.class));
                        Log.d("FrgamentActivity", "vn "+snapshot.child("vn").getValue(String.class));
                        Log.d("FrgamentActivity", "receive "+snapshot.child("receive").getValue(String.class));
                        Log.d("FrgamentActivity", "time "+snapshot.child("time").getValue(String.class));
                        Log.d("FrgamentActivity", "file_size "+snapshot.child("file_size").getValue(String.class));
                        Log.d("FrgamentActivity", "width "+snapshot.child("width").getValue(Integer.class));
                        Log.d("FrgamentActivity", "height "+snapshot.child("height").getValue(Integer.class));
                        String id = FirebaseExecute.Message.replaceNullWithEmpty(snapshot.child("id").getValue(String.class));
                        String message = FirebaseExecute.Message.replaceNullWithEmpty(snapshot.child("message").getValue(String.class));
                        String file = FirebaseExecute.Message.replaceNullWithEmpty(snapshot.child("file").getValue(String.class));
                        String foto = FirebaseExecute.Message.replaceNullWithEmpty(snapshot.child("foto").getValue(String.class));
                        String vn = FirebaseExecute.Message.replaceNullWithEmpty(snapshot.child("vn").getValue(String.class));
                        String email = FirebaseExecute.Message.replaceNullWithEmpty(snapshot.child("receive").getValue(String.class));
                        String time = FirebaseExecute.Message.replaceNullWithEmpty(snapshot.child("time").getValue(String.class));
                        String fileSize = FirebaseExecute.Message.replaceNullWithEmpty(snapshot.child("file_size").getValue(String.class));
                        Integer widthValue = snapshot.child("width").getValue(Integer.class);
                        Integer heightValue = snapshot.child("height").getValue(Integer.class);

                        int width = (widthValue != null) ? widthValue : 0;
                        int height = (heightValue != null) ? heightValue : 0;

                        newChats.add(new Chat(
                                id,
                                message,
                                new Chat.Map(0, 0),
                                new Chat.Images(foto, "success",new Chat.Size(fileSize, width, height) , 10, 0),
                                vn,
                                time,
                                new Chat.File(file, "", ""),
                                new Chat.User(id, name, email, avatar, true)
                        ));
                    }

                    // Urutkan pesan berdasarkan timestamp ID
                    Collections.sort(newChats, Comparator.comparing(Chat::getId));

                    // Gunakan DiffUtil atau cara lain untuk memperbarui hanya jika ada perubahan
                    if (!chats.equals(newChats)) {
                        chats.clear();
                        chats.addAll(newChats);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Log.d("getCheckData", "setData Data tidak ditemukan");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("getCheckData", "Error getting data", databaseError.toException());
            }
        };

        databaseReference.addValueEventListener(messageListener);
    }
}
