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
 * Adaptador para la lista de registros de combustible.
 * Gestiona la visualización de los datos técnicos en el RecyclerView.
 */
public class CombustibleAdapter extends RecyclerView.Adapter<CombustibleAdapter.ViewHolder> {

    private List<Combustible> listaCombustible;

    public CombustibleAdapter(List<Combustible> listaCombustible) {
        this.listaCombustible = listaCombustible;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_combustible, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Combustible c = listaCombustible.get(position);

        // --- 1. PROCESAMIENTO DE LA FECHA ---
        if (c.getFecha() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvFecha.setText(sdf.format(c.getFecha().toDate()));
        } else {
            holder.tvFecha.setText("Sin fecha");
        }

        // --- 2. IDENTIFICACIÓN DEL VEHÍCULO (ACTUALIZADO) ---
        // Priorizamos mostrar la placa si existe en el objeto
        if (c.getPlaca() != null && !c.getPlaca().isEmpty()) {
            holder.tvPlaca.setText("🏍️ " + c.getPlaca()); // Mostrará "BGT984"
            holder.tvPlaca.setVisibility(View.VISIBLE);
        } else if (c.getId_vehiculo() != null) {
            holder.tvPlaca.setText("🏍️ " + c.getId_vehiculo()); // Respaldo por si no hay placa
            holder.tvPlaca.setVisibility(View.VISIBLE);
        } else {
            holder.tvPlaca.setVisibility(View.GONE);
        }

        // --- 3. FORMATEO DE VALORES NUMÉRICOS ---
        try {
            double valorCosto = c.getCosto();
            holder.tvCosto.setText(String.format(Locale.getDefault(), "$ %,.0f", valorCosto));
        } catch (Exception e) {
            holder.tvCosto.setText("$ 0");
        }

        try {
            double valorCant = c.getCantidad();
            holder.tvCant.setText(String.format(Locale.getDefault(), "⛽ %.2f Gal", valorCant));
        } catch (Exception e) {
            holder.tvCant.setText("⛽ 0.00 Gal");
        }

        // --- 4. KILOMETRAJE ---
        holder.tvKm.setText("📍 " + c.getKilometraje() + " KM");
    }

    @Override
    public int getItemCount() {
        return listaCombustible != null ? listaCombustible.size() : 0;
    }

    /**
     * ViewHolder: Caché de referencias visuales.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCosto, tvFecha, tvCant, tvKm, tvPlaca;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCosto = itemView.findViewById(R.id.tvItemCostoComb);
            tvFecha = itemView.findViewById(R.id.tvItemFechaComb);
            tvCant = itemView.findViewById(R.id.tvItemCant);
            tvKm = itemView.findViewById(R.id.tvItemKmComb);
            tvPlaca = itemView.findViewById(R.id.tvItemPlacaComb);
        }
    }
}