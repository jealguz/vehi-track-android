package com.example.vehi_track;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.HashMap;
import java.util.Map;

public class TalleresCercanosActivity extends BaseActivity {

    private MapView map;
    private LinearLayout layoutLoading; // Referencia al contenedor completo
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- SOLUCIÓN MAPA BLANCO: CONFIGURACIÓN ANTES DE INFLAR ---
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        establecerContenido(R.layout.activity_talleres_cercanos);
        db = FirebaseFirestore.getInstance();

        // 1. Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Talleres en Guavatá");
            androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        // 2. Referencia al Layout de carga
        layoutLoading = findViewById(R.id.layoutLoading);
        map = findViewById(R.id.map);

        if (map != null) {
            map.setMultiTouchControls(true);
            map.setTileSource(TileSourceFactory.MAPNIK); // Forzamos fuente de mapas estándar
            map.setVisibility(View.INVISIBLE);

            simularCargaDeTalleres();
        }
    }

    private void simularCargaDeTalleres() {
        if (layoutLoading != null) layoutLoading.setVisibility(View.VISIBLE);

        new Handler().postDelayed(() -> {
            // Ocultamos TODO el bloque de carga (ruedita y fondo blanco)
            if (layoutLoading != null) layoutLoading.setVisibility(View.GONE);

            // Centrar Mapa en Guavatá
            GeoPoint guavataCenter = new GeoPoint(5.9922, -73.6744);
            map.getController().setZoom(17.0); // Un poco más cerca
            map.getController().setCenter(guavataCenter);

            // 3. Agregar Marcadores
            agregarMarcador(5.9922, -73.6744, "Taller Principal Guavatá", "Especialista en Motos");
            agregarMarcador(5.9935, -73.6750, "Motos del Sur", "Repuestos y Accesorios");
            agregarMarcador(5.9910, -73.6730, "Lava-Motos Express", "Limpieza y Brillo");

            map.setVisibility(View.VISIBLE);
            map.invalidate(); // Forzamos redibujado

            Toast.makeText(this, "Talleres sincronizados", Toast.LENGTH_SHORT).show();
        }, 2500); // 2.5 segundos para que se aprecie el "cargando"
    }

    private void agregarMarcador(double lat, double lon, String titulo, String snippet) {
        if (map == null) return;

        Marker nodeMarker = new Marker(map);
        nodeMarker.setPosition(new GeoPoint(lat, lon));
        nodeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        nodeMarker.setTitle(titulo);
        nodeMarker.setSnippet(snippet);

        // Clic para Favoritos
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
                .addOnSuccessListener(dr -> Toast.makeText(this, "⭐ Guardado en favoritos", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show());
    }

    @Override public void onResume() { super.onResume(); if (map != null) map.onResume(); }
    @Override public void onPause() { super.onPause(); if (map != null) map.onPause(); }
}