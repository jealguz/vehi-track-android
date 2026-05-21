package com.example.vehi_track;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

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
 * Módulo de Geolocalización: Talleres Cercanos y Favoritos.
 * @author Jeison Guzman
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

        // Configuración OSMDroid
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        establecerContenido(R.layout.activity_talleres_cercanos);
        db = FirebaseFirestore.getInstance();

        // 1. CONFIGURACIÓN DE TOOLBAR
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
            verificarPermisosYConfigurarGPS();
            simularCargaDeTalleres();
        }

        // 3. BOTÓN PARA VER FAVORITOS (CORREGIDO)
        FloatingActionButton fabFavs = findViewById(R.id.fabVerFavoritos);
        if (fabFavs != null) {
            fabFavs.setOnClickListener(v -> mostrarModalFavoritos());
        }
    } // CIERRE DE ONCREATE

    // --- MÉTODOS DE LÓGICA DE FAVORITOS ---

    private void mostrarModalFavoritos() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_lista_favoritos, null);
        LinearLayout container = view.findViewById(R.id.containerFavoritos);

        // Consulta a la sub-colección de favoritos
        db.collection("usuarios").document(uid)
                .collection("talleres_favoritos")
                .orderBy("fecha_guardado", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    container.removeAllViews();

                    if (queryDocumentSnapshots.isEmpty()) {
                        TextView tvEmpty = new TextView(this);
                        tvEmpty.setText("Aún no tienes talleres favoritos.");
                        tvEmpty.setPadding(20, 40, 20, 40);
                        tvEmpty.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        container.addView(tvEmpty);
                    }

                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        View itemView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);
                        TextView text1 = itemView.findViewById(android.R.id.text1);
                        TextView text2 = itemView.findViewById(android.R.id.text2);

                        text1.setText(doc.getString("nombre"));
                        text2.setText(doc.getString("descripcion"));
                        text1.setTextColor(Color.BLACK);
                        text2.setTextColor(Color.GRAY);

                        container.addView(itemView);
                    }
                });

        dialog.setContentView(view);
        dialog.show();
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

    // --- MÉTODOS DE GEOLOCALIZACIÓN ---

    private void verificarPermisosYConfigurarGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            activarCapaUbicacion();
        }
    }

    private void activarCapaUbicacion() {
        this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        this.mLocationOverlay.enableMyLocation();
        this.mLocationOverlay.enableFollowLocation();

        this.mLocationOverlay.runOnFirstFix(() -> {
            runOnUiThread(() -> {
                if (mLocationOverlay.getMyLocation() != null) {
                    map.getController().animateTo(mLocationOverlay.getMyLocation());
                    map.getController().setZoom(16.5);
                    cargarTalleresCercanosReales(mLocationOverlay.getMyLocation().getLatitude(),
                            mLocationOverlay.getMyLocation().getLongitude());
                }
            });
        });

        map.getOverlays().add(this.mLocationOverlay);
    }

    private void cargarTalleresCercanosReales(double lat, double lon) {
        if (layoutLoading != null) layoutLoading.setVisibility(View.GONE);

        // Simulamos puntos cercanos a la ubicación real del usuario
        agregarMarcador(lat + 0.0015, lon + 0.0015, "Taller Principal El Amigo", "Especialista en Motos");
        agregarMarcador(lat - 0.0015, lon - 0.0015, "Motos del Sur", "Repuestos y Accesorios");
        agregarMarcador(lat + 0.0020, lon - 0.0010, "Lava-Motos Express", "Limpieza y Brillo");

        map.setVisibility(View.VISIBLE);
        map.invalidate();
        Toast.makeText(this, "Talleres sincronizados en tu zona", Toast.LENGTH_SHORT).show();
    }

    private void simularCargaDeTalleres() {
        if (layoutLoading != null) layoutLoading.setVisibility(View.VISIBLE);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activarCapaUbicacion();
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override public void onResume() { super.onResume(); if (map != null) map.onResume(); }
    @Override public void onPause() { super.onPause(); if (map != null) map.onPause(); }
}