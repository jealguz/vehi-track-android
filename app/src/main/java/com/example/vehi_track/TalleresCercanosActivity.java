package com.example.vehi_track;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.HashMap;
import java.util.Map;

/**
 * Módulo de Geolocalización Real.
 * Implementa GPS dinámico para centrar el mapa en la posición actual del usuario
 * y permite la interacción con talleres locales.
 */
public class TalleresCercanosActivity extends BaseActivity {

    private MapView map;
    private MyLocationNewOverlay mLocationOverlay;
    private LinearLayout layoutLoading;
    private FirebaseFirestore db;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuración necesaria para OSMDroid
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        establecerContenido(R.layout.activity_talleres_cercanos);
        db = FirebaseFirestore.getInstance();

        // 1. CONFIGURACIÓN DE NAVEGACIÓN
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Talleres Cercanos");
            androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        // 2. INICIALIZACIÓN DEL MAPA
        layoutLoading = findViewById(R.id.layoutLoading);
        map = findViewById(R.id.map);

        if (map != null) {
            map.setMultiTouchControls(true);
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setVisibility(View.INVISIBLE);

            // Verificación de permisos y activación de ubicación real
            verificarPermisosYConfigurarGPS();

            simularCargaDeTalleres();
        }
    }

    private void verificarPermisosYConfigurarGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            activarCapaUbicacion();
        }
    }

    private void activarCapaUbicacion() {
        // Capa que gestiona la ubicación real del dispositivo
        this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        this.mLocationOverlay.enableMyLocation(); // Dibuja el punto azul
        this.mLocationOverlay.enableFollowLocation(); // Sigue al usuario al moverse

        // Cuando el GPS fija la posición por primera vez, el mapa vuela hacia allá
        this.mLocationOverlay.runOnFirstFix(() -> {
            runOnUiThread(() -> {
                if (mLocationOverlay.getMyLocation() != null) {
                    map.getController().animateTo(mLocationOverlay.getMyLocation());
                    map.getController().setZoom(16.5);
                }
            });
        });

        map.getOverlays().add(this.mLocationOverlay);
    }

    private void simularCargaDeTalleres() {
        if (layoutLoading != null) layoutLoading.setVisibility(View.VISIBLE);

        new Handler().postDelayed(() -> {
            if (layoutLoading != null) layoutLoading.setVisibility(View.GONE);

            // Si tenemos ubicación real, ponemos los talleres cerca de nosotros
            if (mLocationOverlay != null && mLocationOverlay.getMyLocation() != null) {
                double miLat = mLocationOverlay.getMyLocation().getLatitude();
                double miLon = mLocationOverlay.getMyLocation().getLongitude();

                // Taller 1: a unos metros de distancia
                agregarMarcador(miLat + 0.001, miLon + 0.001, "Taller Principal", "Especialista en Motos");
                // Taller 2: a unos metros en otra dirección
                agregarMarcador(miLat - 0.001, miLon - 0.001, "Motos del Sur", "Repuestos y Accesorios");
            } else {
                // Si no hay GPS aún, los ponemos en Guavatá por defecto
                agregarMarcador(5.9922, -73.6744, "Taller Principal El Amigo", "Especialista en Motos");
            }

            map.setVisibility(View.VISIBLE);
            map.invalidate();
        }, 2000);
    }

    private void agregarMarcador(double lat, double lon, String titulo, String snippet) {
        if (map == null) return;
        Marker nodeMarker = new Marker(map);
        nodeMarker.setPosition(new GeoPoint(lat, lon));
        nodeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        nodeMarker.setTitle(titulo);
        nodeMarker.setSnippet(snippet);
        nodeMarker.setOnMarkerClickListener((marker, mapView) -> {
            mostrarModalTaller(marker.getTitle(), marker.getSnippet());
            return true;
        });
        map.getOverlays().add(nodeMarker);
    }

    private void mostrarModalTaller(String nombre, String descripcion) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_modal_taller, null);

        TextView tvNombre = view.findViewById(R.id.tvNombreTaller);
        TextView tvDesc = view.findViewById(R.id.tvDescripcionTaller);
        Button btnFav = view.findViewById(R.id.btnAgregarFavorito);

        tvNombre.setText(nombre);
        tvDesc.setText(descripcion);

        btnFav.setOnClickListener(v -> {
            guardarEnFavoritos(nombre, descripcion);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private void guardarEnFavoritos(String nombre, String desc) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        Map<String, Object> favorito = new HashMap<>();
        favorito.put("nombre", nombre);
        favorito.put("descripcion", desc);
        favorito.put("fecha_guardado", com.google.firebase.Timestamp.now());

        db.collection("usuarios").document(uid)
                .collection("talleres_favoritos")
                .add(favorito)
                .addOnSuccessListener(dr -> Toast.makeText(this, "⭐ Guardado en favoritos", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activarCapaUbicacion();
            } else {
                Toast.makeText(this, "Se requiere permiso de ubicación para mostrar el mapa real", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
        if (mLocationOverlay != null) mLocationOverlay.enableMyLocation();
    }

    @Override public void onPause() {
        super.onPause();
        if (map != null) map.onPause();
        if (mLocationOverlay != null) mLocationOverlay.disableMyLocation();
    }
}