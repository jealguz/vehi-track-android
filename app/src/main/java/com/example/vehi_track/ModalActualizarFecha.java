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

public class ModalActualizarFecha extends BottomSheetDialogFragment {

    private Notificacion notificacion;
    private long fechaSeleccionada;

    public ModalActualizarFecha(Notificacion notificacion) {
        this.notificacion = notificacion;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.modal_actualizar_fecha, container, false);

        TextView tvTitulo = v.findViewById(R.id.tvTituloModal);
        CalendarView calendarView = v.findViewById(R.id.calendarViewModal);
        Button btnGuardar = v.findViewById(R.id.btnGuardarFecha);

        tvTitulo.setText("Renovar: " + notificacion.getTitulo());

        // Por defecto hoy
        fechaSeleccionada = Calendar.getInstance().getTimeInMillis();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            fechaSeleccionada = cal.getTimeInMillis();
        });

        btnGuardar.setOnClickListener(view -> {
            actualizarEnFirebase(new Timestamp(new java.util.Date(fechaSeleccionada)));
        });

        return v;
    }

    private void actualizarEnFirebase(Timestamp nuevaFecha) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (notificacion.getTitulo().startsWith("Mantenimiento:")) {
            // Actualizar mantenimiento realizado
            db.collection("mantenimientos").document(notificacion.getId())
                    .update("fecha_realizacion", nuevaFecha)
                    .addOnSuccessListener(aVoid -> cerrarModal("✅ Mantenimiento registrado"));
        } else {
            // Actualizar SOAT o Tecno
            db.collection("notificaciones").document(notificacion.getId())
                    .update("fecha", nuevaFecha)
                    .addOnSuccessListener(aVoid -> cerrarModal("✅ Fecha actualizada"));

            // Si tiene placa vinculada, actualizar el vehículo también
            if (notificacion.getIdVehiculo() != null) {
                String campo = notificacion.getTitulo().toLowerCase().contains("soat") ? "fechaSoat" : "fechaTecno";
                db.collection("vehiculos").document(notificacion.getIdVehiculo()).update(campo, nuevaFecha);
            }
        }
    }

    private void cerrarModal(String mensaje) {
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
        dismiss();
    }
}