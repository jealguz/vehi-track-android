package com.example.vehi_track;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vehi_track.adapters.NotificacionAdapter;
import com.example.vehi_track.models.Mantenimiento;
import com.example.vehi_track.models.Notificacion;
import com.example.vehi_track.dialogs.ModalActualizarFecha;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificacionesActivity extends BaseActivity
        implements NotificacionAdapter.OnNotificacionClickListener {

    private RecyclerView rvNotif;
    private NotificacionAdapter adapter;
    private List<Notificacion> listaNotificaciones;
    private FirebaseFirestore db;
    private String idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inflar el diseño
        establecerContenido(R.layout.activity_notificaciones);

        db = FirebaseFirestore.getInstance();
        idUsuario = FirebaseAuth.getInstance().getUid();

        // 2. Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Mis Alertas y Pendientes");
            }
        }

        // 3. Configurar RecyclerView
        rvNotif = findViewById(R.id.rvNotificaciones);
        rvNotif.setLayoutManager(new LinearLayoutManager(this));
        listaNotificaciones = new ArrayList<>();

        // 4. Inicializar Adapter
        adapter = new NotificacionAdapter(listaNotificaciones, this);
        rvNotif.setAdapter(adapter);

        // 5. Cargar datos desde Firebase
        cargarDatosCombinados();
    }

    private void cargarDatosCombinados() {
        if (idUsuario == null) return;

        // Escuchar Notificaciones (SOAT / Tecno)
        db.collection("notificaciones")
                .whereEqualTo("id_usuario", idUsuario)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    actualizarLista(value, true);
                });

        // Escuchar Mantenimientos (Solo los que no tienen fecha de realización)
        db.collection("mantenimientos")
                .whereEqualTo("id_usuario", idUsuario)
                .whereEqualTo("fecha_realizacion", null)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    actualizarLista(value, false);
                });
    }

    private synchronized void actualizarLista(com.google.firebase.firestore.QuerySnapshot value, boolean esNotifRaiz) {
        if (value == null) return;

        // Limpieza selectiva para evitar duplicados al combinar flujos
        if (esNotifRaiz) {
            listaNotificaciones.removeIf(n -> !n.getTitulo().startsWith("Mantenimiento:"));
        } else {
            listaNotificaciones.removeIf(n -> n.getTitulo().startsWith("Mantenimiento:"));
        }

        for (QueryDocumentSnapshot doc : value) {
            if (esNotifRaiz) {
                // Mapeo directo para SOAT/Tecno
                Notificacion n = doc.toObject(Notificacion.class);
                n.setId(doc.getId());
                listaNotificaciones.add(n);
            } else {
                // Conversión de Mantenimiento a Notificación para la interfaz
                Mantenimiento m = doc.toObject(Mantenimiento.class);
                Notificacion n = new Notificacion(
                        doc.getId(),
                        "Mantenimiento: " + m.getDescripcion(),
                        "Pendiente por realizar",
                        m.getFecha_programada(),
                        m.getPlaca()
                );
                n.setIdVehiculo(m.getId_vehiculo()); // Importante para el modal
                listaNotificaciones.add(n);
            }
        }

        // Refrescar la vista
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // --- ACCIÓN AL TOCAR UNA ALERTA ---
    @Override
    public void onNotificacionClick(Notificacion notif) {
        // Mostramos el BottomSheetDialog que creamos
        try {
            ModalActualizarFecha modal = new ModalActualizarFecha(notif);
            modal.show(getSupportFragmentManager(), "ModalActualizarFecha");
        } catch (Exception e) {
            Toast.makeText(this, "Error al abrir el editor", Toast.LENGTH_SHORT).show();
            Log.e("NOTIF_ERR", e.getMessage());
        }
    }
}