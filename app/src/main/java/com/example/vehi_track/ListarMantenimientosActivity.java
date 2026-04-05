package com.example.vehi_track;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vehi_track.adapters.MantenimientoAdapter;
import com.example.vehi_track.models.Mantenimiento;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ListarMantenimientosActivity extends BaseActivity {

    private RecyclerView rvMantos;
    private MantenimientoAdapter adapter;
    private List<Mantenimiento> listaMantos;
    private FirebaseFirestore db;
    private String idVehiculo, idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inflar el contenido dentro del contenedor de BaseActivity
        establecerContenido(R.layout.activity_mis_mantenimientos);

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            idUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // 2. Obtener la placa (idVehiculo) si viene de una moto específica
        idVehiculo = getIntent().getStringExtra("idVehiculo");

        // 3. Configurar Toolbar y Menú Lateral
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            // Si viene de una moto específica, mostramos "Historial de [Placa]"
            String titulo = (idVehiculo != null) ? "Historial: " + idVehiculo : "Mis Mantenimientos";
            getSupportActionBar().setTitle(titulo);

            androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        // Inicializar RecyclerView y FAB
        rvMantos = findViewById(R.id.rvMantenimientos);
        FloatingActionButton fab = findViewById(R.id.fabAddManto);

        if (rvMantos != null) {
            rvMantos.setLayoutManager(new LinearLayoutManager(this));
            listaMantos = new ArrayList<>();
            adapter = new MantenimientoAdapter(listaMantos);
            rvMantos.setAdapter(adapter);
        }

        if (fab != null) {
            fab.setOnClickListener(v -> {
                Intent intent = new Intent(this, RegistrarMantenimientoActivity.class);
                intent.putExtra("idVehiculo", idVehiculo);
                startActivity(intent);
            });
        }

        escucharCambiosFirebase();
    }

    private void escucharCambiosFirebase() {
        if (idUsuario == null) return;

        Query query;

        // Filtro inteligente:
        // Si hay idVehiculo, muestra solo los de ese vehículo.
        // Si es null, muestra todos los del usuario (desde el menú lateral).
        if (idVehiculo != null && !idVehiculo.isEmpty()) {
            query = db.collection("mantenimientos")
                    .whereEqualTo("id_vehiculo", idVehiculo)
                    .orderBy("fecha_realizacion", Query.Direction.DESCENDING);
        } else {
            query = db.collection("mantenimientos")
                    .whereEqualTo("id_usuario", idUsuario)
                    .orderBy("fecha_realizacion", Query.Direction.DESCENDING);
        }

        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("FIRESTORE", "Error al cargar mantenimientos: " + error.getMessage());
                return;
            }

            if (value != null) {
                listaMantos.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Mantenimiento m = doc.toObject(Mantenimiento.class);
                    // Importante: Guardar el ID del documento para que el Adapter sepa qué editar
                    m.setId_mantenimiento(doc.getId());
                    listaMantos.add(m);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}