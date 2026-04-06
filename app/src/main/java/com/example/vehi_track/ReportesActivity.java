package com.example.vehi_track;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Módulo de Inteligencia de Negocios y Reportes.
 * Esta clase se encarga de extraer datos de Firebase y transformarlos en documentos PDF profesionales.
 */
public class ReportesActivity extends BaseActivity {

    private FirebaseFirestore db;
    private String idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        establecerContenido(R.layout.activity_reportes);

        // Configuración de la barra de herramientas y el menú lateral (Drawer)
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Generar Reportes");

            androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        // Inicialización de instancias de Firebase
        db = FirebaseFirestore.getInstance();
        idUsuario = FirebaseAuth.getInstance().getUid();

        // Referencia a los botones de la interfaz
        Button btnGenerarManto = findViewById(R.id.btnReporteManto);
        Button btnGenerarComb = findViewById(R.id.btnReporteComb);

        // Listeners para disparar la generación según la colección de Firebase
        btnGenerarManto.setOnClickListener(v -> generarReporte("mantenimientos", "Reporte_Mantenimientos.pdf"));
        btnGenerarComb.setOnClickListener(v -> generarReporte("combustible", "Reporte_Combustible.pdf"));
    }

    /**
     * Consulta Firestore filtrando por el ID del usuario actual.
     */
    private void generarReporte(String coleccion, String nombreArchivo) {
        if (idUsuario == null) return;

        db.collection(coleccion)
                .whereEqualTo("id_usuario", idUsuario)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "No hay datos para reportar", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Si hay datos, procedemos a dibujar el PDF
                    crearPDF(queryDocumentSnapshots, nombreArchivo, coleccion.toUpperCase());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al conectar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Lógica de dibujo y renderizado del documento PDF.
     */
    private void crearPDF(com.google.firebase.firestore.QuerySnapshot datos, String nombreDoc, String tituloReporte) {
        // Inicialización del documento PDF (Tamaño A4 estándar: 595x842 puntos)
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // --- DISEÑO DEL ENCABEZADO CORPORATIVO ---
        paint.setColor(Color.parseColor("#0A2351")); // Azul oscuro Vehi-Track
        canvas.drawRect(0, 0, 595, 90, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(24f);
        paint.setFakeBoldText(true);
        canvas.drawText("VEHI-TRACK", 30, 45, paint);

        paint.setTextSize(12f);
        paint.setFakeBoldText(false);
        canvas.drawText("Gestión Profesional de Vehículos", 30, 70, paint);

        // --- METADATOS DEL REPORTE ---
        paint.setColor(Color.BLACK);
        paint.setTextSize(16f);
        paint.setFakeBoldText(true);
        canvas.drawText(tituloReporte, 30, 130, paint);

        paint.setTextSize(10f);
        paint.setFakeBoldText(false);
        paint.setColor(Color.GRAY);
        canvas.drawText("Generado el: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new java.util.Date()), 30, 150, paint);

        // --- ENCABEZADO DE LA TABLA DE DATOS ---
        int y = 190;
        paint.setColor(Color.BLACK);
        paint.setFakeBoldText(true);
        canvas.drawText("Detalle / Placa", 30, y, paint);
        canvas.drawText("Fecha", 350, y, paint);
        canvas.drawText("Monto", 480, y, paint);

        y += 10;
        canvas.drawLine(30, y, 565, y, paint); // Línea divisoria superior
        y += 25;

        // --- PROCESAMIENTO DINÁMICO DE FILAS ---
        paint.setFakeBoldText(false);
        for (QueryDocumentSnapshot doc : datos) {

            // 1. Obtención de Descripción: Verifica campos según la colección
            String desc = "Registro";
            if (doc.contains("descripcion") && doc.get("descripcion") != null) desc = doc.getString("descripcion");
            else if (doc.contains("tipo_combustible")) desc = "Tanqueo: " + doc.getString("tipo_combustible");

            // 2. Lógica de Placa (FALLBACK): Busca el campo placa, de lo contrario usa el ID
            String placaStr = "S/N";
            if (doc.contains("placa") && doc.get("placa") != null) {
                placaStr = doc.getString("placa");
            } else if (doc.contains("id_vehiculo") && doc.get("id_vehiculo") != null) {
                placaStr = doc.getString("id_vehiculo");
            }

            // 3. Formateo de Fecha: Maneja Timestamps nativos de Firebase
            String fechaStr = "N/A";
            Object fObj = doc.get("fecha_programada");
            if (fObj == null) fObj = doc.get("fecha");

            if (fObj instanceof com.google.firebase.Timestamp) {
                fechaStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(((com.google.firebase.Timestamp) fObj).toDate());
            } else if (fObj != null) {
                fechaStr = fObj.toString();
            }

            // 4. Normalización de Costo: Convierte Strings o Numbers a formato moneda
            double costoVal = 0;
            if (doc.contains("costo")) {
                Object cObj = doc.get("costo");
                if (cObj instanceof Number) {
                    costoVal = ((Number) cObj).doubleValue();
                } else if (cObj instanceof String) {
                    try { costoVal = Double.parseDouble((String) cObj); } catch (Exception e) { costoVal = 0; }
                }
            }
            String montoStr = String.format(Locale.getDefault(), "$ %,.0f", costoVal);

            // DIBUJO DE LA FILA EN EL CANVAS
            canvas.drawText(desc + " [" + placaStr + "]", 30, y, paint);
            canvas.drawText(fechaStr, 350, y, paint);
            canvas.drawText(montoStr, 480, y, paint);

            y += 30; // Espaciado entre filas
            paint.setColor(Color.LTGRAY);
            canvas.drawLine(30, y - 10, 565, y - 10, paint); // Línea sutil entre registros
            paint.setColor(Color.BLACK);

            // Control de desbordamiento de página (Límite inferior)
            if (y > 780) break;
        }

        // Pie de página legal/informativo
        paint.setColor(Color.GRAY);
        paint.setTextSize(9f);
        canvas.drawText("Documento generado automáticamente por el sistema Vehi-Track.", 30, 820, paint);

        document.finishPage(page);

        // --- PROCESO DE ALMACENAMIENTO ---
        // Se guarda en la carpeta pública de Descargas para fácil acceso del usuario
        File directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(directorio, nombreDoc);

        try {
            document.writeTo(new FileOutputStream(file));
            // Notifica al sistema que hay un nuevo archivo para que aparezca en la galería/explorador
            android.media.MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null, null);
            Toast.makeText(this, "PDF generado con éxito", Toast.LENGTH_SHORT).show();

            // --- APERTURA AUTOMÁTICA MEDIANTE INTENT ---
            // Se usa FileProvider para cumplir con las políticas de seguridad de Android
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Permiso de lectura temporal
            startActivity(Intent.createChooser(intent, "Abriendo Reporte..."));

        } catch (IOException e) {
            Toast.makeText(this, "Error al guardar el archivo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            document.close(); // Liberación de recursos de memoria
        }
    }
}