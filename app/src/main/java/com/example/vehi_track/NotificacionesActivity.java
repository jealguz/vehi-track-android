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

/**
 * Centro de Alertas Inteligente.
 * Consolida tres fuentes: Notificaciones manuales, Mantenimientos pendientes
 * y el Monitor Automático de Documentos Legales (SOAT/Tecno).
 */
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
        establecerContenido(R.layout.activity_notificaciones);

        db = FirebaseFirestore.getInstance();
        idUsuario = FirebaseAuth.getInstance().getUid();

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Mis Alertas y Pendientes");
            }
        }

        rvNotif = findViewById(R.id.rvNotificaciones);
        rvNotif.setLayoutManager(new LinearLayoutManager(this));
        listaNotificaciones = new ArrayList<>();

        adapter = new NotificacionAdapter(listaNotificaciones, this);
        rvNotif.setAdapter(adapter);

        // Iniciamos la carga de las 3 fuentes de datos
        cargarDatosCombinados();
    }

    /**
     * Carga sincronizada desde 3 colecciones distintas de Firestore.
     */
    private void cargarDatosCombinados() {
        if (idUsuario == null) return;

        // 1. LEER TODO DEL HISTORIAL (Aquí ya llegan SOAT y RTM desde el Dashboard)
        db.collection("notificaciones")
                .whereEqualTo("id_usuario", idUsuario)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    actualizarListaSimple(value, "NOTIF_RAIZ");
                });

        // 2. MANTENIMIENTOS (Para que salgan los que el mecánico no ha cerrado)
        db.collection("mantenimientos")
                .whereEqualTo("id_usuario", idUsuario)
                .whereEqualTo("fecha_realizacion", null)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    actualizarListaSimple(value, "MANTO");
                });
    }

    private synchronized void actualizarListaSimple(com.google.firebase.firestore.QuerySnapshot value, String tipo) {
        if (value == null) return;

        // Limpiamos según el tipo para evitar duplicados al actualizar en tiempo real
        if (tipo.equals("NOTIF_RAIZ")) {
            listaNotificaciones.removeIf(n -> !n.getTitulo().startsWith("Mantenimiento:"));
        } else {
            listaNotificaciones.removeIf(n -> n.getTitulo().startsWith("Mantenimiento:"));
        }

        for (QueryDocumentSnapshot doc : value) {
            if (tipo.equals("NOTIF_RAIZ")) {
                Notificacion n = doc.toObject(Notificacion.class);
                n.setId(doc.getId());
                listaNotificaciones.add(n);
            } else {
                // Mantenimientos técnicos
                String servicio = doc.getString("servicio");
                String placa = doc.getString("placa");
                com.google.firebase.Timestamp fecha = doc.getTimestamp("fecha_programada");

                Notificacion n = new Notificacion(
                        doc.getId(),
                        "Mantenimiento: " + (servicio != null ? servicio : "Revisión"),
                        "Pendiente para placa: " + placa,
                        fecha,
                        placa
                );
                listaNotificaciones.add(n);
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Monitor de fechas legales.
     * Solo extrae el Timestamp y crea el objeto.
     * El Adapter se encargará de calcular si faltan 15, 7 o si ya venció.
     */
    private void revisarFechaDocumento(QueryDocumentSnapshot doc, String campo, String nombreDoc, String placa, long hoy, long margen) {
        // 1. Verificamos que el campo exista en el vehículo
        if (doc.contains(campo) && doc.get(campo) != null) {

            com.google.firebase.Timestamp fechaDoc = doc.getTimestamp(campo);

            if (fechaDoc != null) {
                long fechaMs = fechaDoc.toDate().getTime();
                long diferencia = fechaMs - hoy;

                // 2. Solo agregamos a la lista si falta el margen de 15 días o ya venció
                if (diferencia <= margen) {

                    // IMPORTANTE: Según tu Modelo Notificacion(id, titulo, mensaje, fecha, idVehiculo)
                    // Pasamos la placa en el campo 'idVehiculo' para que el Adapter la muestre.
                    Notificacion n = new Notificacion(
                            doc.getId(),                    // id
                            "Vencimiento de " + nombreDoc,  // titulo
                            "Revisión requerida para el vehículo", // mensaje base
                            fechaDoc,                       // fecha (TIPO TIMESTAMP)
                            placa                           // Pasamos la placa aquí
                    );

                    listaNotificaciones.add(n);
                }
            }
        }
    }

    @Override
    public void onNotificacionClick(Notificacion notif) {
        try {
            ModalActualizarFecha modal = new ModalActualizarFecha(notif);
            modal.show(getSupportFragmentManager(), "ModalActualizarFecha");
        } catch (Exception e) {
            Toast.makeText(this, "Error al abrir el editor", Toast.LENGTH_SHORT).show();
            Log.e("NOTIF_ERR", e.getMessage());
        }
    }
}