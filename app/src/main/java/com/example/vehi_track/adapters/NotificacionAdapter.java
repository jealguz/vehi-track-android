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

/**
 * Adaptador inteligente para el sistema de alertas y notificaciones.
 * Gestiona estados visuales (colores) basados en la urgencia del vencimiento
 * y procesa cálculos de tiempo en tiempo real.
 */
public class NotificacionAdapter extends RecyclerView.Adapter<NotificacionAdapter.ViewHolder> {

    private List<Notificacion> listaNotificaciones;
    private OnNotificacionClickListener listener; // Interface para manejar eventos de clic
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    /**
     * Interface personalizada para delegar la acción del clic a la Actividad principal.
     * Sigue el patrón de diseño "Listener" para desacoplar el adaptador de la UI.
     */
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
        // Inflado del layout específico para alertas
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notificacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notificacion notif = listaNotificaciones.get(position);

        // --- CÁLCULO DE PRIORIDAD ---
        // Se determina cuántos días faltan (o han pasado) para el evento
        long diasRestantes = calcularDias(notif.getFecha());
        // Se identifica si el mensaje es de taller o de documentos legales
        boolean esMantenimiento = notif.getTitulo() != null && notif.getTitulo().startsWith("Mantenimiento:");

        // 1. CONSTRUCCIÓN DEL TÍTULO DINÁMICO
        // Se concatena la placa del vehículo para que el usuario identifique la alerta de inmediato
        String vehiculo = (notif.getIdVehiculo() != null) ? " [" + notif.getIdVehiculo() + "]" : "";
        holder.tvTitulo.setText(notif.getTitulo() + vehiculo);

        // --- 2. LÓGICA DE SEMÁFORO (ESTADOS VISUALES) ---

        if (diasRestantes < 0) {
            // ESTADO CRÍTICO: El documento o mantenimiento ya venció (Rojo)
            String prefijo = esMantenimiento ? "🛠️ Atrasado hace: " : "🚨 ¡VENCIDO! Hace ";
            holder.tvMensaje.setText(prefijo + Math.abs(diasRestantes) + " días.");

            holder.card.setCardBackgroundColor(Color.parseColor("#FFEBEE")); // Fondo rojizo
            holder.tvTitulo.setTextColor(Color.parseColor("#B71C1C")); // Texto rojo oscuro
        }
        else if (diasRestantes <= 7) {
            // ESTADO DE ADVERTENCIA: Faltan 7 días o menos (Naranja)
            String mensaje = esMantenimiento ? "⏳ Taller programado en " + diasRestantes + " días."
                    : "⚠️ Faltan " + diasRestantes + " días. ¡Renueva pronto!";
            holder.tvMensaje.setText(mensaje);

            holder.card.setCardBackgroundColor(Color.parseColor("#FFF3E0")); // Fondo naranja suave
            holder.tvTitulo.setTextColor(Color.parseColor("#E65100")); // Texto naranja fuerte
        }
        else {
            // ESTADO NORMAL: Evento a futuro o al día (Azul Institucional)
            String fechaLegible = (notif.getFecha() != null) ? sdf.format(notif.getFecha().toDate()) : "S/N";
            String prefijo = esMantenimiento ? "📅 Programado para: " : "📅 Vence el: ";
            holder.tvMensaje.setText(prefijo + fechaLegible);

            holder.card.setCardBackgroundColor(Color.WHITE);
            holder.tvTitulo.setTextColor(Color.parseColor("#0A2351")); // Color corporativo Vehi-Track
        }

        // Programación del evento clic para abrir detalles o acciones
        holder.itemView.setOnClickListener(v -> listener.onNotificacionClick(notif));
    }

    @Override
    public int getItemCount() {
        return listaNotificaciones.size();
    }

    /**
     * Lógica aritmética para calcular la diferencia de días entre la fecha actual y el vencimiento.
     * @param fechaVencimiento Timestamp recuperado de Firestore.
     * @return Número de días (Negativo si ya pasó, positivo si es futuro).
     */
    private long calcularDias(Timestamp fechaVencimiento) {
        if (fechaVencimiento == null) return 999;

        try {
            // Se normaliza la fecha de hoy a medianoche para evitar errores por minutos/segundos
            Calendar hoy = Calendar.getInstance();
            hoy.set(Calendar.HOUR_OF_DAY, 0);
            hoy.set(Calendar.MINUTE, 0);
            hoy.set(Calendar.SECOND, 0);
            hoy.set(Calendar.MILLISECOND, 0);

            Date dateVen = fechaVencimiento.toDate();
            // Cálculo de la diferencia en milisegundos
            long diff = dateVen.getTime() - hoy.getTimeInMillis();

            // Conversión técnica de unidades de tiempo
            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            return 999; // Valor por defecto en caso de error de parseo
        }
    }

    /**
     * ViewHolder optimizado que incluye el CardView para poder manipular el fondo dinámicamente.
     */
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