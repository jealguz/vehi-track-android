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

/**
 * Adaptador para gestionar la lista de vehículos en el RecyclerView.
 * Permite visualizar la marca, placa y tipo de cada vehículo registrado.
 */
public class VehiculoAdapter extends RecyclerView.Adapter<VehiculoAdapter.ViewHolder> {

    // Lista que contiene los objetos de tipo vehiculo traídos de la base de datos
    private List<vehiculo> listaVehiculos;

    // Constructor que recibe la lista de datos para el adaptador
    public VehiculoAdapter(List<vehiculo> listaVehiculos) {
        this.listaVehiculos = listaVehiculos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Se infla el layout individual de cada ítem (tarjeta del vehículo)
        // Se utiliza R.layout.item_vehiculo para definir la apariencia de cada fila
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehiculo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Se obtiene el objeto vehiculo correspondiente a la posición actual en la lista
        vehiculo v = listaVehiculos.get(position);

        // Se asignan los valores del objeto a los componentes de texto de la interfaz
        // Muestra la marca y modelo del vehículo
        holder.txtMarcaModelo.setText(v.getMarca());
        // Muestra la placa identificadora
        holder.txtPlaca.setText("Placa: " + v.getPlaca());
        // Muestra la categoría o tipo de vehículo
        holder.txtTipo.setText("Tipo: " + v.getTipo());
    }

    @Override
    public int getItemCount() {
        // Retorna el tamaño de la lista, validando que no sea nula para evitar errores
        return listaVehiculos != null ? listaVehiculos.size() : 0;
    }

    /**
     * Clase interna que mapea los componentes visuales del archivo XML
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Referencias a los TextViews del diseño item_vehiculo
        TextView txtMarcaModelo, txtPlaca, txtTipo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Vinculación de los objetos Java con sus respectivos IDs en el XML
            txtMarcaModelo = itemView.findViewById(R.id.txtMarcaModelo);
            txtPlaca = itemView.findViewById(R.id.txtPlaca);
            txtTipo = itemView.findViewById(R.id.txtTipo);
        }
    }
}