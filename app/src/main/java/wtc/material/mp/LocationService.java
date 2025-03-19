package wtc.material.mp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.modern.chating.R;

public class LocationService extends Service {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 201;
    private static final String CHANNEL_ID = "LocationServiceChannel";

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();

        // Buat notification untuk Foreground Service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Tracking Lokasi")
                    .setContentText("Service berjalan di latar belakang")
                    .setSmallIcon(R.drawable.baseline_location_pin_24)  // Ganti dengan icon yang sesuai
                    .build();
            startForeground(1, notification);
        }
    }

    // Membuat LocationRequest
    private void createLocationRequest() {
        locationRequest = LocationRequest.create()
                .setInterval(10000) // Update setiap 10 detik
                .setFastestInterval(5000) // Interval tercepat 5 detik
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Akurasi tinggi
    }

    // Callback untuk pembaruan lokasi
    private void startLocationUpdates(final String email) {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && locationResult.getLocations().size() > 0) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        updateUserLocation(email, location);
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("LocationService", "Izin lokasi tidak diberikan!");
            stopSelf();
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    // Mengupdate lokasi ke Firebase
    private void updateUserLocation(String email, Location location) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(email.replace(".", "_"));
        userRef.child("location").child("latitude").setValue(location.getLatitude());
        userRef.child("location").child("longitude").setValue(location.getLongitude());

        Log.d("LocationService", "Lokasi diperbarui: " + location.getLatitude() + ", " + location.getLongitude());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String email = intent.getStringExtra("email");
        if (email != null) {
            startLocationUpdates(email);
        }
        return START_NOT_STICKY;  // Gunakan START_NOT_STICKY agar tidak restart otomatis
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Membuat notification channel untuk Foreground Service
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
