package com.example.vehi_track;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vehi_track.adapters.CombustibleAdapter;
import com.example.vehi_track.models.Combustible;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ListarCombustibleActivity extends BaseActivity {

    private RecyclerView rvCombustible;
    private CombustibleAdapter adapter;
    private List<Combustible> listaCombustible;
    private FirebaseFirestore db;
    private String idVehiculo, idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inflamos usando el contenedor de la BaseActivity
        establecerContenido(R.layout.activity_mis_combustibles);

        db = FirebaseFirestore.getInstance();

        // Obtenemos el UID del usuario logueado
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            idUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // Intentamos obtener el idVehiculo (si viene de una moto específica)
        idVehiculo = getIntent().getStringExtra("idVehiculo");

        // 2. Configurar Toolbar y Menú Lateral
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            // Título dinámico según el contexto
            String titulo = (idVehiculo != null) ? "Gasolina: " + idVehiculo : "Gastos de Combustible";
            getSupportActionBar().setTitle(titulo);

            androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        // 3. Configurar RecyclerView y FAB
        rvCombustible = findViewById(R.id.rvCombustible);
        if (rvCombustible != null) {
            rvCombustible.setLayoutManager(new LinearLayoutManager(this));
            listaCombustible = new ArrayList<>();
            adapter = new CombustibleAdapter(listaCombustible);
            rvCombustible.setAdapter(adapter);
        }

        FloatingActionButton fab = findViewById(R.id.fabAddCombustible);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                Intent intent = new Intent(this, RegistrarCombustibleActivity.class);
                intent.putExtra("idVehiculo", idVehiculo);
                startActivity(intent);
            });
        }

        obtenerGastosFirebase();
    }

    private void obtenerGastosFirebase() {
        if (idUsuario == null) return;

        Query query;

        // Filtro Inteligente: Por moto o por usuario total
        if (idVehiculo != null && !idVehiculo.isEmpty()) {
            query = db.collection("combustible")
                    .whereEqualTo("id_vehiculo", idVehiculo)
                    .orderBy("fecha", Query.Direction.DESCENDING);
        } else {
            query = db.collection("combustible")
                    .whereEqualTo("id_usuario", idUsuario)
                    .orderBy("fecha", Query.Direction.DESCENDING);
        }

        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("FIRESTORE", "Error al cargar combustible: " + error.getMessage());
                return;
            }

            if (value != null) {
                listaCombustible.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Combustible c = doc.toObject(Combustible.class);
                    // Guardamos el ID del documento para que el Adapter pueda manejarlo
                    c.setId_gasto_combustible(doc.getId());
                    listaCombustible.add(c);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}