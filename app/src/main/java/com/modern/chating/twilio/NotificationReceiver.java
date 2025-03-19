package com.modern.chating.twilio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("ACTION_END_CALL".equals(intent.getAction())) {
            Toast.makeText(context, "Panggilan Dihentikan!", Toast.LENGTH_SHORT).show();
            // Tambahkan logika untuk mengakhiri panggilan di sini
        }
    }
}
