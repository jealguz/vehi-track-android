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

public class RegistrarCombustibleActivity extends BaseActivity {

    private EditText etFecha, etCantidad, etCosto, etKm;
    private AutoCompleteTextView spinnerVehiculo;
    private TextInputLayout layoutSelector;
    private MaterialButton btnGuardar;
    private FirebaseFirestore db;
    private String idVehiculo, idUsuario, placaVehiculo; // Añadimos placaVehiculo para la DB
    private List<vehiculo> listaVehiculosParaSeleccionar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        establecerContenido(R.layout.activity_registrar_combustible);

        db = FirebaseFirestore.getInstance();
        idUsuario = FirebaseAuth.getInstance().getUid();

        // 1. Inicializar Vistas
        etFecha = findViewById(R.id.etFechaCombustible);
        etCantidad = findViewById(R.id.etGalones);
        etCosto = findViewById(R.id.etCostoCombustible);
        etKm = findViewById(R.id.etKmCombustible);
        btnGuardar = findViewById(R.id.btnGuardarCombustible);
        spinnerVehiculo = findViewById(R.id.spinnerVehiculoComb);
        layoutSelector = findViewById(R.id.layoutSelectorVehiculoComb);

        // 2. Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Registrar Tanqueo");
            androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        // 3. Lógica de selección de vehículo
        idVehiculo = getIntent().getStringExtra("idVehiculo");
        placaVehiculo = getIntent().getStringExtra("placa");

        if (idVehiculo != null) {
            // Si ya sabemos qué vehículo es, ocultamos el selector
            layoutSelector.setVisibility(View.GONE);
        } else {
            // Si viene del menú general, cargamos sus vehículos
            cargarVehiculosUsuario();
        }

        // 4. Listeners
        etFecha.setOnClickListener(v -> mostrarDatePicker());
        btnGuardar.setOnClickListener(v -> guardarEnFirestore());

        if (findViewById(R.id.btnBack) != null) {
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        }
    }

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

    private void mostrarDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String fechaSel = String.format(Locale.getDefault(), "%02d/%02d/%d", day, (month + 1), year);
            etFecha.setText(fechaSel);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void guardarEnFirestore() {
        String fechaStr = etFecha.getText().toString().trim();
        String cantStr = etCantidad.getText().toString().trim();
        String costoStr = etCosto.getText().toString().trim();
        String kmStr = etKm.getText().toString().trim();

        if(fechaStr.isEmpty() || cantStr.isEmpty() || costoStr.isEmpty() || kmStr.isEmpty() || idVehiculo == null){
            Toast.makeText(this, "⚠️ Selecciona un vehículo y completa los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date fechaDate = sdf.parse(fechaStr);
            Timestamp fechaTimestamp = new Timestamp(fechaDate);

            // Creamos el mapa para enviar (más flexible que el modelo si quieres asegurar tipos)
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("fecha", fechaTimestamp);
            data.put("cantidad", Float.parseFloat(cantStr));
            data.put("costo", Float.parseFloat(costoStr));
            data.put("kilometraje", Integer.parseInt(kmStr));
            data.put("id_vehiculo", idVehiculo);
            data.put("placa", placaVehiculo);
            data.put("id_usuario", idUsuario);

            btnGuardar.setEnabled(false);
            btnGuardar.setText("Guardando...");

            db.collection("combustible")
                    .add(data)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this, "✅ Tanqueo registrado correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnGuardar.setEnabled(true);
                        btnGuardar.setText("Guardar");
                        Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (ParseException | NumberFormatException e) {
            Toast.makeText(this, "❌ Revisa los números y la fecha", Toast.LENGTH_SHORT).show();
        }
    }
}