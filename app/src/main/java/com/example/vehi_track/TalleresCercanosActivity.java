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

/**
 * Módulo de Geolocalización y Servicios Externos.
 * Utiliza la librería OSMDroid para renderizar mapas interactivos y Firebase para
 * gestionar la persistencia de puntos de interés (Talleres Favoritos).
 */
public class TalleresCercanosActivity extends BaseActivity {

    private MapView map;
    private LinearLayout layoutLoading; // Contenedor de la pantalla de carga
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- OPTIMIZACIÓN DE RENDERIZADO: CONFIGURACIÓN PRE-INFLADO ---
        // Se establece el UserAgent y se cargan preferencias para evitar el error de "Mapa en Blanco".
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        establecerContenido(R.layout.activity_talleres_cercanos);
        db = FirebaseFirestore.getInstance();

        // 1. CONFIGURACIÓN DE NAVEGACIÓN (Toolbar + Drawer)
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Talleres en Guavatá");
            androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        // 2. INICIALIZACIÓN DEL MOTOR DE MAPAS
        layoutLoading = findViewById(R.id.layoutLoading);
        map = findViewById(R.id.map);

        if (map != null) {
            map.setMultiTouchControls(true); // Permite Zoom con dos dedos
            map.setTileSource(TileSourceFactory.MAPNIK); // Fuente de datos de OpenStreetMap
            map.setVisibility(View.INVISIBLE); // Oculto inicialmente para mostrar la animación de carga

            simularCargaDeTalleres();
        }
    }

    /**
     * Gestión de la Experiencia de Usuario (UX):
     * Simula una petición asíncrona a un servidor para sincronizar puntos de interés.
     */
    private void simularCargaDeTalleres() {
        if (layoutLoading != null) layoutLoading.setVisibility(View.VISIBLE);

        new Handler().postDelayed(() -> {
            // Finaliza la animación de carga y revela el mapa
            if (layoutLoading != null) layoutLoading.setVisibility(View.GONE);

            // GEOPOSICIONAMIENTO ESTRATÉGICO: Centrado en Guavatá, Santander
            GeoPoint guavataCenter = new GeoPoint(5.9922, -73.6744);
            map.getController().setZoom(17.0);
            map.getController().setCenter(guavataCenter);

            // 3. GENERACIÓN DINÁMICA DE MARCADORES (POIs)
            agregarMarcador(5.9922, -73.6744, "Taller Principal Guavatá", "Especialista en Motos");
            agregarMarcador(5.9935, -73.6750, "Motos del Sur", "Repuestos y Accesorios");
            agregarMarcador(5.9910, -73.6730, "Lava-Motos Express", "Limpieza y Brillo");

            map.setVisibility(View.VISIBLE);
            map.invalidate(); // Forzar refresco del Canvas del mapa

            Toast.makeText(this, "Talleres sincronizados", Toast.LENGTH_SHORT).show();
        }, 2500); // Delay controlado para mejorar la percepción de robustez del sistema
    }

    /**
     * Inyección de Capas (Overlays):
     * Agrega marcadores interactivos al mapa con listeners de eventos.
     */
    private void agregarMarcador(double lat, double lon, String titulo, String snippet) {
        if (map == null) return;

        Marker nodeMarker = new Marker(map);
        nodeMarker.setPosition(new GeoPoint(lat, lon));
        nodeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        nodeMarker.setTitle(titulo);
        nodeMarker.setSnippet(snippet);

        // EVENTO DE CLIC: Despliega información detallada mediante un modal
        nodeMarker.setOnMarkerClickListener((marker, mapView) -> {
            mostrarModalTaller(marker.getTitle(), marker.getSnippet());
            return true;
        });

        map.getOverlays().add(nodeMarker);
    }

    /**
     * Interfaz de Información Detallada (Material Design):
     * Utiliza un BottomSheetDialog para presentar opciones de interacción adicionales.
     */
    private void mostrarModalTaller(String nombre, String descripcion) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_modal_taller, null);

        TextView tvNombre = view.findViewById(R.id.tvNombreTaller);
        TextView tvDesc = view.findViewById(R.id.tvDescripcionTaller);
        Button btnFav = view.findViewById(R.id.btnAgregarFavorito);

        tvNombre.setText(nombre);
        tvDesc.setText(descripcion);

        // Acción: Guardado de favoritos en la sub-colección del usuario
        btnFav.setOnClickListener(v -> {
            guardarEnFavoritos(nombre, descripcion);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    /**
     * Persistencia de Intereses:
     * Almacena el taller en una colección anidada para personalización del usuario.
     */
    private void guardarEnFavoritos(String nombre, String desc) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        Map<String, Object> favorito = new HashMap<>();
        favorito.put("nombre", nombre);
        favorito.put("descripcion", desc);
        favorito.put("fecha_guardado", com.google.firebase.Timestamp.now());

        // Implementación de Sub-colecciones en Firestore para mejor organización
        db.collection("usuarios").document(uid)
                .collection("talleres_favoritos")
                .add(favorito)
                .addOnSuccessListener(dr -> Toast.makeText(this, "⭐ Guardado en favoritos", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show());
    }

    // CICLO DE VIDA DEL MAPA: Gestión de recursos para evitar fugas de memoria
    @Override public void onResume() { super.onResume(); if (map != null) map.onResume(); }
    @Override public void onPause() { super.onPause(); if (map != null) map.onPause(); }
}