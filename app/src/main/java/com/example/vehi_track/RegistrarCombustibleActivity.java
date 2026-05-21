package com.example.vehi_track;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.example.vehi_track.models.Combustible;
import com.example.vehi_track.models.vehiculo;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Actividad: Registro de Consumo de Combustible.
 * Objetivo: Capturar datos técnicos de tanqueo y persistirlos en Firestore.
 * Corregido: Se utiliza Double para asegurar compatibilidad total con Firebase y evitar errores de casteo.
 * @author Jeison Guzman
 */
public class RegistrarCombustibleActivity extends BaseActivity {

    // Componentes visuales del formulario
    private EditText etFecha, etCantidad, etCosto, etKm;
    private AutoCompleteTextView spinnerVehiculo;
    private TextInputLayout layoutSelector;
    private MaterialButton btnGuardar;

    // Servicios de backend y control de datos
    private FirebaseFirestore db;
    private String idVehiculo, idUsuario, placaVehiculo;
    private List<vehiculo> listaVehiculosParaSeleccionar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. HERENCIA Y UI: Inflado de vista mediante el método de BaseActivity
        establecerContenido(R.layout.activity_registrar_combustible);

        db = FirebaseFirestore.getInstance();
        idUsuario = FirebaseAuth.getInstance().getUid();

        // Vinculación de componentes con el XML
        etFecha = findViewById(R.id.etFechaCombustible);
        etCantidad = findViewById(R.id.etGalones);
        etCosto = findViewById(R.id.etCostoCombustible);
        etKm = findViewById(R.id.etKmCombustible);
        btnGuardar = findViewById(R.id.btnGuardarCombustible);
        spinnerVehiculo = findViewById(R.id.spinnerVehiculoComb);
        layoutSelector = findViewById(R.id.layoutSelectorVehiculoComb);

        // 2. NAVEGACIÓN: Configuración de Toolbar sincronizada con el Drawer
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Registrar Tanqueo");

            androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        // 3. LÓGICA DE CONTEXTO:
        // Si venimos de la pantalla "Detalle de Vehículo", ya tenemos el ID y la Placa.
        idVehiculo = getIntent().getStringExtra("idVehiculo");
        placaVehiculo = getIntent().getStringExtra("placa");

        if (idVehiculo != null) {
            // Se oculta el selector para mejorar la experiencia de usuario (UX) si el vehículo ya es conocido.
            layoutSelector.setVisibility(View.GONE);
        } else {
            // Si el usuario entra directamente, cargamos su lista de vehículos.
            cargarVehiculosUsuario();
        }

        // 4. LISTENERS: Eventos de interacción
        etFecha.setOnClickListener(v -> mostrarDatePicker());
        btnGuardar.setOnClickListener(v -> guardarEnFirestore());
    }

    /**
     * Consultas a Firestore:
     * Obtiene los vehículos vinculados al UID del usuario activo para llenar el dropdown.
     */
    private void cargarVehiculosUsuario() {
        listaVehiculosParaSeleccionar = new ArrayList<>();
        db.collection("vehiculos")
                .whereEqualTo("id_usuario", idUsuario)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> nombres = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        vehiculo v = doc.toObject(vehiculo.class);
                        if (v != null) {
                            v.setId_vehiculo(doc.getId());
                            listaVehiculosParaSeleccionar.add(v);
                            nombres.add(v.getPlaca() + " - " + v.getMarca());
                        }
                    }
                    // Adaptador para el selector visual
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1, nombres);
                    spinnerVehiculo.setAdapter(adapter);

                    spinnerVehiculo.setOnItemClickListener((parent, view, position, id) -> {
                        vehiculo sel = listaVehiculosParaSeleccionar.get(position);
                        idVehiculo = sel.getId_vehiculo();
                        placaVehiculo = sel.getPlaca();
                    });
                });
    }

    /**
     * Selector de Fecha:
     * Implementa un DatePickerDialog para evitar errores manuales en el formato de fecha.
     */
    private void mostrarDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String fechaSel = String.format(Locale.getDefault(), "%02d/%02d/%d", day, (month + 1), year);
            etFecha.setText(fechaSel);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Lógica Principal de Persistencia:
     * Procesa la información, realiza el parseo de tipos y guarda en la nube.
     */
    private void guardarEnFirestore() {
        String fechaStr = etFecha.getText().toString().trim();
        String cantStr = etCantidad.getText().toString().trim();
        String costoStr = etCosto.getText().toString().trim();
        String kmStr = etKm.getText().toString().trim();

        // Validación de integridad de datos
        if(fechaStr.isEmpty() || cantStr.isEmpty() || costoStr.isEmpty() || kmStr.isEmpty() || idVehiculo == null){
            Toast.makeText(this, "⚠️ Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Conversión de formato de fecha a Timestamp (requerido para ordenamiento en Firestore)
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date fechaDate = sdf.parse(fechaStr);
            Timestamp fechaTimestamp = new Timestamp(fechaDate);

            /**
             * IMPORTANTE: Se utiliza Double.parseDouble() para cantidad y costo.
             * Esto soluciona el error Fatal Exception de deserialización, alineando
             * los datos con el tipo 'Number' de Firestore y el modelo Java.
             */
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("fecha", fechaTimestamp);
            data.put("cantidad", Double.parseDouble(cantStr)); // Cambio a Double
            data.put("costo", Double.parseDouble(costoStr));     // Cambio a Double
            data.put("kilometraje", Integer.parseInt(kmStr));
            data.put("id_vehiculo", idVehiculo);
            data.put("placa", placaVehiculo);
            data.put("id_usuario", idUsuario);

            btnGuardar.setEnabled(false);
            btnGuardar.setText("Guardando...");

            db.collection("combustible")
                    .add(data)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this, "✅ Tanqueo registrado exitosamente", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnGuardar.setEnabled(true);
                        btnGuardar.setText("Guardar");
                        Toast.makeText(this, "❌ Error de conexión", Toast.LENGTH_SHORT).show();
                    });

        } catch (ParseException | NumberFormatException e) {
            Toast.makeText(this, "❌ Formato numérico inválido", Toast.LENGTH_SHORT).show();
        }
    }
}