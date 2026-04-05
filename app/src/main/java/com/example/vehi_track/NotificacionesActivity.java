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
 * Centro de Alertas y Notificaciones.
 * Esta actividad consolida en una única interfaz dos fuentes de datos distintas:
 * Documentación legal (SOAT/Tecno) y Mantenimientos mecánicos pendientes.
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

        // 1. INFLADO CON HERENCIA: Mantiene la estructura del menú lateral (BaseActivity)
        establecerContenido(R.layout.activity_notificaciones);

        db = FirebaseFirestore.getInstance();
        idUsuario = FirebaseAuth.getInstance().getUid();

        // 2. CONFIGURACIÓN DE LA BARRA DE HERRAMIENTAS
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Mis Alertas y Pendientes");
            }
        }

        // 3. CONFIGURACIÓN DEL LISTADO (RECYCLERVIEW)
        rvNotif = findViewById(R.id.rvNotificaciones);
        rvNotif.setLayoutManager(new LinearLayoutManager(this));
        listaNotificaciones = new ArrayList<>();

        // 4. INICIALIZACIÓN DEL ADAPTADOR CON ESCUCHADOR DE CLICS
        // Se pasa 'this' como listener para manejar las acciones táctiles en cada alerta.
        adapter = new NotificacionAdapter(listaNotificaciones, this);
        rvNotif.setAdapter(adapter);

        // 5. CARGA SINCRONIZADA DE DATOS
        cargarDatosCombinados();
    }

    /**
     * Gestión Multifuente:
     * Ejecuta dos consultas simultáneas a Firestore para traer alertas legales
     * y tareas técnicas sin realizar, garantizando una vista integral de la moto.
     */
    private void cargarDatosCombinados() {
        if (idUsuario == null) return;

        // FLUJO 1: Alertas de Documentos (SOAT / Tecnomecánica)
        db.collection("notificaciones")
                .whereEqualTo("id_usuario", idUsuario)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    actualizarLista(value, true);
                });

        // FLUJO 2: Alertas de Taller (Mantenimientos que no tienen 'fecha_realizacion')
        db.collection("mantenimientos")
                .whereEqualTo("id_usuario", idUsuario)
                .whereEqualTo("fecha_realizacion", null)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    actualizarLista(value, false);
                });
    }

    /**
     * Normalización de Datos (Data Mapping):
     * Este método sincronizado combina los dos flujos de Firebase en una sola lista.
     * Convierte objetos 'Mantenimiento' en objetos 'Notificacion' para uniformidad visual.
     */
    private synchronized void actualizarLista(com.google.firebase.firestore.QuerySnapshot value, boolean esNotifRaiz) {
        if (value == null) return;

        // LIMPIEZA SELECTIVA: Elimina registros previos según el flujo para evitar duplicidad
        // al recibir actualizaciones en tiempo real de cualquiera de las dos colecciones.
        if (esNotifRaiz) {
            listaNotificaciones.removeIf(n -> !n.getTitulo().startsWith("Mantenimiento:"));
        } else {
            listaNotificaciones.removeIf(n -> n.getTitulo().startsWith("Mantenimiento:"));
        }

        for (QueryDocumentSnapshot doc : value) {
            if (esNotifRaiz) {
                // Conversión directa para alertas de documentos
                Notificacion n = doc.toObject(Notificacion.class);
                n.setId(doc.getId());
                listaNotificaciones.add(n);
            } else {
                // ADAPTACIÓN DE MODELO: Transforma un Mantenimiento técnico en una Notificación visual
                Mantenimiento m = doc.toObject(Mantenimiento.class);
                Notificacion n = new Notificacion(
                        doc.getId(),
                        "Mantenimiento: " + m.getDescripcion(),
                        "Pendiente por realizar",
                        m.getFecha_programada(),
                        m.getPlaca()
                );
                n.setIdVehiculo(m.getId_vehiculo()); // Vinculación necesaria para el modal de edición
                listaNotificaciones.add(n);
            }
        }

        // Sincronización final con la interfaz de usuario
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Interacción con el Usuario:
     * Al tocar una alerta, se despliega el BottomSheetDialog (Modal) para
     * actualizar fechas o confirmar la realización de un mantenimiento.
     */
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