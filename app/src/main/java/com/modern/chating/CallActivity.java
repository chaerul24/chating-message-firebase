package com.modern.chating;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.modern.chating.twilio.TwilioExecute;

public class CallActivity extends AppCompatActivity {
    private LinearLayout ll_call_end;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_call);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            String imageUrl = bundle.getString("imageUrl");
            ImageView image_avatar = findViewById(R.id.image_avatar);
            Glide.with(this)
                    .load(imageUrl)
                    .circleCrop()
                    .into(image_avatar);

            String name = bundle.getString("name");
            TextView text_name = findViewById(R.id.text_name_call);
            text_name.setText(name);

            TextView text_status = findViewById(R.id.text_status_call);

            ll_call_end = findViewById(R.id.ll_call_end);

            TwilioExecute twilio = new TwilioExecute(this, name, "+16613702640", text_status);
            ll_call_end.setOnClickListener(v-> twilio.end_call(new TwilioExecute.OnStatus() {
                @Override
                public void success(String message) {
                    if (message.equals("Call Ended")) {
                        text_status.setText("Panggilan diakhiri");
                        new Handler(Looper.getMainLooper()).postDelayed(() -> finish(), 5000);
                    }
                }

                @Override
                public void failed(String message) {
                    text_status.setText(message);
                }
            }));
        }

    }
}
