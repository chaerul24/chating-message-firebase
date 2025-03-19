package com.modern.chating;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.modern.chating.firebase.FirebaseExecute;

public class DataActivity extends AppCompatActivity {
    private EditText input_nama;
    private AppCompatButton button;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");
        Log.d("DataActivity", "Email: " + email);

        // Cek apakah email ada dan valid
        if (!email.isEmpty()) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // Cek apakah email terdaftar
            new FirebaseExecute.User().checkIfEmailRegistered(email, new FirebaseExecute.User.OnCheckCallback() {
                @Override
                public void onCheckResult(boolean isRegistered) {
                    progressDialog.dismiss();
                    if (isRegistered) {
                        // Jika email terdaftar, simpan status data dan lanjut ke MainActivity
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("data", true);
                        editor.apply();
                        Intent intent = new Intent(DataActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Jika email belum terdaftar, biarkan pengguna mengisi data nama
                        Log.d("DataActivity", "Email not registered.");
                    }
                }

            });
        }else{
            Log.d("DataActivity", "Email is empty.");
        }

        input_nama = findViewById(R.id.input_name);
        button = findViewById(R.id.btn_next);
        button.setOnClickListener(v -> {
            String nama = input_nama.getText().toString();
            if (nama.isEmpty()) {
                input_nama.setError("Nama wajib diisi untuk identitas anda.");
            } else {
                // Tambahkan user baru jika email belum terdaftar
                new FirebaseExecute.User().addUser(nama, sharedPreferences.getString("email", ""), new FirebaseExecute.User.OnCallback() {
                    @Override
                    public void onStatus(boolean status) {
                        if (status) {
                            // Jika berhasil, simpan status data dan lanjut ke MainActivity
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("data", true);
                            editor.apply();
                            Intent intent = new Intent(DataActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Jika gagal, tampilkan pesan error
                            Toast.makeText(DataActivity.this, "Error adding user. Please try again.", Toast.LENGTH_SHORT).show();
                            Log.e("addUser", "Error adding user");
                        }
                    }
                });
            }
        });
    }
}
