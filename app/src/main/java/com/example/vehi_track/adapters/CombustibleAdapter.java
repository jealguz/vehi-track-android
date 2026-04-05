package com.example.vehi_track.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vehi_track.R;
import com.example.vehi_track.models.Combustible;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Adaptador encargado de gestionar la visualización de la bitácora de combustible.
 * Implementa el patrón ViewHolder para optimizar el rendimiento al reciclar las vistas de la lista.
 */
public class CombustibleAdapter extends RecyclerView.Adapter<CombustibleAdapter.ViewHolder> {

    // Lista de objetos tipo Combustible que contiene la información traída de Firestore
    private List<Combustible> listaCombustible;

    /**
     * Constructor del adaptador que recibe la fuente de datos.
     * @param listaCombustible Lista de registros de tanqueo.
     */
    public CombustibleAdapter(List<Combustible> listaCombustible) {
        this.listaCombustible = listaCombustible;
    }

    /**
     * Método que "infla" (crea) la estructura visual de cada fila de la lista.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Vinculación con el archivo XML personalizado 'item_combustible'
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_combustible, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Método que vincula los datos del objeto Combustible con los elementos visuales de la fila.
     * Se ejecuta por cada elemento visible en la pantalla.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Obtención del registro actual basado en la posición de la lista
        Combustible c = listaCombustible.get(position);

        // --- 1. PROCESAMIENTO DE LA FECHA ---
        // Se valida que la fecha exista para evitar cierres inesperados (NullPointerException)
        if (c.getFecha() != null) {
            // Se define un formato legible para Colombia (Día/Mes/Año)
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            // Conversión del Timestamp de Firebase a un String formateado
            String fechaLegible = sdf.format(c.getFecha().toDate());
            holder.tvFecha.setText(fechaLegible);
        } else {
            holder.tvFecha.setText("Sin fecha");
        }

        // --- 2. IDENTIFICACIÓN DEL VEHÍCULO ---
        // Muestra la placa para que el usuario sepa a qué moto corresponde el gasto
        if (c.getId_vehiculo() != null) {
            holder.tvPlaca.setText("🏍️ " + c.getId_vehiculo());
            holder.tvPlaca.setVisibility(View.VISIBLE); // Asegura que sea visible si hay datos
        } else {
            holder.tvPlaca.setVisibility(View.GONE); // Oculta el campo si no hay ID de vehículo
        }

        // --- 3. FORMATEO DE VALORES ECONÓMICOS Y TÉCNICOS ---
        // Formateo de moneda: Agrega separadores de miles para mayor legibilidad (Ej: $ 15,000)
        holder.tvCosto.setText(String.format(Locale.getDefault(), "$ %,.0f", c.getCosto()));

        // Formateo de volumen: Muestra dos decimales para la cantidad de galones
        holder.tvCant.setText(String.format(Locale.getDefault(), "⛽ %.2f Gal", c.getCantidad()));

        // Muestra el recorrido del vehículo al momento del tanqueo
        holder.tvKm.setText("📍 " + c.getKilometraje() + " KM");
    }

    /**
     * Retorna la cantidad total de elementos en la lista para que el RecyclerView sepa cuántos dibujar.
     */
    @Override
    public int getItemCount() {
        return listaCombustible.size();
    }

    /**
     * Clase interna que actúa como contenedor de las vistas (caché de elementos visuales).
     * Evita llamadas repetitivas a findViewById, mejorando la fluidez del scroll.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Referencias a los componentes de texto del XML
        TextView tvCosto, tvFecha, tvCant, tvKm, tvPlaca;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Vinculación de los objetos Java con los IDs definidos en item_combustible.xml
            tvCosto = itemView.findViewById(R.id.tvItemCostoComb);
            tvFecha = itemView.findViewById(R.id.tvItemFechaComb);
            tvCant = itemView.findViewById(R.id.tvItemCant);
            tvKm = itemView.findViewById(R.id.tvItemKmComb);
            tvPlaca = itemView.findViewById(R.id.tvItemPlacaComb);
        }
    }
}