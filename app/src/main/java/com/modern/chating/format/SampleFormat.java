package com.modern.chating.format;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SampleFormat {
    public String getJam(){
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        System.out.println("Waktu sekarang: " + currentTime);
        return currentTime;
    }
}
