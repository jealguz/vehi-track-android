package com.example.vehi_track;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class ReportesActivity extends BaseActivity {

    private FirebaseFirestore db;
    private String idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inflamos el contenido usando el método de la BaseActivity
        establecerContenido(R.layout.activity_reportes);

        // 2. Configurar la Toolbar para que aparezca la hamburguesa
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Generar Reportes");

            androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        db = FirebaseFirestore.getInstance();
        idUsuario = FirebaseAuth.getInstance().getUid();

        Button btnGenerarManto = findViewById(R.id.btnReporteManto);
        Button btnGenerarComb = findViewById(R.id.btnReporteComb);

        // 3. Lógica de botones (se mantiene tu lógica excelente)
        btnGenerarManto.setOnClickListener(v -> generarReporte("mantenimientos", "Reporte_Mantenimientos.pdf"));
        btnGenerarComb.setOnClickListener(v -> generarReporte("combustible", "Reporte_Combustible.pdf"));
    }

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
                    crearPDF(queryDocumentSnapshots, nombreArchivo, coleccion.toUpperCase());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al conectar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void crearPDF(com.google.firebase.firestore.QuerySnapshot datos, String nombreDoc, String tituloReporte) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // --- ENCABEZADO AZUL ---
        paint.setColor(Color.parseColor("#0A2351"));
        canvas.drawRect(0, 0, 595, 90, paint);

        // --- LOGO Y TÍTULO ---
        paint.setColor(Color.WHITE);
        paint.setTextSize(24f);
        paint.setFakeBoldText(true);
        canvas.drawText("VEHI-TRACK", 30, 45, paint);

        paint.setTextSize(14f);
        paint.setFakeBoldText(false);
        canvas.drawText("Gestión Inteligente de Vehículos", 30, 70, paint);

        // --- INFO DEL REPORTE ---
        paint.setColor(Color.BLACK);
        paint.setTextSize(16f);
        paint.setFakeBoldText(true);
        canvas.drawText(tituloReporte, 30, 130, paint);

        paint.setTextSize(10f);
        paint.setFakeBoldText(false);
        paint.setColor(Color.GRAY);
        canvas.drawText("Generado el: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new java.util.Date()), 30, 150, paint);

        // --- TABLA DE DATOS ---
        int y = 190;
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(1f);
        paint.setFakeBoldText(true);
        canvas.drawText("Descripción / Placa", 30, y, paint);
        canvas.drawText("Fecha", 350, y, paint);
        canvas.drawText("Monto", 480, y, paint);

        y += 10;
        canvas.drawLine(30, y, 565, y, paint);
        y += 25;

        paint.setFakeBoldText(false);
        for (QueryDocumentSnapshot doc : datos) {
            String desc = doc.contains("descripcion") ? doc.getString("descripcion") : "Combustible";
            String placa = doc.getString("id_vehiculo") != null ? doc.getString("id_vehiculo") : "S/N";

            String fecha = "N/A";
            if (doc.contains("fecha_realizacion")) {
                fecha = doc.getString("fecha_realizacion");
            } else if (doc.contains("fecha") && doc.get("fecha") instanceof com.google.firebase.Timestamp) {
                fecha = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(((com.google.firebase.Timestamp) doc.get("fecha")).toDate());
            }

            // Manejo seguro del costo para evitar errores de tipo
            double costoVal = 0;
            if (doc.contains("costo")) {
                Object costoObj = doc.get("costo");
                if (costoObj instanceof Number) {
                    costoVal = ((Number) costoObj).doubleValue();
                }
            }
            String monto = String.format(Locale.getDefault(), "$ %,.0f", costoVal);

            canvas.drawText(desc + " [" + placa + "]", 30, y, paint);
            canvas.drawText(fecha, 350, y, paint);
            canvas.drawText(monto, 480, y, paint);

            y += 30;
            paint.setColor(Color.LTGRAY);
            canvas.drawLine(30, y-10, 565, y-10, paint);
            paint.setColor(Color.BLACK);

            if (y > 780) break;
        }

        // --- PIE DE PÁGINA ---
        paint.setColor(Color.GRAY);
        paint.setTextSize(9f);
        canvas.drawText("Reporte oficial generado por el usuario desde Vehi-Track App.", 30, 820, paint);

        document.finishPage(page);

        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), nombreDoc);
        try {
            document.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF Profesional generado en descargas", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al escribir archivo", Toast.LENGTH_SHORT).show();
        }
        document.close();
    }
}