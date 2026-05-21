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

    // Variables de contexto
    private String idVehiculo, idUsuario, placaVehiculo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        establecerContenido(R.layout.activity_mis_combustibles);

        db = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            idUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // --- CAMBIO AQUÍ: Recibimos tanto el ID como la PLACA opcionalmente ---
        idVehiculo = getIntent().getStringExtra("idVehiculo");
        placaVehiculo = getIntent().getStringExtra("placa"); // Para el título

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);

            // UX: Si tenemos la placa ("BGT984"), la mostramos. Si no, mostramos el ID o el título general.
            String identificador = (placaVehiculo != null) ? placaVehiculo : idVehiculo;
            String titulo = (idVehiculo != null) ? "Gasolina: " + identificador : "Gastos de Combustible";

            getSupportActionBar().setTitle(titulo);

            androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

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
                intent.putExtra("placa", placaVehiculo); // Pasamos la placa al registro también
                startActivity(intent);
            });
        }

        obtenerGastosFirebase();
    }

    private void obtenerGastosFirebase() {
        if (idUsuario == null) return;

        Query query;

        // El filtro sigue funcionando por ID de vehículo (que es el campo estable en la DB)
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
                    try {
                        Combustible c = doc.toObject(Combustible.class);
                        c.setId_gasto_combustible(doc.getId());

                        // Si por alguna razón el objeto no traía la placa pero la tenemos en la actividad, se la ponemos
                        if (c.getPlaca() == null && idVehiculo != null && idVehiculo.equals(c.getId_vehiculo())) {
                            c.setPlaca(placaVehiculo);
                        }

                        listaCombustible.add(c);
                    } catch (Exception e) {
                        Log.e("ERROR_DATOS", "ID: " + doc.getId() + " malo.");
                        Combustible errorDoc = new Combustible();
                        errorDoc.setId_vehiculo("Error en datos");
                        listaCombustible.add(errorDoc);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}