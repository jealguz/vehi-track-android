package com.example.vehi_track.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.example.vehi_track.R;
import com.example.vehi_track.models.vehiculo;

public class VehiculoAdapter extends RecyclerView.Adapter<VehiculoAdapter.ViewHolder> {

    private List<vehiculo> listaVehiculos;

    public VehiculoAdapter(List<vehiculo> listaVehiculos) {
        this.listaVehiculos = listaVehiculos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // CORRECCIÓN: Aquí debes usar el layout del ITEM (la tarjeta), no el de la Activity
        // Supongo que se llama item_vehiculo. Si tiene otro nombre, cámbialo aquí:
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehiculo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        vehiculo v = listaVehiculos.get(position);

        // Usamos los campos según tu Firebase
        holder.txtMarcaModelo.setText(v.getMarca());
        holder.txtPlaca.setText("Placa: " + v.getPlaca());
        holder.txtTipo.setText("Tipo: " + v.getTipo());
    }

    @Override
    public int getItemCount() {
        return listaVehiculos != null ? listaVehiculos.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtMarcaModelo, txtPlaca, txtTipo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Estos IDs deben existir dentro de tu R.layout.item_vehiculo
            txtMarcaModelo = itemView.findViewById(R.id.txtMarcaModelo);
            txtPlaca = itemView.findViewById(R.id.txtPlaca);
            txtTipo = itemView.findViewById(R.id.txtTipo);
        }
    }
}