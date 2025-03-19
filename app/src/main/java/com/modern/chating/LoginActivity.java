package com.modern.chating;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.Firebase;
import com.hbb20.CountryCodePicker;
import com.modern.chating.firebase.BrevoExecute;
import com.modern.chating.firebase.FirebaseExecute;

public class LoginActivity extends AppCompatActivity {
    private String countryCode = "62"; // Default Indonesia
    private EditText input_email, input_verify;
    private AppCompatButton button;
    private CountryCodePicker ccp;
    private FirebaseExecute firebase;
    private BrevoExecute brevoExecute;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        if(sharedPreferences.getBoolean("otp", false)){
            Intent intent = new Intent(LoginActivity.this, DataActivity.class);
            startActivity(intent);
            finish();
        }

        if(sharedPreferences.getBoolean("data", false) && sharedPreferences.getBoolean("otp", false)){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        brevoExecute = new BrevoExecute(this);

        button = findViewById(R.id.btn_next);
        input_email = findViewById(R.id.input_email);
        input_verify = findViewById(R.id.input_verify_otp);
        input_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                button.setEnabled(!editable.toString().isEmpty());
            }
        });

        input_verify.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                if (text.length() == 6) {
                    brevoExecute.verify(input_email.getText().toString().trim(), input_verify.getText().toString(), new BrevoExecute.OnCallback() {
                        @Override
                        public void onSuccess(String responseBody) {
                            if(responseBody.equals("OTP is valid")){
                                Intent intent = new Intent(LoginActivity.this, DataActivity.class);
                                startActivity(intent);
                                finish();
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("email", input_email.getText().toString().trim());
                                editor.putBoolean("otp", true);
                                editor.apply();
                            }else{
                                Toast.makeText(LoginActivity.this, "OTP tidak valid", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            System.out.println(errorMessage);
                            
                        }
                    });
                }
            }
        });


        // Handle klik tombol
        button.setOnClickListener(v -> {
            brevoExecute.sendOtp(input_email.getText().toString().trim());

            // Disable tombol
            button.setEnabled(false);

            // Mulai timer 20 detik
            new CountDownTimer(50000, 1000) {
                public void onTick(long millisUntilFinished) {
                    button.setText((millisUntilFinished / 1000) + " ");
                }

                public void onFinish() {
                    button.setEnabled(true);
                    button.setText("Kirim Ulang OTP");
                }
            }.start();
        });


        button.setEnabled(false);
    }
}
