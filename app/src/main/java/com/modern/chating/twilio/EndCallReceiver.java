package com.modern.chating.twilio;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class EndCallReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "call_notifications"; // Channel harus tetap String
    private static final int NOTIFICATION_ID = 1; // ID notifikasi untuk dihapus

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("EndCallReceiver", "End call button pressed");

        // Dapatkan instance TwilioExecute dari aplikasi (gunakan singleton atau mekanisme lain)
        TwilioExecute twilioExecute = TwilioInstance.getInstance();
        if (twilioExecute != null) {
            twilioExecute.end_call(new TwilioExecute.OnStatus() {
                @Override
                public void success(String message) {
                    Log.d("EndCallReceiver", "Call ended successfully");

                    // Hapus notifikasi setelah panggilan berakhir
                    NotificationManager notificationManager = (NotificationManager)
                            context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null) {
                        notificationManager.cancel(NOTIFICATION_ID); // Pakai NOTIFICATION_ID, bukan CHANNEL_ID
                    }
                }

                @Override
                public void failed(String message) {
                    Log.d("EndCallReceiver", "Failed to end call");
                }
            });
        }
    }
}
