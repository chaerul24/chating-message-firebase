package com.modern.chating;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {
    private static final int SPLASH_DELAY = 3000; // 3 detik

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        SharedPreferences shared = getSharedPreferences("MyApp", MODE_PRIVATE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(shared != null){
                    if(shared.getBoolean("otp", false) && shared.getBoolean("data", false)){
                        Log.d("SplashScreen", "OTP and data are set");
                        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }else if(shared.getBoolean("otp", false)){
                        Log.d("SplashScreen", "OTP is set");
                        Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }else if(shared.getBoolean("data", false)){
                        Log.d("SplashScreen", "Data is set");
                        Intent intent = new Intent(SplashScreen.this, DataActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }else{
                    Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, SPLASH_DELAY);
    }
}
