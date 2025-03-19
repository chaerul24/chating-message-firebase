package com.modern.chating.firebase;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.modern.chating.ChatActivity;
import com.modern.chating.R;
import com.modern.chating.firebase.user.User;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationMessages {
    private final Context context;
    String myEmail;
    private final Set<String> notifiedMessages = new HashSet<>();

    public NotificationMessages(String myEmail, Context context) {
        this.myEmail = myEmail;
        this.context = context;
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "chat_channel",
                    "Chat Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public void listenPercakapan() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("messages");
        databaseReference.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot conversationSnapshot : dataSnapshot.getChildren()) {
                        String conversationID = conversationSnapshot.getKey();
                        listenPercakapanById(conversationID);
                    }
                } else {
                    Log.d("Percakapan", "Tidak ada percakapan");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Percakapan", "Gagal mengambil data: " + databaseError.getMessage());
            }
        });
    }

    private void listenPercakapanById(String conversationId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("messages").child(conversationId);

        User user = new User();
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                boolean isRead = snapshot.child("read").getValue(Boolean.class) != null &&
                        snapshot.child("read").getValue(Boolean.class);

                if (!isRead) {
                    String receive = snapshot.child("receive").getValue(String.class);
                    String sender = snapshot.child("sender").getValue(String.class);
                    String message = snapshot.child("message").getValue(String.class);

                    // Cek apakah pengirim adalah user yang sedang login

                    if (myEmail != null && myEmail.equals(sender)) {
                        return; // Jangan kirim notifikasi ke diri sendiri
                    }

                    if(receive.equals("chatbot@app.com")) {
                        return;
                    }
                    if(sender.equals("chatbot@app.com")) {
                        return;
                    }

                    user.getName(receive, new User.OnStatus() {
                        @Override
                        public void onSuccess(String nama) {
                            Log.d("Percakapan", "Nama pengirim: " + nama);
                            sendNotification(conversationId, sender, nama, message);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.d("Percakapan", "Error: " + errorMessage);
                        }
                    });
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Percakapan", "Gagal mengambil data: " + error.getMessage());
            }
        });
    }

    private boolean isAppInForeground() {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) return false;

        List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
        if (processes == null) return false;

        for (ActivityManager.RunningAppProcessInfo process : processes) {
            if (process.processName.equals(context.getPackageName()) &&
                    process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true; // 🔥 Aplikasi sedang di depan
            }
        }
        return false;
    }


    private void sendNotification(String conversationId, String sender, String receiver, String message) {
        // 🔥 Jika user sedang di ChatActivity dengan pengirim pesan, jangan munculkan notifikasi
        if (ChatActivity.getChat() != null && ChatActivity.getChat().equals(sender)) {
            System.out.println("Current > ChatActivity.currentChatUser: "+ChatActivity.getChat());
            return;
        }


        String messageKey = conversationId + "_" + message;
        if (notifiedMessages.contains(messageKey)) return;
        notifiedMessages.add(messageKey);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            return;
        }

        String groupKey = "GROUP_CHAT_" + receiver;

        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("chatUserID", receiver);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "chat_channel")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(receiver)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setGroup(groupKey)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        NotificationCompat.Builder summaryNotification = new NotificationCompat.Builder(context, "chat_channel")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Pesan baru dari " + receiver)
                .setContentText("Anda memiliki pesan baru.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setGroup(groupKey)
                .setGroupSummary(true)
                .setAutoCancel(true);

        notificationManager.notify(message.hashCode(), builder.build());
        notificationManager.notify(groupKey.hashCode(), summaryNotification.build());
    }




}
