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

/**
 * Actividad encargada de visualizar el historial de gastos de combustible.
 * Implementa filtros dinámicos para mostrar registros por vehículo específico o globales por usuario.
 */
public class ListarCombustibleActivity extends BaseActivity {

    // Componentes de la interfaz de usuario
    private RecyclerView rvCombustible;
    private CombustibleAdapter adapter;
    private List<Combustible> listaCombustible;

    // Instancia de la base de datos Firestore
    private FirebaseFirestore db;

    // Variables de contexto para filtrado de datos
    private String idVehiculo, idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. INFLADO CON HERENCIA
        // Se inyecta el diseño en el contenedor principal de la BaseActivity para mantener el menú lateral.
        establecerContenido(R.layout.activity_mis_combustibles);

        db = FirebaseFirestore.getInstance();

        // SEGURIDAD: Se obtiene el identificador único del usuario logueado actualmente.
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            idUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // CONTEXTO DE NAVEGACIÓN:
        // Intentamos obtener el ID de una moto específica si el usuario viene de la pantalla "Detalles de Moto".
        idVehiculo = getIntent().getStringExtra("idVehiculo");

        // 2. CONFIGURACIÓN DE TOOLBAR Y NAVEGACIÓN
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);

            // TÍTULO DINÁMICO (UX):
            // Si el usuario filtra por una moto, el título cambia para mostrar la placa de esa moto.
            String titulo = (idVehiculo != null) ? "Gasolina: " + idVehiculo : "Gastos de Combustible";
            getSupportActionBar().setTitle(titulo);

            // Sincronización con el DrawerLayout (menú de hamburguesa) definido en la BaseActivity.
            androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        // 3. CONFIGURACIÓN DEL RECYCLERVIEW (LISTADO)
        rvCombustible = findViewById(R.id.rvCombustible);
        if (rvCombustible != null) {
            // Diseño lineal para el historial (uno debajo del otro)
            rvCombustible.setLayoutManager(new LinearLayoutManager(this));
            listaCombustible = new ArrayList<>();
            adapter = new CombustibleAdapter(listaCombustible);
            rvCombustible.setAdapter(adapter);
        }

        // 4. ACCIÓN DE REGISTRO (FAB)
        // El Floating Action Button permite ir a la pantalla de agregar nuevo tanqueo.
        FloatingActionButton fab = findViewById(R.id.fabAddCombustible);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                Intent intent = new Intent(this, RegistrarCombustibleActivity.class);
                // Si ya estamos filtrando por una moto, pasamos ese ID para facilitar el registro.
                intent.putExtra("idVehiculo", idVehiculo);
                startActivity(intent);
            });
        }

        // Inicio de la carga de datos desde la nube
        obtenerGastosFirebase();
    }

    /**
     * Consulta Reactiva a Firestore:
     * Recupera los registros de combustible ordenados por fecha de forma descendente (más recientes primero).
     */
    private void obtenerGastosFirebase() {
        if (idUsuario == null) return;

        Query query;

        // --- LÓGICA DE FILTRO INTELIGENTE ---
        // Se decide la consulta según si el usuario quiere ver los gastos de una sola moto o de toda su cuenta.
        if (idVehiculo != null && !idVehiculo.isEmpty()) {
            // Filtro por placa específica
            query = db.collection("combustible")
                    .whereEqualTo("id_vehiculo", idVehiculo)
                    .orderBy("fecha", Query.Direction.DESCENDING);
        } else {
            // Filtro global por usuario
            query = db.collection("combustible")
                    .whereEqualTo("id_usuario", idUsuario)
                    .orderBy("fecha", Query.Direction.DESCENDING);
        }

        // SnapshotListener: La lista se actualiza automáticamente si se agrega un gasto desde otro lugar.
        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("FIRESTORE", "Error al cargar combustible: " + error.getMessage());
                return;
            }

            if (value != null) {
                listaCombustible.clear(); // Se limpia la lista local para evitar duplicados
                for (QueryDocumentSnapshot doc : value) {
                    // Mapeo automático del documento de Firestore a la clase modelo Combustible
                    Combustible c = doc.toObject(Combustible.class);

                    // Se captura el ID autogenerado de Firebase para futuras ediciones o eliminaciones
                    c.setId_gasto_combustible(doc.getId());
                    listaCombustible.add(c);
                }
                // Notifica al adaptador que los datos cambiaron para refrescar la interfaz
                adapter.notifyDataSetChanged();
            }
        });
    }
}