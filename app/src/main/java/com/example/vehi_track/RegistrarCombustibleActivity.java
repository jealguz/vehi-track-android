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
 * Actividad para el registro de consumo de combustible.
 * Gestiona la entrada de datos técnicos (galones, costo, kilometraje) y los vincula
 * a un vehículo específico mediante una relación en Firestore.
 */
public class RegistrarCombustibleActivity extends BaseActivity {

    // Componentes de formulario
    private EditText etFecha, etCantidad, etCosto, etKm;
    private AutoCompleteTextView spinnerVehiculo;
    private TextInputLayout layoutSelector;
    private MaterialButton btnGuardar;

    // Servicios y variables de estado
    private FirebaseFirestore db;
    private String idVehiculo, idUsuario, placaVehiculo;
    private List<vehiculo> listaVehiculosParaSeleccionar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. INFLADO CON ARQUITECTURA DE HERENCIA (BaseActivity)
        establecerContenido(R.layout.activity_registrar_combustible);

        db = FirebaseFirestore.getInstance();
        idUsuario = FirebaseAuth.getInstance().getUid();

        // Inicialización de componentes UI
        etFecha = findViewById(R.id.etFechaCombustible);
        etCantidad = findViewById(R.id.etGalones);
        etCosto = findViewById(R.id.etCostoCombustible);
        etKm = findViewById(R.id.etKmCombustible);
        btnGuardar = findViewById(R.id.btnGuardarCombustible);
        spinnerVehiculo = findViewById(R.id.spinnerVehiculoComb);
        layoutSelector = findViewById(R.id.layoutSelectorVehiculoComb);

        // 2. CONFIGURACIÓN DE BARRA DE HERRAMIENTAS Y MENÚ
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Registrar Tanqueo");

            // Sincronización con el menú lateral de la aplicación
            androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        // 3. LÓGICA DE INTERFAZ DINÁMICA (UX):
        // Se recupera el contexto de navegación desde el Intent.
        idVehiculo = getIntent().getStringExtra("idVehiculo");
        placaVehiculo = getIntent().getStringExtra("placa");

        if (idVehiculo != null) {
            // OPTIMIZACIÓN: Si el usuario ya seleccionó una moto previamente, se oculta el selector
            // para agilizar el proceso de registro (Evita redundancia).
            layoutSelector.setVisibility(View.GONE);
        } else {
            // Si el acceso es general, se consultan los vehículos del usuario para llenar el selector.
            cargarVehiculosUsuario();
        }

        // 4. CONFIGURACIÓN DE EVENTOS (LISTENERS)
        etFecha.setOnClickListener(v -> mostrarDatePicker());
        btnGuardar.setOnClickListener(v -> guardarEnFirestore());

        // Botón opcional de retorno rápido
        if (findViewById(R.id.btnBack) != null) {
            findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        }
    }

    /**
     * Carga de datos para el Selector (Dropdown):
     * Recupera la flota del usuario para poblar el AutoCompleteTextView.
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
                            // Formato amigable para el usuario: "Placa - Marca"
                            nombres.add(v.getPlaca() + " - " + v.getMarca());
                        }
                    }
                    // Uso de adaptador estándar para el despliegue de la lista
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1, nombres);
                    spinnerVehiculo.setAdapter(adapter);

                    // Captura de selección para obtener el ID real del vehículo
                    spinnerVehiculo.setOnItemClickListener((parent, view, position, id) -> {
                        vehiculo sel = listaVehiculosParaSeleccionar.get(position);
                        idVehiculo = sel.getId_vehiculo();
                        placaVehiculo = sel.getPlaca();
                    });
                });
    }

    /**
     * Interfaz de Selección de Fecha:
     * Despliega un componente nativo de Android para asegurar el formato de fecha correcto.
     */
    private void mostrarDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            // Formateo de fecha con ceros a la izquierda (dd/MM/yyyy)
            String fechaSel = String.format(Locale.getDefault(), "%02d/%02d/%d", day, (month + 1), year);
            etFecha.setText(fechaSel);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Persistencia de Datos:
     * Valida, parsea y envía la información del tanqueo a Firebase Firestore.
     */
    private void guardarEnFirestore() {
        String fechaStr = etFecha.getText().toString().trim();
        String cantStr = etCantidad.getText().toString().trim();
        String costoStr = etCosto.getText().toString().trim();
        String kmStr = etKm.getText().toString().trim();

        // VALIDACIÓN DE SEGURIDAD: Previene documentos incompletos en la DB
        if(fechaStr.isEmpty() || cantStr.isEmpty() || costoStr.isEmpty() || kmStr.isEmpty() || idVehiculo == null){
            Toast.makeText(this, "⚠️ Selecciona un vehículo y completa los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // CONVERSIÓN DE TIPOS: Paso de String a Date y luego a Timestamp de Google
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date fechaDate = sdf.parse(fechaStr);
            Timestamp fechaTimestamp = new Timestamp(fechaDate);

            // MAPEADO DE DATOS: Estructura de mapa para asegurar los tipos numéricos en Firestore
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("fecha", fechaTimestamp);
            data.put("cantidad", Float.parseFloat(cantStr)); // Litros/Galones como Float
            data.put("costo", Float.parseFloat(costoStr));     // Moneda como Float
            data.put("kilometraje", Integer.parseInt(kmStr)); // Kilómetros como Integer
            data.put("id_vehiculo", idVehiculo);
            data.put("placa", placaVehiculo);
            data.put("id_usuario", idUsuario);

            // UX: Desactivar botón para evitar registros duplicados por clics múltiples
            btnGuardar.setEnabled(false);
            btnGuardar.setText("Guardando...");

            db.collection("combustible")
                    .add(data)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this, "✅ Tanqueo registrado correctamente", Toast.LENGTH_SHORT).show();
                        finish(); // Cierra la actividad al completar la tarea
                    })
                    .addOnFailureListener(e -> {
                        btnGuardar.setEnabled(true);
                        btnGuardar.setText("Guardar");
                        Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (ParseException | NumberFormatException e) {
            // Gestión de errores de entrada de usuario
            Toast.makeText(this, "❌ Revisa los números y la fecha", Toast.LENGTH_SHORT).show();
        }
    }
}