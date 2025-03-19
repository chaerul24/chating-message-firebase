package com.modern.chating.twilio;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.modern.chating.R;
import com.modern.chating.file.AudioPlayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

public class CallService extends Service {

    private static final String BASE_URL = "https://twilio.chaerul.biz.id/";
    private static final String CHANNEL_ID = "CallNotification";

    private AudioPlayer audioPlayer;
    private OkHttpClient client = new OkHttpClient();
    private Handler handler = new Handler();
    private boolean isCallActive = false;
    private String sid;
    private int detik = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String number = intent.getStringExtra("number");

        audioPlayer = new AudioPlayer(this, R.raw.call_ringin);
        audioPlayer.play();

        startForeground(1, buildNotification("Calling " + number));

        RequestBody body = new FormBody.Builder()
                .add("to", number)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "index.php")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TwilioCall", "Request failed: " + e.getMessage());
                stopSelf(); // Stop service jika gagal
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("TwilioCall", "Response: " + responseBody);

                try {
                    JSONObject json = new JSONObject(responseBody);
                    sid = json.getString("call_sid");

                    // Tunggu 3 detik sebelum cek status panggilan
                    handler.postDelayed(() -> checkCallStatus(), 3000);
                } catch (JSONException e) {
                    Log.e("TwilioCall", "JSON Parsing Error: " + e.getMessage());
                    stopSelf();
                }
            }
        });

        return START_STICKY; // Agar service restart jika dihentikan oleh sistem
    }

    private void checkCallStatus() {
        Request request = new Request.Builder()
                .url(BASE_URL + "check_status.php?call_sid=" + sid)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TwilioCall", "Status request failed: " + e.getMessage());
                stopSelf();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("TwilioCall", "Call Status: " + responseBody);

                try {
                    JSONObject json = new JSONObject(responseBody);
                    if (json.getString("status").equals("ok")) {
                        String callStatus = json.getString("call_status");

                        if (callStatus.equals("in-progress")) {
                            isCallActive = true;
                            audioPlayer.stop();
                            startTimer();
                        } else if (callStatus.equals("completed")) {
                            isCallActive = false;
                            stopSelf();
                        }
                    }
                } catch (JSONException e) {
                    Log.e("TwilioCall", "JSON Parsing Error: " + e.getMessage());
                    stopSelf();
                }
            }
        });
    }

    private Notification buildNotification(String text) {
        Intent endCallIntent = new Intent(this, EndCallReceiver.class);
        PendingIntent endCallPendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                endCallIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // Tambahkan FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Twilio Call")
                .setContentText(text)
                .setSmallIcon(R.drawable.baseline_call_end_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.baseline_send_24, "End Call", endCallPendingIntent)
                .setOngoing(true)
                .build();
    }



    private void startTimer() {
        new Thread(() -> {
            while (isCallActive) {
                try {
                    Thread.sleep(1000); // **Tunggu 1 detik**
                    detik++;

                    String waktu = String.format("%02d:%02d", detik / 60, detik % 60);
                    updateNotification("Call in progress: " + waktu);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    private void updateNotification(String text) {
        Intent endCallIntent = new Intent(this, EndCallReceiver.class);
        PendingIntent endCallPendingIntent = PendingIntent.getBroadcast(this, 0, endCallIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Twilio Call")
                .setContentText(text)
                .setSmallIcon(R.drawable.baseline_call_white_24)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.baseline_call_end_24, "End Call", endCallPendingIntent) // Tombol End Call
                .setOngoing(true) // Tidak bisa di-swipe
                .build();

        manager.notify(1, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Call Notifications", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCallActive = false;
        handler.removeCallbacksAndMessages(null);
        audioPlayer.stop();

        // Hapus notifikasi saat panggilan berakhir
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(1);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
