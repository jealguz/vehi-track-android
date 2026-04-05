package com.example.vehi_track;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.example.vehi_track.models.vehiculo;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Actividad para el registro de servicios técnicos y preventivos.
 * Permite documentar mantenimientos realizados o programar tareas futuras,
 * vinculándolas dinámicamente a la flota del usuario.
 */
public class RegistrarMantenimientoActivity extends BaseActivity {

    // Campos de entrada técnica
    private EditText etFechaProg, etFechaReal, etDescManto, etKmManto, etCostoManto;
    private AutoCompleteTextView spinnerVehiculos;
    private TextInputLayout layoutSelector;
    private MaterialButton btnGuardar;

    // Servicios de Backend y persistencia de estado
    private FirebaseFirestore db;
    private String idVehiculoFinal, placaFinal, idUsuario;
    private List<vehiculo> listaVehiculosCargados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. INTEGRACIÓN CON BASEACTIVITY
        // Hereda el comportamiento del Navigation Drawer y la estructura base.
        establecerContenido(R.layout.activity_registrar_mantenimiento);

        db = FirebaseFirestore.getInstance();
        idUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Vinculación de la interfaz de usuario (UI)
        etFechaProg = findViewById(R.id.etFechaProg);
        etFechaReal = findViewById(R.id.etFechaReal);
        etDescManto = findViewById(R.id.etDescManto);
        etKmManto = findViewById(R.id.etKmManto);
        etCostoManto = findViewById(R.id.etCostoManto);
        btnGuardar = findViewById(R.id.btnGuardarManto);
        spinnerVehiculos = findViewById(R.id.spinnerVehiculosManto);
        layoutSelector = findViewById(R.id.layoutSeleccionarVehiculo);

        // 2. GESTIÓN DE CONTEXTO DE NAVEGACIÓN (Deep Linking interno)
        idVehiculoFinal = getIntent().getStringExtra("idVehiculo");
        placaFinal = getIntent().getStringExtra("placa");

        if (idVehiculoFinal != null) {
            // Si la navegación nace desde una moto específica, se simplifica la UI ocultando el selector.
            layoutSelector.setVisibility(View.GONE);
        } else {
            // Si el acceso es global, se requiere la selección manual del vehículo.
            cargarVehiculosDelUsuario();
        }

        // 3. LISTENERS DE INTERACCIÓN
        // Uso de DatePickerDialog para estandarizar la entrada de fechas.
        etFechaProg.setOnClickListener(v -> mostrarCalendario(etFechaProg));
        etFechaReal.setOnClickListener(v -> mostrarCalendario(etFechaReal));
        btnGuardar.setOnClickListener(v -> validarYGuardar());

        // Control de navegación hacia atrás
        if (findViewById(R.id.btnBack) != null) {
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        }
    }

    /**
     * Recuperación de Flota:
     * Consulta Firestore para obtener los vehículos asociados al UID del desarrollador/usuario.
     */
    private void cargarVehiculosDelUsuario() {
        listaVehiculosCargados = new ArrayList<>();
        db.collection("vehiculos")
                .whereEqualTo("id_usuario", idUsuario)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> opciones = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        vehiculo v = doc.toObject(vehiculo.class);
                        if (v != null) {
                            v.setId_vehiculo(doc.getId());
                            listaVehiculosCargados.add(v);
                            opciones.add(v.getPlaca() + " - " + v.getMarca());
                        }
                    }
                    // Adaptador para el componente visual de selección (Dropdown)
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1, opciones);
                    spinnerVehiculos.setAdapter(adapter);

                    spinnerVehiculos.setOnItemClickListener((parent, view, position, id) -> {
                        vehiculo sel = listaVehiculosCargados.get(position);
                        idVehiculoFinal = sel.getId_vehiculo();
                        placaFinal = sel.getPlaca();
                    });
                });
    }

    /**
     * Utilidad de Interfaz: Despliega el calendario nativo.
     */
    private void mostrarCalendario(EditText editText) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String fechaSel = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, (month + 1), year);
            editText.setText(fechaSel);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Persistencia y Validación de Negocio:
     * Transforma la entrada del usuario en un documento estructurado para Firestore.
     */
    private void validarYGuardar() {
        String desc = etDescManto.getText().toString().trim();
        String fProgStr = etFechaProg.getText().toString().trim();
        String fRealStr = etFechaReal.getText().toString().trim();
        String kmStr = etKmManto.getText().toString().trim();
        String costoStr = etCostoManto.getText().toString().trim();

        // VALIDACIÓN DE CAMPOS CRÍTICOS
        if (desc.isEmpty() || fProgStr.isEmpty() || kmStr.isEmpty() || idVehiculoFinal == null) {
            Toast.makeText(this, "⚠️ Selecciona un vehículo y llena los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // BLOQUEO DE UI: Previene el envío de peticiones duplicadas
            btnGuardar.setEnabled(false);
            btnGuardar.setText("Guardando...");

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Map<String, Object> manto = new HashMap<>();

            // NORMALIZACIÓN DE DATOS (Relaciones en la DB)
            manto.put("id_usuario", idUsuario);
            manto.put("id_vehiculo", idVehiculoFinal);
            manto.put("placa", placaFinal);

            // DATOS TÉCNICOS Y FINANCIEROS
            manto.put("descripcion", desc);
            manto.put("kilometraje_mantenimiento", Integer.parseInt(kmStr));
            manto.put("costo", costoStr.isEmpty() ? 0 : Double.parseDouble(costoStr));

            // MANEJO DE FECHAS: Conversión a objetos Timestamp para facilitar consultas por rango
            manto.put("fecha_programada", new Timestamp(sdf.parse(fProgStr)));

            // Lógica condicional: Si no hay fecha real, el mantenimiento queda como 'Pendiente'
            if (!fRealStr.isEmpty()) {
                manto.put("fecha_realizacion", new Timestamp(sdf.parse(fRealStr)));
            } else {
                manto.put("fecha_realizacion", null);
            }

            // Inserción asíncrona en la colección 'mantenimientos'
            db.collection("mantenimientos")
                    .add(manto)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this, "✅ Mantenimiento guardado", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnGuardar.setEnabled(true);
                        btnGuardar.setText("Guardar");
                        Toast.makeText(this, "❌ Error al guardar", Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            btnGuardar.setEnabled(true);
            btnGuardar.setText("Guardar");
            Toast.makeText(this, "❌ Error en el formato de datos", Toast.LENGTH_SHORT).show();
        }
    }
}