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

// ... (mismos imports)

public class CombustibleAdapter extends RecyclerView.Adapter<CombustibleAdapter.ViewHolder> {

    private List<Combustible> listaCombustible;

    public CombustibleAdapter(List<Combustible> listaCombustible) {
        this.listaCombustible = listaCombustible;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Asegúrate de que item_combustible tenga un TextView para la placa (ej: tvItemPlaca)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_combustible, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Combustible c = listaCombustible.get(position);

        // 1. Formatear la fecha
        if (c.getFecha() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String fechaLegible = sdf.format(c.getFecha().toDate());
            holder.tvFecha.setText(fechaLegible);
        } else {
            holder.tvFecha.setText("Sin fecha");
        }

        // 2. Mostrar la Placa (Muy importante ahora que es colección raíz)
        if (c.getId_vehiculo() != null) {
            holder.tvPlaca.setText("🏍️ " + c.getId_vehiculo());
            holder.tvPlaca.setVisibility(View.VISIBLE);
        } else {
            holder.tvPlaca.setVisibility(View.GONE);
        }

        // 3. Formateo de valores económicos y técnicos
        holder.tvCosto.setText(String.format(Locale.getDefault(), "$ %,.0f", c.getCosto())); // Usamos %,.0f para miles
        holder.tvCant.setText(String.format(Locale.getDefault(), "⛽ %.2f Gal", c.getCantidad()));
        holder.tvKm.setText("📍 " + c.getKilometraje() + " KM");
    }

    @Override
    public int getItemCount() {
        return listaCombustible.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCosto, tvFecha, tvCant, tvKm, tvPlaca;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCosto = itemView.findViewById(R.id.tvItemCostoComb);
            tvFecha = itemView.findViewById(R.id.tvItemFechaComb);
            tvCant = itemView.findViewById(R.id.tvItemCant);
            tvKm = itemView.findViewById(R.id.tvItemKmComb);
            // Agrega este ID en tu XML item_combustible.xml si no lo tienes
            tvPlaca = itemView.findViewById(R.id.tvItemPlacaComb);
        }
    }
}