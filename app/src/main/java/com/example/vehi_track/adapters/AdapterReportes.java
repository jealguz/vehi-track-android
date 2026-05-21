package com.example.vehi_track.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vehi_track.R;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdapterReportes extends RecyclerView.Adapter<AdapterReportes.ViewHolder> {

    private List<File> listaArchivos;
    private Context context;

    public AdapterReportes(List<File> listaArchivos, Context context) {
        this.listaArchivos = listaArchivos;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reporte, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File archivo = listaArchivos.get(position);
        holder.tvNombre.setText(archivo.getName());

        // Formatear fecha de creación
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvFecha.setText(sdf.format(new Date(archivo.lastModified())));

        holder.itemView.setOnClickListener(v -> abrirPDF(archivo));
    }

    private void abrirPDF(File file) {
        // Uso de FileProvider para seguridad en Android 7.0+
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Abrir reporte con..."));
    }

    @Override
    public int getItemCount() {
        return listaArchivos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvFecha;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreArchivo);
            tvFecha = itemView.findViewById(R.id.tvFechaArchivo);
        }
    }
}