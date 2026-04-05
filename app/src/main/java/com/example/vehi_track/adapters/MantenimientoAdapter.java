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

public class MantenimientoAdapter extends RecyclerView.Adapter<MantenimientoAdapter.ViewHolder> {

    private List<Mantenimiento> mList;
    // Definimos el formato de fecha una sola vez para ahorrar memoria
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public MantenimientoAdapter(List<Mantenimiento> mList) {
        this.mList = mList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mantenimiento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mantenimiento m = mList.get(position);

        holder.tvDesc.setText(m.getDescripcion());

        // Formateo de moneda profesional
        holder.tvCosto.setText(String.format(Locale.getDefault(), "$ %,.0f", m.getCosto()));

        holder.tvKm.setText("📍 " + m.getKilometraje_mantenimiento() + " KM");

        // --- MANEJO DE FECHA PROGRAMADA (Timestamp a String) ---
        if (m.getFecha_programada() != null) {
            Date dateProg = m.getFecha_programada().toDate();
            holder.tvFechaP.setText("📅 Prog: " + sdf.format(dateProg));
        } else {
            holder.tvFechaP.setText("📅 Prog: S/N");
        }

        // --- MANEJO DE PLACA ---
        // Priorizamos el campo 'placa', si no, usamos el id_vehiculo como respaldo
        String placaMostrar = (m.getPlaca() != null) ? m.getPlaca() : m.getId_vehiculo();
        if (placaMostrar != null) {
            holder.tvPlaca.setText("🏍️ " + placaMostrar);
            holder.tvPlaca.setVisibility(View.VISIBLE);
        } else {
            holder.tvPlaca.setVisibility(View.GONE);
        }

        // --- MANEJO DE FECHA REALIZACIÓN ---
        if (m.getFecha_realizacion() != null) {
            Date dateReal = m.getFecha_realizacion().toDate();
            holder.tvFechaR.setText("✅ Realizado: " + sdf.format(dateReal));
            holder.tvFechaR.setVisibility(View.VISIBLE);
        } else {
            holder.tvFechaR.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDesc, tvCosto, tvKm, tvFechaP, tvFechaR, tvPlaca;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDesc = itemView.findViewById(R.id.tvItemDesc);
            tvCosto = itemView.findViewById(R.id.tvItemCosto);
            tvKm = itemView.findViewById(R.id.tvItemKm);
            tvFechaP = itemView.findViewById(R.id.tvItemFechaProg);
            tvFechaR = itemView.findViewById(R.id.tvItemFechaReal);
            tvPlaca = itemView.findViewById(R.id.tvItemPlacaManto);
        }
    }
}