package com.example.vehi_track;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vehi_track.adapters.VehiculoAdapter;
import com.example.vehi_track.models.vehiculo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class MisVehiculosActivity extends BaseActivity {

    private RecyclerView rvVehiculos;
    private VehiculoAdapter adapter;
    private List<vehiculo> lista;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView tvContador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Usar el contenedor de la BaseActivity
        establecerContenido(R.layout.activity_mis_vehiculos);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // 2. Configurar la Toolbar para que aparezca el menú lateral
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Mis Vehículos");

            androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        // Inicializar UI
        tvContador = findViewById(R.id.tvContador);
        rvVehiculos = findViewById(R.id.rvListaCompleta);
        if (rvVehiculos != null) {
            rvVehiculos.setLayoutManager(new LinearLayoutManager(this));
            lista = new ArrayList<>();
            // Pasamos el contexto para que el adapter pueda manejar clics si es necesario
            adapter = new VehiculoAdapter(lista);
            rvVehiculos.setAdapter(adapter);
        }

        // Botón agregar uno nuevo (Mantenemos tu lógica del FAB)
        if (findViewById(R.id.fabNuevoDesdeLista) != null) {
            findViewById(R.id.fabNuevoDesdeLista).setOnClickListener(v -> {
                startActivity(new Intent(this, RegistrarVehiculoActivity.class));
            });
        }

        obtenerVehiculos();
    }

    private void obtenerVehiculos() {
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("vehiculos")
                .whereEqualTo("id_usuario", uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("FIREBASE", "Error al obtener datos: " + error.getMessage());
                        return;
                    }

                    if (value != null) {
                        lista.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            vehiculo v = doc.toObject(vehiculo.class);
                            if (v != null) {
                                // Es vital guardar el ID para poder editar la moto después
                                v.setId_vehiculo(doc.getId());
                                lista.add(v);
                            }
                        }

                        adapter.notifyDataSetChanged();

                        if (tvContador != null) {
                            tvContador.setText("Tienes " + lista.size() + " vehículo(s) registrados");
                        }
                    }
                });
    }
}