package com.example.vehi_track;

import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Importación del adaptador personalizado para la gestión de la lista
import com.example.vehi_track.adapters.AdapterReportes;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Actividad encargada de listar los reportes PDF generados por el usuario.
 * Implementa un explorador de archivos local filtrado para la gestión administrativa.
 */
public class ListaReportesActivity extends BaseActivity {

    private RecyclerView rvReportes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Vinculación de la interfaz de usuario (Layout de la lista)
        establecerContenido(R.layout.activity_lista_reportes);

        // 2. Configuración del RecyclerView:
        // Se utiliza LinearLayoutManager para un listado vertical estándar.
        rvReportes = findViewById(R.id.rvReportes);
        rvReportes.setLayoutManager(new LinearLayoutManager(this));

        // 3. Ejecución de la lógica de escaneo de archivos
        cargarReportesLocales();
    }

    /**
     * Escanea el almacenamiento externo en busca de documentos PDF
     * que coincidan con la nomenclatura de la aplicación.
     */
    private void cargarReportesLocales() {
        // CORRECCIÓN TÉCNICA: Se apunta al directorio público de descargas.
        // Esto garantiza que la app encuentre los archivos generados en ReportesActivity.
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        // Obtenemos un arreglo con todos los archivos de esa carpeta
        File[] files = folder.listFiles();

        // Lista dinámica para almacenar solo los resultados que nos interesan
        List<File> listaPdfs = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                // FILTRO DE SEGURIDAD Y ORDEN:
                // Solo añadimos archivos que terminen en .pdf Y empiecen por "Reporte_"
                // Esto evita que archivos ajenos a VehiTrack aparezcan en la lista.
                if (file.getName().endsWith(".pdf") && file.getName().startsWith("Reporte_")) {
                    listaPdfs.add(file);
                }
            }
        }

        // 4. Vinculación con la lógica de presentación (Adapter)
        if (!listaPdfs.isEmpty()) {
            // Se inicializa el adaptador con la lista filtrada y el contexto de la actividad
            AdapterReportes adapter = new AdapterReportes(listaPdfs, this);
            rvReportes.setAdapter(adapter);
        } else {
            // Notificación al usuario en caso de que la carpeta esté vacía o no haya reportes
            Toast.makeText(this, "Aún no has generado ningún reporte PDF", Toast.LENGTH_LONG).show();
        }
    }
}