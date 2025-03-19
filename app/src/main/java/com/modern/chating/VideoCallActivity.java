package com.modern.chating;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.vision.CameraSource;
import com.modern.chating.twilio.EndCallReceiver;
import com.modern.chating.twilio.TwilioExecute;
import com.modern.chating.twilio.TwilioInstance;
import com.modern.chating.twilio.VideoEndCallReceiver;
import com.twilio.video.Camera2Capturer;
import com.twilio.video.ConnectOptions;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.Room;
import com.twilio.video.TwilioException;
import com.twilio.video.Video;
import com.twilio.video.VideoView;

import java.util.Collections;

import tvi.webrtc.Camera2Enumerator;

public class VideoCallActivity extends AppCompatActivity {
    private VideoView localVideoView;
    private VideoView remoteVideoView;
    private LocalVideoTrack localVideoTrack;
    private static final String CHANNEL_ID = "call_notifications";
    private Camera2Capturer cameraCapturer;
    private Room room;
    private String _t_;
    private String sid = BuildConfig.TWILIO_SID;
    private TextView text_status;
    private LinearLayout ll_end_call;
    private String name;
    private int detik = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isCallActive = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_video_call);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TwilioInstance.getInstance();

        createNotificationChannel(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String imageUrl = bundle.getString("imageUrl");
            name = bundle.getString("name");
            TextView text_name = findViewById(R.id.text_name_call);
            text_name.setText(name);
            new TwilioExecute.Token()._t(name, new TwilioExecute.Token.OnStatusTwilio() {
                @Override
                public void success(String message, String identity) {
                    connectToRoom(sid, message);
                }

                @Override
                public void failed(String message) {
                    Log.e("TwilioError", message);
                }
            });

        }
        text_status = findViewById(R.id.text_status_call);
        ll_end_call = findViewById(R.id.ll_call_end);
        ll_end_call.setOnClickListener(v -> endCall());

        localVideoView = findViewById(R.id.local_video);
        remoteVideoView = findViewById(R.id.remote_video);

        cameraCapturer = new Camera2Capturer(this, getFrontCameraId());
        localVideoTrack = LocalVideoTrack.create(this, true, cameraCapturer);

        Camera2Enumerator camera2Enumerator = new Camera2Enumerator(this);
        String[] deviceNames = camera2Enumerator.getDeviceNames();

        if (deviceNames.length == 0) {
            Log.e("VideoCallActivity", "Tidak ada kamera yang tersedia!");
            return;
        }
        if (localVideoTrack != null) {
            localVideoTrack.addSink(localVideoView);
        }else{
            Log.e("VideoCallActivity", "localVideoTrack is null");
        }





    }



    private String getFrontCameraId() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void connectToRoom(String roomName, String _t_) {
        ConnectOptions connectOptions = new ConnectOptions.Builder(_t_)
                .roomName(roomName)
                .videoTracks(Collections.singletonList(localVideoTrack))
                .build();

        room = Video.connect(this, connectOptions, new Room.Listener() {
            @Override
            public void onConnected(Room room) {
                showNotification(name, "Video Call Berlangsung");
                text_status.setText("Berlangsung");
                startTimer();
                isCallActive = true;
                VideoEndCallReceiver.setRoomInstance(room); // Simpan Room
            }

            @Override
            public void onConnectFailure(Room room, TwilioException e) {
                text_status.setText("Gagal terhubung");
                e.printStackTrace();
                if (localVideoTrack != null) {
                    localVideoTrack.release();
                }
                System.out.println(e.getMessage());
//                finish();
                isCallActive = false;

            }

            @Override
            public void onReconnecting(@NonNull Room room, @NonNull TwilioException e) {
                text_status.setText("Memanggil...");
                showNotification(name, "Memanggil...");
                isCallActive = false;
            }

            @Override
            public void onReconnected(@NonNull Room room) {
                text_status.setText("Menghubungkan...");

            }

            @Override
            public void onDisconnected(Room room, TwilioException e) {
                text_status.setText("Panggilan selesai");
                VideoEndCallReceiver.setRoomInstance(null); // Hapus Room
            }

            @Override
            public void onParticipantConnected(@NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {

            }

            @Override
            public void onParticipantDisconnected(@NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {

            }

            @Override
            public void onRecordingStarted(@NonNull Room room) {

            }

            @Override
            public void onRecordingStopped(@NonNull Room room) {

            }
        });
    }
    private void endCall() {
        if (room != null) {
            room.disconnect();  // Mengakhiri panggilan
            text_status.setText("Panggilan diakhiri");
            isCallActive = false;
            showNotification(name, "Panggilan diakhiri");
            new Handler(Looper.getMainLooper()).postDelayed(() -> finish(), 5000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (localVideoTrack != null) {
            localVideoTrack.release();
        }
    }



    private void showNotification(String title, String message) {
        Intent intent = new Intent(this, VideoEndCallReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Video Call Channel",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_videocam_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.baseline_call_end_24, "End Call", pendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .build();

        notificationManager.notify(1, notification);
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


    private void startTimer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isCallActive) {
                    detik++;
                    int menit = detik / 60;
                    int sisaDetik = detik % 60;
                    String waktu = String.format("%02d:%02d", menit, sisaDetik);
                    text_status.setText(waktu);
                    showNotification(name, "Berlangsug "+waktu);
                    handler.postDelayed(this, 1000); // Update setiap 1 detik
                }
            }
        }, 1000);
    }
}
