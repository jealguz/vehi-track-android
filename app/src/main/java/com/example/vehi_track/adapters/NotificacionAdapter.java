package com.example.vehi_track.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vehi_track.R;
import com.example.vehi_track.models.Notificacion;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificacionAdapter extends RecyclerView.Adapter<NotificacionAdapter.ViewHolder> {

    private List<Notificacion> listaNotificaciones;
    private OnNotificacionClickListener listener;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnNotificacionClickListener {
        void onNotificacionClick(Notificacion notificacion);
    }

    public NotificacionAdapter(List<Notificacion> lista, OnNotificacionClickListener listener) {
        this.listaNotificaciones = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notificacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notificacion notif = listaNotificaciones.get(position);

        long diasRestantes = calcularDias(notif.getFecha());
        boolean esMantenimiento = notif.getTitulo().startsWith("Mantenimiento:");

        // 1. Título con Placa
        String vehiculo = (notif.getIdVehiculo() != null) ? " [" + notif.getIdVehiculo() + "]" : "";
        holder.tvTitulo.setText(notif.getTitulo() + vehiculo);

        // 2. Lógica de colores y mensajes
        if (diasRestantes < 0) {
            // CASO VENCIDO O ATRASADO
            String prefijo = esMantenimiento ? "🛠️ Atrasado hace: " : "🚨 ¡VENCIDO! Hace ";
            holder.tvMensaje.setText(prefijo + Math.abs(diasRestantes) + " días.");

            holder.card.setCardBackgroundColor(Color.parseColor("#FFEBEE")); // Rojo claro
            holder.tvTitulo.setTextColor(Color.parseColor("#B71C1C"));
        }
        else if (diasRestantes <= 7) {
            // CASO PRÓXIMO (7 días o menos)
            String mensaje = esMantenimiento ? "⏳ Taller programado en " + diasRestantes + " días."
                    : "⚠️ Faltan " + diasRestantes + " días. ¡Renueva pronto!";
            holder.tvMensaje.setText(mensaje);

            holder.card.setCardBackgroundColor(Color.parseColor("#FFF3E0")); // Naranja claro
            holder.tvTitulo.setTextColor(Color.parseColor("#E65100"));
        }
        else {
            // CASO AL DÍA
            String fechaLegible = (notif.getFecha() != null) ? sdf.format(notif.getFecha().toDate()) : "S/N";
            String prefijo = esMantenimiento ? "📅 Programado para: " : "📅 Vence el: ";
            holder.tvMensaje.setText(prefijo + fechaLegible);

            holder.card.setCardBackgroundColor(Color.WHITE);
            holder.tvTitulo.setTextColor(Color.parseColor("#0A2351")); // Tu azul oscuro
        }

        holder.itemView.setOnClickListener(v -> listener.onNotificacionClick(notif));
    }

    @Override
    public int getItemCount() {
        return listaNotificaciones.size();
    }

    // Método optimizado para usar el Timestamp directamente
    private long calcularDias(Timestamp fechaVencimiento) {
        if (fechaVencimiento == null) return 999;

        try {
            // Fecha de hoy a las 00:00:00
            Calendar hoy = Calendar.getInstance();
            hoy.set(Calendar.HOUR_OF_DAY, 0);
            hoy.set(Calendar.MINUTE, 0);
            hoy.set(Calendar.SECOND, 0);
            hoy.set(Calendar.MILLISECOND, 0);

            // Fecha de vencimiento desde el Timestamp
            Date dateVen = fechaVencimiento.toDate();

            long diff = dateVen.getTime() - hoy.getTimeInMillis();

            // Convertir la diferencia de milisegundos a días
            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            return 999;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvMensaje;
        CardView card;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloNotif);
            tvMensaje = itemView.findViewById(R.id.tvMensajeNotif);
            card = itemView.findViewById(R.id.cardNotif);
        }
    }
}