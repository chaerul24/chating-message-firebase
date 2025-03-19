package com.modern.chating.firebase;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.modern.chating.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BrevoExecute {

    private OkHttpClient client = new OkHttpClient();
    Context context;
    public BrevoExecute(Context context){
        this.context = context;
    }
    public void sendOtp(String email){
        Request request = new Request.Builder()
                .url(BuildConfig._EMAIL_SENDER+email)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Brevo", "Status request failed: " + e.getMessage());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("Brevo", "Call Status: " + responseBody);

                try {
                    JSONObject json = new JSONObject(responseBody);
                    if (json.getString("status").equals("success")) {
                        String message = json.getString("message");

                        Log.d("Brevo", "Message: " + message);

                    }
                } catch (JSONException e) {
                    Log.e("Brevo", "JSON Parsing Error: " + e.getMessage());
                }
            }
        });
    }

    public void verify(String email, String otp, OnCallback callback){
        Request request = new Request.Builder()
                .url(BuildConfig._VERIFY_OTP+email+"&otp="+otp)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("Brevo", "Status request failed: " + e.getMessage());
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("Brevo", "Call Status: " + responseBody);

                try {
                    JSONObject json = new JSONObject(responseBody);
                    if (json.getString("status").equals("success")) {
                        String message = json.getString("message");
                        callback.onSuccess(message);

                        Log.d("Brevo", "Message: " + message);

                    }else{
                        callback.onFailure("OTP is invalid or expired");
                    }
                } catch (JSONException e) {
                    Log.e("Brevo", "JSON Parsing Error: " + e.getMessage());
                }
            }
        });

    }
    public interface OnCallback {
        void onSuccess(String responseBody);
        void onFailure(String errorMessage);
    }
}
