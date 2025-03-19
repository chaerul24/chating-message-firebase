package com.modern.chating.twilio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.twilio.video.Room;

public class VideoEndCallReceiver extends BroadcastReceiver {
    private static Room roomInstance;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("VideoEndCallReceiver", "Ending video call...");
        if (roomInstance != null) {
            roomInstance.disconnect();
            roomInstance = null;
        }
    }

    public static void setRoomInstance(Room room) {
        roomInstance = room;
    }
}

