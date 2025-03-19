package com.modern.chating;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.modern.chating.chat.FgChat;
import com.modern.chating.chat.FgNotification;
import com.modern.chating.chat.FgStatus;
import com.modern.chating.firebase.NotificationMessages;
import com.modern.chating.firebase.user.User;

import java.util.Map;

import wtc.material.mp.LocationService;

public class MainActivity extends AppCompatActivity {
    private FrameLayout frameLayout;
    private BottomNavigationView bottomNavi;
    private SharedPreferences shared;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize SharedPreferences
        shared = getSharedPreferences("MyApp", MODE_PRIVATE);
        TextView text_title = findViewById(R.id.text_title);

        // Check permissions
        checkPermissions();

        // Initialize ViewPager2 and BottomNavigationView
        viewPager = findViewById(R.id.viewPager);
        bottomNavi = findViewById(R.id.bottomNavi);

        // Set adapter for ViewPager2
        FragmentAdapter adapter = new FragmentAdapter(this);
        viewPager.setAdapter(adapter);

        // Set listener for BottomNavigationView
        bottomNavi.setOnItemSelectedListener(menuItem -> {
            int position = 0;
            if(menuItem.getItemId() == R.id.nav_chat) {
                position = 0; // FgChat
            } else if(menuItem.getItemId() == R.id.nav_status) {
                position = 1; // FgStatus
            } else if(menuItem.getItemId() == R.id.navi_notification) {
                position = 2; // FgNotification
            }
            viewPager.setCurrentItem(position);
            return true;
        });

        // Sync ViewPager2 with BottomNavigationView
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        text_title.setText(R.string.app_name);
                        bottomNavi.setSelectedItemId(R.id.nav_chat);
                        break;
                    case 1:
                        text_title.setText("Status");
                        bottomNavi.setSelectedItemId(R.id.nav_status);
                        break;
                    case 2:
                        text_title.setText("Notifikasi");
                        bottomNavi.setSelectedItemId(R.id.navi_notification);
                        break;
                }
            }
        });

        // Pop-up menu
        ImageView popUp = findViewById(R.id.image_popup);
        popUp.setOnClickListener(this::showPopupMenu);
    }

    private static class FragmentAdapter extends FragmentStateAdapter {

        public FragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new FgChat();
                case 1:
                    return new FgStatus();
                case 2:
                    return new FgNotification();
                default:
                    return new FgChat();
            }
        }

        @Override
        public int getItemCount() {
            return 3; // Number of fragments
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.menu_popup, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> false);
        popup.show();
    }

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                    Log.d("Permission", entry.getKey() + " granted: " + entry.getValue());
                }
            });

    private void checkPermissions() {
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        requestPermissionLauncher.launch(permissions);
    }

    @Override
    protected void onStart() {
        super.onStart();
        String myEmail = shared.getString("email", "");
        User user = new User(this);
        user.lastOnline(myEmail);
        NotificationMessages notificationMessages = new NotificationMessages(myEmail, this);
        notificationMessages.listenPercakapan();
    }

}
