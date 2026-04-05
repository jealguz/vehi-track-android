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

public class RegistrarMantenimientoActivity extends BaseActivity {

    private EditText etFechaProg, etFechaReal, etDescManto, etKmManto, etCostoManto;
    private AutoCompleteTextView spinnerVehiculos;
    private TextInputLayout layoutSelector;
    private MaterialButton btnGuardar;
    private FirebaseFirestore db;
    private String idVehiculoFinal, placaFinal, idUsuario;
    private List<vehiculo> listaVehiculosCargados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        establecerContenido(R.layout.activity_registrar_mantenimiento);

        db = FirebaseFirestore.getInstance();
        idUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 1. Vincular Vistas
        etFechaProg = findViewById(R.id.etFechaProg);
        etFechaReal = findViewById(R.id.etFechaReal);
        etDescManto = findViewById(R.id.etDescManto);
        etKmManto = findViewById(R.id.etKmManto);
        etCostoManto = findViewById(R.id.etCostoManto);
        btnGuardar = findViewById(R.id.btnGuardarManto);
        spinnerVehiculos = findViewById(R.id.spinnerVehiculosManto); // El ID que sugerí para el XML
        layoutSelector = findViewById(R.id.layoutSeleccionarVehiculo);

        // 2. ¿De dónde viene el vehículo?
        idVehiculoFinal = getIntent().getStringExtra("idVehiculo");
        placaFinal = getIntent().getStringExtra("placa");

        if (idVehiculoFinal != null) {
            // Si ya viene de una moto, ocultamos el selector
            layoutSelector.setVisibility(View.GONE);
        } else {
            // Si viene del menú general, cargamos sus vehículos para que elija
            cargarVehiculosDelUsuario();
        }

        // 3. Listeners
        etFechaProg.setOnClickListener(v -> mostrarCalendario(etFechaProg));
        etFechaReal.setOnClickListener(v -> mostrarCalendario(etFechaReal));
        btnGuardar.setOnClickListener(v -> validarYGuardar());

        // Botón atrás (btnBack en tu XML)
        if (findViewById(R.id.btnBack) != null) {
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        }
    }

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

    private void mostrarCalendario(EditText editText) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String fechaSel = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, (month + 1), year);
            editText.setText(fechaSel);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void validarYGuardar() {
        String desc = etDescManto.getText().toString().trim();
        String fProgStr = etFechaProg.getText().toString().trim();
        String fRealStr = etFechaReal.getText().toString().trim();
        String kmStr = etKmManto.getText().toString().trim();
        String costoStr = etCostoManto.getText().toString().trim();

        if (desc.isEmpty() || fProgStr.isEmpty() || kmStr.isEmpty() || idVehiculoFinal == null) {
            Toast.makeText(this, "⚠️ Selecciona un vehículo y llena los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            btnGuardar.setEnabled(false);
            btnGuardar.setText("Guardando...");

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Map<String, Object> manto = new HashMap<>();

            // DATOS DE IDENTIFICACIÓN (Lo que faltaba)
            manto.put("id_usuario", idUsuario);
            manto.put("id_vehiculo", idVehiculoFinal);
            manto.put("placa", placaFinal);

            // DATOS DEL SERVICIO
            manto.put("descripcion", desc);
            manto.put("kilometraje_mantenimiento", Integer.parseInt(kmStr));
            manto.put("costo", costoStr.isEmpty() ? 0 : Double.parseDouble(costoStr));

            // FECHAS COMO TIMESTAMP (Para que Firestore las entienda)
            manto.put("fecha_programada", new Timestamp(sdf.parse(fProgStr)));

            if (!fRealStr.isEmpty()) {
                manto.put("fecha_realizacion", new Timestamp(sdf.parse(fRealStr)));
            } else {
                manto.put("fecha_realizacion", null); // Opcional
            }

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