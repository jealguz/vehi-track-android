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

/**
 * Actividad encargada de gestionar la visualización del historial técnico.
 * Utiliza Firebase Firestore para recuperar mantenimientos preventivos y correctivos,
 * permitiendo un seguimiento detallado del estado de cada vehículo.
 */
public class ListarMantenimientosActivity extends BaseActivity {

    // Componentes para la gestión de la lista dinámica
    private RecyclerView rvMantos;
    private MantenimientoAdapter adapter;
    private List<Mantenimiento> listaMantos;

    // Servicios de base de datos en la nube
    private FirebaseFirestore db;

    // Identificadores para segmentación de datos
    private String idVehiculo, idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. INFLADO CON ARQUITECTURA DE HERENCIA
        // Inyecta el layout dentro del contenedor de BaseActivity para heredar el menú lateral.
        establecerContenido(R.layout.activity_mis_mantenimientos);

        // Inicialización de la base de datos Firestore
        db = FirebaseFirestore.getInstance();

        // Validación de sesión activa para recuperar el UID del usuario
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            idUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // 2. RECUPERACIÓN DE CONTEXTO (NAVEGACIÓN)
        // Se intenta capturar el ID del vehículo (placa) si la navegación proviene de una moto específica.
        idVehiculo = getIntent().getStringExtra("idVehiculo");

        // 3. CONFIGURACIÓN ESTRUCTURAL DE LA INTERFAZ (TOOLBAR)
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);

            // TÍTULO DINÁMICO (UX): Personaliza la experiencia según el origen del usuario.
            String titulo = (idVehiculo != null) ? "Historial: " + idVehiculo : "Mis Mantenimientos";
            getSupportActionBar().setTitle(titulo);

            // Sincronización del menú lateral con la barra de herramientas.
            androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        // 4. INICIALIZACIÓN DEL RECYCLERVIEW Y COMPONENTES DE ACCIÓN
        rvMantos = findViewById(R.id.rvMantenimientos);
        FloatingActionButton fab = findViewById(R.id.fabAddManto);

        if (rvMantos != null) {
            // Se define un Administrador de Diseño Lineal para el listado vertical.
            rvMantos.setLayoutManager(new LinearLayoutManager(this));
            listaMantos = new ArrayList<>();
            adapter = new MantenimientoAdapter(listaMantos);
            rvMantos.setAdapter(adapter);
        }

        // ACCIÓN PARA REGISTRAR NUEVO MANTENIMIENTO (FAB)
        if (fab != null) {
            fab.setOnClickListener(v -> {
                Intent intent = new Intent(this, RegistrarMantenimientoActivity.class);
                // Se transfiere el ID del vehículo actual para simplificar el formulario de registro.
                intent.putExtra("idVehiculo", idVehiculo);
                startActivity(intent);
            });
        }

        // Inicio del monitoreo de datos en tiempo real
        escucharCambiosFirebase();
    }

    /**
     * Gestión Reactiva de Datos con Firestore:
     * Configura escuchadores (Listeners) que detectan cambios en la base de datos
     * y actualizan la interfaz automáticamente sin recargar la pantalla.
     */
    private void escucharCambiosFirebase() {
        if (idUsuario == null) return;

        Query query;

        // --- LÓGICA DE FILTRADO INTELIGENTE ---
        // Se construye la consulta según el alcance requerido (Vehículo único o Flota total).
        if (idVehiculo != null && !idVehiculo.isEmpty()) {
            // Filtrado por placa específica del vehículo.
            query = db.collection("mantenimientos")
                    .whereEqualTo("id_vehiculo", idVehiculo)
                    .orderBy("fecha_realizacion", Query.Direction.DESCENDING);
        } else {
            // Filtrado global por cuenta de usuario.
            query = db.collection("mantenimientos")
                    .whereEqualTo("id_usuario", idUsuario)
                    .orderBy("fecha_realizacion", Query.Direction.DESCENDING);
        }

        // Suscripción a cambios en la colección de Firestore
        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                // Registro de errores técnicos en el Logcat para depuración (Debugging)
                Log.e("FIRESTORE", "Error al cargar mantenimientos: " + error.getMessage());
                return;
            }

            if (value != null) {
                listaMantos.clear(); // Se limpia el buffer de la lista para evitar solapamientos
                for (QueryDocumentSnapshot doc : value) {
                    // MAPEO DE DATOS: Conversión del documento NoSQL a Objeto POJO (Plain Old Java Object)
                    Mantenimiento m = doc.toObject(Mantenimiento.class);

                    // SEGURIDAD DE DATOS: Se asocia el ID del documento para permitir futuras ediciones.
                    m.setId_mantenimiento(doc.getId());
                    listaMantos.add(m);
                }
                // Sincronización del Adaptador con la nueva data
                adapter.notifyDataSetChanged();
            }
        });
    }
}