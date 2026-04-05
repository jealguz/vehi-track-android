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

/**
 * Actividad destinada a la gestión del inventario vehicular del usuario.
 * Implementa un listado dinámico mediante RecyclerView que se sincroniza
 * en tiempo real con la base de datos NoSQL de Firestore.
 */
public class MisVehiculosActivity extends BaseActivity {

    // Componentes para la visualización de la lista
    private RecyclerView rvVehiculos;
    private VehiculoAdapter adapter;
    private List<vehiculo> lista;

    // Servicios de Backend
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Indicador visual de cantidad de registros
    private TextView tvContador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. ARQUITECTURA DE CONTENEDOR:
        // Inyecta el layout 'activity_mis_vehiculos' dentro del frame de la BaseActivity
        // para garantizar que el menú de hamburguesa esté presente.
        establecerContenido(R.layout.activity_mis_vehiculos);

        // Inicialización de las instancias de Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // 2. CONFIGURACIÓN DE LA TOOLBAR Y NAVEGACIÓN
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Mis Vehículos");

            // Configuración del Toggle para el DrawerLayout (Menú Lateral)
            androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        // 3. INICIALIZACIÓN DE COMPONENTES DE INTERFAZ (UI)
        tvContador = findViewById(R.id.tvContador);
        rvVehiculos = findViewById(R.id.rvListaCompleta);

        if (rvVehiculos != null) {
            // Se utiliza un LinearLayoutManager para un scroll vertical estándar
            rvVehiculos.setLayoutManager(new LinearLayoutManager(this));
            lista = new ArrayList<>();
            // El adaptador vincula los datos de la lista con el diseño de las tarjetas
            adapter = new VehiculoAdapter(lista);
            rvVehiculos.setAdapter(adapter);
        }

        // 4. ACCIÓN DE REGISTRO (Floating Action Button)
        // Permite al usuario navegar al formulario de creación de nuevos vehículos.
        if (findViewById(R.id.fabNuevoDesdeLista) != null) {
            findViewById(R.id.fabNuevoDesdeLista).setOnClickListener(v -> {
                startActivity(new Intent(this, RegistrarVehiculoActivity.class));
            });
        }

        // Inicio de la sincronización de datos con la nube
        obtenerVehiculos();
    }

    /**
     * Sincronización en Tiempo Real:
     * Utiliza un SnapshotListener para filtrar vehículos que pertenezcan únicamente
     * al usuario autenticado (id_usuario == uid).
     */
    private void obtenerVehiculos() {
        // Validación de sesión activa antes de realizar la consulta
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        // Consulta a la colección 'vehiculos' filtrada por el UID del propietario
        db.collection("vehiculos")
                .whereEqualTo("id_usuario", uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Gestión de errores en la consola de depuración
                        Log.e("FIREBASE", "Error al obtener datos: " + error.getMessage());
                        return;
                    }

                    if (value != null) {
                        lista.clear(); // Se limpia la lista para refrescar el contenido sin duplicar

                        // Iteración sobre los documentos recuperados de Firestore
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            vehiculo v = doc.toObject(vehiculo.class);
                            if (v != null) {
                                // ASIGNACIÓN CRÍTICA: Se captura el ID autogenerado del documento.
                                // Esto es vital para poder realizar operaciones de edición o borrado.
                                v.setId_vehiculo(doc.getId());
                                lista.add(v);
                            }
                        }

                        // Notificación al Adaptador para redibujar el RecyclerView con los nuevos datos
                        adapter.notifyDataSetChanged();

                        // Actualización del contador dinámico de la interfaz
                        if (tvContador != null) {
                            tvContador.setText("Tienes " + lista.size() + " vehículo(s) registrados");
                        }
                    }
                });
    }
}