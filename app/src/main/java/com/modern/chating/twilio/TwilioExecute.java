package com.modern.chating.twilio;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import com.modern.chating.BuildConfig;
import com.modern.chating.R;
import com.modern.chating.file.AudioPlayer;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;

public class TwilioExecute {
    private final AudioPlayer audioPlayer;
    private OkHttpClient client = new OkHttpClient();
    private TextView status;
    private int detik = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isCallActive = false;
    private String sid;
    private Context context; // Tambahkan context untuk akses MediaPlayer
    private NotificationManager notificationManager;
    private static final String CHANNEL_ID = "call_notifications";
    private  String name;
    public TwilioExecute(Context context, String name, String number, TextView status) {
        if(name.isEmpty()){
            this.name = number;
        }else{
            this.name = name;
        }
        this.context = context;
        this.status = status;
        createNotificationChannel(context);
        TwilioInstance.init(this);
        audioPlayer = new AudioPlayer(context, R.raw.call_ringin);
        audioPlayer.setVolume(100);
        audioPlayer.setLooping(true);
        audioPlayer.play();
        showNotification(name, "Memanggil...");

        RequestBody body = new FormBody.Builder()
                .add("to", number)
                .build();

        Request request = new Request.Builder()
                .url(BuildConfig._INDEX)
                .post(body)
                .build();

        String finalName = name;
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TwilioCall", "Request failed: " + e.getMessage());
                audioPlayer.stop();
                showNotification(finalName, "Tidak ada koneksi");
                removeNotification();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("TwilioCall", "Response: " + responseBody);

                try {
                    JSONObject json = new JSONObject(responseBody);
                    String callSid = json.getString("call_sid");
                    Log.d("TwilioCall", "Call SID: " + callSid);

                    // Tunggu 3 detik sebelum memeriksa status panggilan
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        checkCallStatus(callSid);
                    }, 3000);

                } catch (JSONException e) {
                    Log.e("TwilioCall", "JSON Parsing Error: " + e.getMessage());
                    audioPlayer.stop();
                    removeNotification();
                }
            }
        });
    }

    private void checkCallStatus(String callSid) {
        this.sid = callSid;
        Request request = new Request.Builder()
                .url(BuildConfig._CALL_STATUS+ callSid)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TwilioCall", "Status request failed: " + e.getMessage());
                audioPlayer.stop();
                removeNotification();

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("TwilioCall", "Call Status: " + responseBody);

                try {
                    JSONObject json = new JSONObject(responseBody);
                    if (json.getString("status").equals("ok")) {
                        String callStatus = json.getString("call_status");

                        Log.d("TwilioCall", "Current Call Status: " + callStatus);

                        if (callStatus.equals("in-progress")) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                isCallActive = true;
                                audioPlayer.stop();
                                startTimer();

                            });
                        } else if (callStatus.equals("completed")) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                isCallActive = false;
                                audioPlayer.stop();
                                status.setText("Call Ended");
                                handler.removeCallbacksAndMessages(null); // Hentikan timer
                                showNotification("Call Ended", "Conversation ended");
                                removeNotification();
                            });
                        }
                    }
                } catch (JSONException e) {
                    Log.e("TwilioCall", "JSON Parsing Error: " + e.getMessage());
                    audioPlayer.stop();
                    removeNotification();
                }
            }
        });
    }

    // Fungsi untuk memulai timer
    private void startTimer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isCallActive) {
                    detik++;
                    int menit = detik / 60;
                    int sisaDetik = detik % 60;
                    String waktu = String.format("%02d:%02d", menit, sisaDetik);
                    status.setText(waktu);
                    showNotification(name, "Berlangsug "+waktu);
                    handler.postDelayed(this, 1000); // Update setiap 1 detik
                }
            }
        }, 1000);
    }

    public void end_call(OnStatus onStatus){
        Request request = new Request.Builder()
                .url(BuildConfig._CALL_END+ sid)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TwilioCall", "Status request failed: " + e.getMessage());
                audioPlayer.stop();
                removeNotification();
                sound_ended();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("TwilioCall", "Call Status: " + responseBody);

                try {
                    JSONObject json = new JSONObject(responseBody);
                    if (json.getString("status").equals("ok")) {
                        String callStatus = json.getString("message");

                        Log.d("TwilioCall", "Current Call Status: " + callStatus);

                        if (callStatus.equals("Call ended")) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                isCallActive = false;
                                handler.removeCallbacksAndMessages(null);
                                audioPlayer.stop();
                                onStatus.success("Call Ended");
                                showNotification("Call Ended", "Conversation ended");
                                removeNotification();
                                sound_ended();
                            });
                        } else if (callStatus.equals("Call SID required")) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                isCallActive = false;
                                audioPlayer.stop();
                                onStatus.failed("Tidak ada koneksi");
                                handler.removeCallbacksAndMessages(null); // Hentikan timer
                                removeNotification();
                                sound_ended();
                            });
                            showNotification("MR.", "Conversation ended");
                        }
                    }
                } catch (JSONException e) {
                    Log.e("TwilioCall", "JSON Parsing Error: " + e.getMessage());
                    audioPlayer.stop();
                    sound_ended();
                    removeNotification();
                }
            }
        });
    }

    private void sound_ended(){
        AudioPlayer audioPlayer = new AudioPlayer(context, R.raw.call_ended);
        audioPlayer.play();
    }

    public interface OnStatus {
        void success(String message);
        void failed(String message);
    }

    private void showNotification(String title, String message) {
        Intent intent = new Intent(context, EndCallReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_call_white_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.baseline_call_end_24, "End Call", pendingIntent) // Tambah tombol "End Call"
                .setOngoing(true) // Notifikasi tetap berjalan sampai dihapus secara manual
                .setAutoCancel(false) // Pastikan tidak tertutup otomatis saat diklik
                .build();

        if (notificationManager != null) {
            notificationManager.notify(1, notification);
        }
    }

    private void removeNotification() {
        if (notificationManager != null) {
            notificationManager.cancel(1);
        }
    }







    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Call Notifications";
            String description = "Notifies about call status";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }


    public static class Token {
        private OkHttpClient client = new OkHttpClient();
        public void _t(String name, OnStatusTwilio onStatus){
            Request request = new Request.Builder()
                    .url(BuildConfig._TOKEN + name)
                    .get()
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("TwilioCall", "Status request failed: " + e.getMessage());
                    onStatus.failed(e.getMessage());

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.d("TwilioCall", "Call Status: " + responseBody);

                    try {
                        JSONObject json = new JSONObject(responseBody);
                        if (json.getString("status").equals("ok")) {
                            String identity = json.getString("identity");
                            String _t_ = json.getString("token");

                            Log.d("TwilioCall", "identity: " + identity);

                            if (!_t_.isEmpty()) {
                                onStatus.success(_t_, identity);
                            }
                        }
                    } catch (JSONException e) {
                        Log.e("TwilioCall", "JSON Parsing Error: " + e.getMessage());
                        onStatus.failed("Tidak ada koneksi");
                    }
                }
            });
        }

        public interface OnStatusTwilio {
            void success(String message, String identity);
            void failed(String message);
        }
    }

}
