package com.example.vehi_track.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vehi_track.R;
import com.example.vehi_track.models.Mantenimiento;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador para la gestión y visualización del historial de mantenimientos preventivos y correctivos.
 * Implementa lógica de visibilidad dinámica para diferenciar mantenimientos pendientes de realizados.
 */
public class MantenimientoAdapter extends RecyclerView.Adapter<MantenimientoAdapter.ViewHolder> {

    // Lista de objetos tipo Mantenimiento vinculados a Firestore
    private List<Mantenimiento> mList;

    // Buena Práctica: Se define el formato de fecha como atributo de clase para evitar
    // crear múltiples instancias de SimpleDateFormat durante el scroll, optimizando la memoria RAM.
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    /**
     * Constructor que inicializa la fuente de datos del historial.
     */
    public MantenimientoAdapter(List<Mantenimiento> mList) {
        this.mList = mList;
    }

    /**
     * Infla el diseño XML 'item_mantenimiento' para cada entrada de la lista.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mantenimiento, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Vincula los datos del objeto Mantenimiento con la interfaz gráfica.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Obtención del objeto de mantenimiento actual
        Mantenimiento m = mList.get(position);

        // Asignación de la descripción del servicio (Ej: Cambio de aceite)
        holder.tvDesc.setText(m.getDescripcion());

        // --- FORMATEO DE MONEDA PROFESIONAL ---
        // Se aplica formato de miles para facilitar la lectura del costo del servicio
        holder.tvCosto.setText(String.format(Locale.getDefault(), "$ %,.0f", m.getCosto()));

        // Registro del kilometraje capturado en el momento del servicio
        holder.tvKm.setText("📍 " + m.getKilometraje_mantenimiento() + " KM");

        // --- MANEJO DE FECHA PROGRAMADA ---
        // Se valida la existencia de una fecha sugerida para el próximo mantenimiento
        if (m.getFecha_programada() != null) {
            Date dateProg = m.getFecha_programada().toDate(); // Conversión de Timestamp a Date
            holder.tvFechaP.setText("📅 Prog: " + sdf.format(dateProg));
        } else {
            holder.tvFechaP.setText("📅 Prog: S/N"); // S/N = Sin Novedad / No programado
        }

        // --- LÓGICA DE IDENTIFICACIÓN DEL VEHÍCULO (PLACA) ---
        // Se aplica un respaldo (fallback): si el campo 'placa' es nulo, intenta usar 'id_vehiculo'
        String placaMostrar = (m.getPlaca() != null) ? m.getPlaca() : m.getId_vehiculo();

        if (placaMostrar != null) {
            holder.tvPlaca.setText("🏍️ " + placaMostrar);
            holder.tvPlaca.setVisibility(View.VISIBLE);
        } else {
            // Se oculta el campo si no hay referencia al vehículo para mantener la estética de la UI
            holder.tvPlaca.setVisibility(View.GONE);
        }

        // --- MANEJO DE FECHA DE REALIZACIÓN ---
        // Lógica condicional: Si el mantenimiento ya fue ejecutado, muestra la fecha de éxito.
        // Si no, el campo se oculta completamente para no confundir al usuario.
        if (m.getFecha_realizacion() != null) {
            Date dateReal = m.getFecha_realizacion().toDate();
            holder.tvFechaR.setText("✅ Realizado: " + sdf.format(dateReal));
            holder.tvFechaR.setVisibility(View.VISIBLE);
        } else {
            holder.tvFechaR.setVisibility(View.GONE);
        }
    }

    /**
     * Indica al RecyclerView la cantidad total de registros a renderizar.
     */
    @Override
    public int getItemCount() {
        return mList.size();
    }

    /**
     * Clase interna que referencia los componentes visuales del ítem.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDesc, tvCosto, tvKm, tvFechaP, tvFechaR, tvPlaca;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Mapeo de IDs definidos en item_mantenimiento.xml
            tvDesc = itemView.findViewById(R.id.tvItemDesc);
            tvCosto = itemView.findViewById(R.id.tvItemCosto);
            tvKm = itemView.findViewById(R.id.tvItemKm);
            tvFechaP = itemView.findViewById(R.id.tvItemFechaProg);
            tvFechaR = itemView.findViewById(R.id.tvItemFechaReal);
            tvPlaca = itemView.findViewById(R.id.tvItemPlacaManto);
        }
    }
}