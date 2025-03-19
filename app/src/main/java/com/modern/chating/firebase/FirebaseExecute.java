package com.modern.chating.firebase;

import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.modern.chating.R;
import com.modern.chating.adapter.ChatAdapter;
import com.modern.chating.adapter.ItemAdapter;
import com.modern.chating.adapter.StatusAdapter;
import com.modern.chating.modal.Chat;
import com.modern.chating.modal.ModalArray;
import com.modern.chating.modal.Status;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FirebaseExecute {
    private Context context;
    public User user = new User();
    private FirebaseAuth mAuth;

    public Message message;
    public FirebaseExecute() {}
    public FirebaseExecute(Context context){
        this.context = context;
        FirebaseApp.initializeApp(context);
        mAuth = FirebaseAuth.getInstance();
    }

    public static class Status {
        String email;
        StatusAdapter adapter;
        ArrayList<com.modern.chating.modal.Status> array;
        RecyclerView recyclerView;
        Context context;

        public Status(Context context, String email, ArrayList<com.modern.chating.modal.Status> array, StatusAdapter adapter, RecyclerView recyclerView) {
            this.email = email;
            this.array = array;
            this.context = context;
            this.adapter = adapter;
            this.recyclerView = recyclerView;
        }

        public void load() {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("status");
            databaseReference.addValueEventListener(new ValueEventListener() {
                private boolean read;

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    array.clear();  // Clear the existing list before adding new data

                    if (dataSnapshot.exists()) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            String key = data.getKey();
                            String sender = data.child("sender").getValue(String.class);
                            String file = data.child("file").getValue(String.class);
                            String jam = data.child("jam").getValue(String.class);
                            String tanggal = data.child("tanggal").getValue(String.class);

                            Log.d("Status", "=====================");
                            Log.d("Status", "Key: " + key);
                            Log.d("Status", "Sender: " + sender);
                            Log.d("Status", "File: " + file);
                            Log.d("Status", "Jam: " + jam);
                            Log.d("Status", "Tanggal: " + tanggal);

                            for (DataSnapshot view : data.child("view").getChildren()) {
                                String email_view = view.child("email").getValue(String.class);
                                read = Boolean.TRUE.equals(view.child("read").getValue(Boolean.class));
                                Log.d("Status", "Email View: " + email_view);
                                Log.d("Status", "Read: " + read);
                            }
                            Log.d("Status", "=====================");

                            FirebaseExecute.User user = new FirebaseExecute.User();
                            user.detailUser(sender.replace(".", "_"), new FirebaseExecute.User.OnDetailUser() {
                                @Override
                                public void onSuccess(String name, String avatar, boolean status, String email_) {
                                    Log.d("Status", "Status Saya: " + sender + "=" + email);

                                    // Check if the sender already exists in the array
                                    boolean isDuplicate = false;
                                    for (com.modern.chating.modal.Status status_ : array) {
                                        if (status_.getSender().equals(sender)) {
                                            isDuplicate = true;
                                            break;
                                        }
                                    }

                                    // If sender is not a duplicate, add the status
                                    if (!isDuplicate) {
                                        if (sender.equals(email)) {
                                            array.add(0, new com.modern.chating.modal.Status(
                                                    "Anda",
                                                    sender,
                                                    avatar,
                                                    file,
                                                    jam,
                                                    tanggal,
                                                    read
                                            ));
                                        } else {
                                            array.add(new com.modern.chating.modal.Status(
                                                    name,
                                                    sender,
                                                    avatar,
                                                    file,
                                                    jam,
                                                    tanggal,
                                                    read
                                            ));
                                        }
                                    }

                                    // Update the RecyclerView
                                    StatusAdapter adapter = new StatusAdapter(context, array);
                                    recyclerView.setAdapter(adapter);
                                }

                                @Override
                                public void onFailure(String message) {
                                    Log.e("Status", "Error getting user details: " + message);
                                }
                            });
                        }
                    } else {
                        Log.d("Status", "Data tidak ditemukan");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("Status", "Error getting data", databaseError.toException());
                }
            });
        }

    }


    public static class User {
        private final DatabaseReference mDatabase;
        public User() {
            mDatabase = FirebaseDatabase.getInstance().getReference("users");
        }

        public void checkIfEmailRegistered(String email, OnCheckCallback callback) {
            // Ubah titik (.) menjadi karakter lain (misalnya "_")
            String emailKey = email.replace(".", "_");

            // Mengecek apakah email sudah terdaftar
            mDatabase.child(emailKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Jika data ditemukan, berarti email sudah terdaftar
                    if (dataSnapshot.exists()) {
                        // Callback dengan status "email sudah terdaftar"
                        callback.onCheckResult(true);
                    } else {
                        // Callback dengan status "email belum terdaftar"
                        callback.onCheckResult(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Callback jika ada error
                    callback.onCheckResult(false);
                }
            });
        }

        // Interface untuk callback
        public interface OnCheckCallback {
            void onCheckResult(boolean isRegistered);
        }


        public void dataProfile(String email, OnDataProfile dataProfile){
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users/"+email.replace(".", "_"));
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        String name = dataSnapshot.child("name").getValue(String.class);
                        String avatar = dataSnapshot.child("avatar").getValue(String.class);
                        boolean status = dataSnapshot.child("status").getValue(Boolean.class);
                        String email = dataSnapshot.child("email").getValue(String.class);

                        dataProfile.onSuccess(name, avatar, status, email);
                    }else{
                        dataProfile.onFailure("Data tidak ditemukan");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    dataProfile.onFailure("Error getting data");
                    Log.e("Firebase", "Error getting data", databaseError.toException());
                }

            });
        }

        public interface OnDataProfile {
            void onSuccess(String name, String avatar, boolean status, String email);
            void onFailure(String message);
        }

        public void addUser(String name, String email, OnCallback callback) {

            // Ganti titik (.) di email agar bisa menjadi key unik
            String emailKey = email.replace(".", "_");

            // Buat HashMap untuk menyimpan data user
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("email", email);
            userData.put("status", true);
            userData.put("lastOnline", System.currentTimeMillis());
            userData.put("avatar", "https://wallpapers.com/images/featured/cool-profile-pictures-4co57dtwk64fb7lv.jpg");

            // Simpan data ke Firebase di node "users/{emailKey}"
            mDatabase.child(emailKey).setValue(userData)
                    .addOnSuccessListener(unused -> callback.onStatus(true))
                    .addOnFailureListener(e -> {
                        Log.e("addUser", "Error adding user", e);
                        callback.onStatus(false);
                    });
        }

        public void addFriends(String emailSaya, String emailTeman, F2base.OnCheckCallback callback){
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users/"+emailSaya.replace(".", "_")+"/firends/");

            HashMap<String, Object> data = new HashMap<>();
            data.put("email", emailTeman);
            data.put("confirmation", true);
            data.put("time", System.currentTimeMillis());
            mDatabase.push().setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    callback.onResult(true);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    callback.onResult(false);
                    Log.d("Firebase", "Error adding friends:");
                    Log.d("Firebase", e.getMessage());
                }
            });
        }


        public interface OnCallback {
            void onStatus(boolean status);
        }

        public void listUser(String user, ArrayList<ModalArray> array, ItemAdapter adapter, RecyclerView recyclerView) {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.replace(".", "_"))
                    .child("firends");

            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    array.clear(); // Kosongkan array sebelum mengisi ulang
                    HashSet<String> uniqueEmails = new HashSet<>(); // Set untuk menyimpan email unik
                    int totalFriends = (int) dataSnapshot.getChildrenCount();
                    final int[] loadedFriends = {0};

                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        String key = data.getKey();
                        String email_ = data.child("email").getValue(String.class);

                        Log.d("listUser", "Email Saya: " + user);
                        Log.d("listUser", "Key: " + key);
                        Log.d("listUser", "Email: " + email_);

                        if (email_ != null && !uniqueEmails.contains(email_)) {
                            uniqueEmails.add(email_); // Tambahkan ke HashSet agar tidak duplikat

                            detailUser(email_, new OnDetailUser() {
                                @Override
                                public void onSuccess(String name, String avatar, boolean status, String email) {
                                    array.add(new ModalArray(
                                            name,
                                            email,
                                            R.drawable.baseline_check_circle_outline_24,
                                            avatar,
                                            "user"
                                    ));

                                    loadedFriends[0]++;

                                    if (loadedFriends[0] == totalFriends) {
                                        adapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onFailure(String message) {
                                    Log.e("listUser", "Error getting data: " + message);
                                }
                            });
                        }
                    }

                    if (totalFriends == 0) {
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("listUser", "Error getting data", databaseError.toException());
                }
            });
        }

        public void listChatbot(String user, ArrayList<ModalArray> array, ItemAdapter adapter, RecyclerView recyclerView) {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.replace(".", "_"))
                    .child("chatbot");

            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    array.clear(); // Bersihkan daftar sebelum menambahkan data baru
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) { // Langsung loop dari `dataSnapshot`
                            String name = data.child("name").getValue(String.class);
                            String email = data.child("owner").getValue(String.class);
                            String image = data.child("image").getValue(String.class);
                            String avatar = data.child("avatar").getValue(String.class);

                            if (name != null && email != null && image != null) {
                                array.add(new ModalArray(
                                        name,
                                        email,
                                        R.drawable.baseline_check_circle_outline_24,
                                        avatar,
                                        email.replace(".", "_")+"_"+name.replace(" ", "_")
                                ));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d("listChatbot", "Tidak ada data chatbot.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("listChatbot", "Error getting data", databaseError.toException());
                }
            });
        }




        public void detailUser(String email, OnDetailUser callback) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users/"+email.replace(".", "_"));
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String avatar = dataSnapshot.child("avatar").getValue(String.class);
                    boolean status = Boolean.TRUE.equals(dataSnapshot.child("status").getValue(Boolean.class));

                    Log.d("listUser", "======== detail user ========");
                    Log.d("listUser", "Name: " + name);
                    Log.d("listUser", "Avatar: " + avatar);
                    Log.d("listUser", "Status: " + status);
                    Log.d("listUser", "======== end user ========");

                    callback.onSuccess(name, avatar, status, email);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    callback.onFailure(databaseError.getMessage());
                }
            });
        }

        public interface OnDetailUser {
            void onSuccess(String name, String avatar, boolean status, String email);
            void onFailure(String message);
        }



        public void statusFriends(String email, OnFriendStatusCallback callback) {
            DatabaseReference friendsRef = FirebaseDatabase.getInstance()
                    .getReference("users/" + email.replace(".", "_") + "/status");

            friendsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        boolean isOnline = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                        callback.onStatusChecked(email, isOnline);
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


        public interface OnFriendStatusCallback {
            void onStatusChecked(String friendEmail, boolean isOnline);
        }


        public void getFriends(String userEmail) {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("friends");

            String userKey = userEmail.replace(".", "_");

            mDatabase.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<String> friendsList = new ArrayList<>();
                    for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                        friendsList.add(friendSnapshot.getValue(String.class));
                    }
                    Log.d("Firebase", "Teman: " + friendsList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Gagal membaca teman: " + error.getMessage());
                }
            });
        }


        public void addFriend(String userEmail, String friendEmail) {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("friends");

            // Ganti titik di email agar bisa jadi key unik
            String userKey = userEmail.replace(".", "_");
            String friendKey = friendEmail.replace(".", "_");

            // Tambahkan teman ke daftar friends
            mDatabase.child(userKey).push().setValue(friendEmail)
                    .addOnSuccessListener(unused -> Log.d("Firebase", "Teman berhasil ditambahkan!"))
                    .addOnFailureListener(e -> Log.e("Firebase", "Gagal menambahkan teman: " + e.getMessage()));
        }
        public void isFriend(String userEmail, String friendEmail, OnFriendCheckCallback callback) {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("friends");

            String userKey = userEmail.replace(".", "_");
            String friendKey = friendEmail.replace(".", "_");

            mDatabase.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean isFriend = false;
                    for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                        String friend = friendSnapshot.getValue(String.class);
                        if (friend.equals(friendEmail)) {
                            isFriend = true;
                            break;
                        }
                    }
                    callback.onCheckResult(isFriend);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onCheckResult(false);
                }
            });
        }

        public interface OnFriendCheckCallback {
            void onCheckResult(boolean isFriend);
        }

    }

    public static class Message {
        private String sender;
        private String receiver;
        private String message;
        private String foto;
        private String vn;
        private String file;
        private long timestamp;
        private String emailSender;
        private String emailReceive;

        // Constructor
        public Message() {
        }

        // Constructor
        public Message(String nameSender, String nameReceive, String emailSender, String emailReceive, String message, String file, String foto, String vn) {
            this.sender = nameSender;
            this.receiver = nameReceive;
            this.emailSender = emailSender.replace(".", "_");
            this.emailReceive = emailReceive.replace(".", "_");
            this.message = message;
            this.file = file;
            this.foto = foto;
            this.vn = vn;
            this.timestamp = System.currentTimeMillis();
        }

        // Method to save message to Firebase with callback validation
        public void saveToDatabase(final OnCallBackStatus callback) {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("messages");

            // Create a map with the message data
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("id", mDatabase.push().getKey());
            messageData.put("sender", sender);
            messageData.put("receiver", receiver);
            messageData.put("message", message);
            messageData.put("foto", foto);
            messageData.put("vn", vn);
            messageData.put("file", file);
            messageData.put("timestamp", timestamp);
            messageData.put("time", getJamMenit());

            // Create a new unique key for each message
            String uniqueMessageKey = mDatabase.push().getKey();

            // Save the message under a unique key
            mDatabase.child(emailSender+"_"+emailReceive).child(uniqueMessageKey).setValue(messageData).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // If the operation is successful, call the success callback
                        callback.onSuccess("Message sent successfully.");
                    } else {
                        // If the operation fails, call the failure callback
                        callback.onFailure(task.getException().getMessage());
                    }
                }
            });
        }

        public static String getJamMenit() {
            // Mendapatkan waktu saat ini
            Calendar calendar = Calendar.getInstance();

            // Membuat format waktu
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");

            // Mengembalikan waktu dalam format 12 jam dengan AM/PM
            return sdf.format(calendar.getTime());
        }




        public static String replaceNullWithEmpty(String value) {
            return "null".equals(value) ? "" : value;
        }

        public void checkData(String senderEmail, String receiverEmail, OnCheckDataListener listener) {
            // Query pertama
            String query1 = senderEmail + "_" + receiverEmail;
            System.out.println(query1);
            final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("messages/" + query1);

            // Mengecek apakah data ada di query pertama
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Cek apakah data ada pada query pertama
                    if (dataSnapshot.exists()) {
                        listener.onDataChecked(query1); // Callback with query1
                    } else {
                        // Jika tidak ada data di query pertama, coba query kedua
                        String query2 = receiverEmail + "_" + senderEmail;
                        DatabaseReference mDatabase2 = FirebaseDatabase.getInstance().getReference("messages/" + query2);

                        // Mengecek data di query kedua
                        mDatabase2.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    listener.onDataChecked(query2); // Callback with query2
                                } else {
                                    listener.onDataChecked("null"); // Callback with "null"
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                listener.onDataChecked("null"); // Callback with "null" on error
                                Log.e("Firebase", "Error checking data in query2: " + databaseError.getMessage());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    listener.onDataChecked("null"); // Callback with "null" on error
                    Log.e("Firebase", "Error checking data in query1: " + databaseError.getMessage());
                }
            });
        }

        public interface OnCheckDataListener {
            void onDataChecked(String query);
        }
    }


    public interface OnCallBackStatus {
        void onSuccess(String message);
        void onFailure(String message);

    }

}
