package com.example.vehi_track.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.vehi_track.R;
import com.example.vehi_track.models.Notificacion;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

/**
 * Diálogo de tipo BottomSheet para la gestión ágil de fechas.
 * Permite al usuario renovar documentos (SOAT/Tecno) o confirmar mantenimientos
 * mediante una interfaz táctil optimizada.
 */
public class ModalActualizarFecha extends BottomSheetDialogFragment {

    private Notificacion notificacion;
    private long fechaSeleccionada;

    // Constructor que recibe el objeto Notificación para conocer el contexto de la edición
    public ModalActualizarFecha(Notificacion notificacion) {
        this.notificacion = notificacion;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflado del layout personalizado para el modal inferior
        View v = inflater.inflate(R.layout.modal_actualizar_fecha, container, false);

        // Vinculación de componentes visuales
        TextView tvTitulo = v.findViewById(R.id.tvTituloModal);
        CalendarView calendarView = v.findViewById(R.id.calendarViewModal);
        Button btnGuardar = v.findViewById(R.id.btnGuardarFecha);

        // FEEDBACK VISUAL: Indica qué documento se está renovando
        tvTitulo.setText("Renovar: " + notificacion.getTitulo());

        // CONFIGURACIÓN INICIAL: Por defecto se selecciona la fecha actual del sistema
        fechaSeleccionada = Calendar.getInstance().getTimeInMillis();

        // ESCUCHADOR DE CALENDARIO: Captura la fecha seleccionada por el usuario
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            fechaSeleccionada = cal.getTimeInMillis();
        });

        // ACCIÓN DE GUARDADO: Ejecuta la persistencia en Firebase
        btnGuardar.setOnClickListener(view -> {
            // Conversión de milisegundos a Timestamp de Firebase para compatibilidad
            actualizarEnFirebase(new Timestamp(new java.util.Date(fechaSeleccionada)));
        });

        return v;
    }

    /**
     * Lógica de Negocio Centralizada:
     * Discrimina si el registro pertenece a la colección de mantenimientos o de documentos legales.
     */
    // Ajuste en tu método actualizarEnFirebase del Modal:
    private void actualizarEnFirebase(Timestamp nuevaFecha) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (notificacion.getTitulo().startsWith("Mantenimiento:")) {
            db.collection("mantenimientos").document(notificacion.getId())
                    .update("fecha_realizacion", nuevaFecha)
                    .addOnSuccessListener(aVoid -> cerrarModal("✅ Mantenimiento realizado"));
        } else {
            // 1. Borramos la notificación de la lista de pendientes (porque ya se renovó)
            db.collection("notificaciones").document(notificacion.getId()).delete();

            // 2. Actualizamos la fecha real en el Vehículo
            if (notificacion.getIdVehiculo() != null) {
                String campo = notificacion.getTitulo().toLowerCase().contains("soat") ? "fecha_soat" : "fecha_tecno";
                db.collection("vehiculos").document(notificacion.getIdVehiculo())
                        .update(campo, nuevaFecha)
                        .addOnSuccessListener(aVoid -> cerrarModal("✅ Documento Renovado"));
            }
        }
    }

    /**
     * Finalización del diálogo con retroalimentación al usuario
     */
    private void cerrarModal(String mensaje) {
        if (getContext() != null) {
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
        }
        dismiss(); // Cierra el modal inferior
    }
}