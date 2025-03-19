package com.modern.chating;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.textfield.TextInputEditText;
import com.modern.chating.chat.FragmentChat;
import com.modern.chating.firebase.F2base;
import com.modern.chating.firebase.FirebaseExecute;
import com.modern.chating.firebase.send.Messages;

import java.io.OutputStream;

public class ChatActivity extends AppCompatActivity {
    private ImageView image_pictures;
    private Bundle bundle;
    private TextView text_chat_name, text_time;
    private TextInputEditText input_text;
    private ImageView image_call_action;
    private ImageView image_video_action;
    private FirebaseExecute firebase;
    private String email;
    private SharedPreferences shared;
    private Messages messages;
    private static String status = "";
    private Toolbar toolbar_2;
    private String category;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        shared = getSharedPreferences("MyApp", MODE_PRIVATE);
        firebase = new FirebaseExecute(this);
        messages = new Messages(this);
        
        toolbar_2 = findViewById(R.id.toolbar_2);
        


        image_pictures = findViewById(R.id.picture_profile_image);
        text_chat_name = findViewById(R.id.text_chat_name);
        text_time = findViewById(R.id.text_time_chat);
        bundle = getIntent().getExtras();
        if(bundle != null){
            String imageUrl = bundle.getString("imageUrl");

            email = bundle.getString("email", "");
            category = bundle.getString("category", "");
            Log.d("ChatActivity", "Category: "+category);
            Log.d("ChatActivity", "Email: "+email);
            firebase.user.statusFriends(email, new FirebaseExecute.User.OnFriendStatusCallback() {
                @Override
                public void onStatusChecked(String friendEmail, boolean isOnline) {
                    if (isOnline) {
                        text_time.setText("Online");
                    } else {
                        text_time.setText("Offline");
                    }
                }
            });
            Glide.with(this)
                    .load(imageUrl)
                    .circleCrop()
                    .into(image_pictures);

            text_chat_name.setText(bundle.getString("name"));

            FrameLayout frameLayout = findViewById(R.id.frame_chat);
            FragmentChat fragmentChat = new FragmentChat(
                    toolbar_2,
                    bundle.getString("me", ""),
                    bundle.getString("name", "").toString(),
                    email,
                    category,
                    imageUrl);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_chat, fragmentChat).commit();
            image_call_action = findViewById(R.id.image_call_action);
            image_call_action.setOnClickListener(v-> {
                Intent intent = new Intent(this, CallActivity.class);
                intent.putExtra("imageUrl", imageUrl);
                intent.putExtra("name", bundle.getString("name"));
                startActivity(intent);
            });

            image_video_action = findViewById(R.id.image_video_action);
            image_video_action.setOnClickListener(v-> {
                Intent intent = new Intent(this, VideoCallActivity.class);
                intent.putExtra("imageUrl", imageUrl);
                intent.putExtra("name", bundle.getString("name"));
                startActivity(intent);
            });
        }

        System.out.println("Penerima: "+email);
        System.out.println("Pengirim: "+shared.getString("email", ""));


    }

    public static String getChat(){
        return status;
    }

    private Handler handler = new Handler();
    private Runnable readStatusUpdater;

    @Override
    protected void onStart() {
        super.onStart();
        status = email;
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopReadStatusUpdater();
        status = "";
    }

    private void startReadStatusUpdater() {
        readStatusUpdater = new Runnable() {
            @Override
            public void run() {
                String myEmail = shared.getString("email", "");
                messages.updateRead(email, myEmail);

                // Jalankan lagi setelah 1 detik (1000 ms)
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(readStatusUpdater);
    }

    // Hentikan thread ketika user keluar dari chat
    private void stopReadStatusUpdater() {
        if (readStatusUpdater != null) {
            handler.removeCallbacks(readStatusUpdater);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        startReadStatusUpdater();
        if (email != null) {
            String myEmail = shared.getString("email", "");
            messages.updateMessageReadStatus(myEmail, email);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        status = "";
    }



}
