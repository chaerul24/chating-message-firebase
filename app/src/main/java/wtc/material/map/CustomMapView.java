package wtc.material.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.util.AttributeSet;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import androidx.preference.PreferenceManager;

public class CustomMapView extends MapView {
    private FusedLocationProviderClient fusedLocationClient;
    private Context mContext;
    private Marker userMarker;

    public CustomMapView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initMap(context);
    }

    private void initMap(Context context) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        // Set up MapView
        this.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        this.setMultiTouchControls(true);
        this.getController().setZoom(15.0);
        this.getController().setCenter(new GeoPoint(-6.2088, 106.8456)); // Default Jakarta

        // Inisialisasi FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        // Ambil lokasi pengguna
        getLocation();
    }

    @SuppressLint("MissingPermission") // Pastikan izin sudah diminta di MainActivity
    public void getLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                updateLocationOnMap(location);
            } else {
                Toast.makeText(mContext, "Gagal mendapatkan lokasi!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLocationOnMap(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        GeoPoint userLocation = new GeoPoint(latitude, longitude);

        // Pindahkan kamera ke lokasi pengguna
        this.getController().setCenter(userLocation);
        this.getController().setZoom(17.0);

        // Tambahkan marker lokasi pengguna
        if (userMarker != null) {
            this.getOverlays().remove(userMarker);
        }

        userMarker = new Marker(this);
        userMarker.setPosition(userLocation);
        userMarker.setTitle("Lokasi Saya");
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        this.getOverlays().add(userMarker);
    }
}
