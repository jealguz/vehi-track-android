package com.example.vehi_track;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegistrarVehiculoActivity extends BaseActivity {

    private AutoCompleteTextView spinnerTipo;
    private TextInputEditText etMarca, etModelo, etAnio, etKilometraje, etFechaSoat, etFechaMecanica, etPlaca;
    private MaterialButton btnRegistrar, btnCancelar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inflamos con el método de la BaseActivity para tener el menú
        establecerContenido(R.layout.activity_registrar_vehiculo);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // 2. Configurar Toolbar (Hamburguesa)
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Registrar Vehículo");

            androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        // 3. Inicializar vistas
        spinnerTipo = findViewById(R.id.spinnerTipoVehiculo);
        etPlaca = findViewById(R.id.etPlaca); // Asegúrate de tener este ID en tu XML
        etMarca = findViewById(R.id.etMarca);
        etModelo = findViewById(R.id.etModelo);
        etAnio = findViewById(R.id.etAnio);
        etKilometraje = findViewById(R.id.etKilometraje);
        etFechaSoat = findViewById(R.id.etFechaSoat);
        etFechaMecanica = findViewById(R.id.etFechaMecanica);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnCancelar = findViewById(R.id.btnCancelar);

        // 4. Configurar Dropdown
        String[] tipos = {"Carro", "Moto", "Camión", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tipos);
        spinnerTipo.setAdapter(adapter);

        // 5. Calendarios
        etFechaSoat.setOnClickListener(v -> mostrarDatePicker(etFechaSoat));
        etFechaMecanica.setOnClickListener(v -> mostrarDatePicker(etFechaMecanica));

        // 6. Botones
        btnRegistrar.setOnClickListener(v -> validarYGuardar());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void mostrarDatePicker(TextInputEditText editText) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String fecha = String.format(Locale.getDefault(), "%02d/%02d/%d", day, (month + 1), year);
            editText.setText(fecha);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void validarYGuardar() {
        String tipo = spinnerTipo.getText().toString();
        String placa = etPlaca.getText().toString().trim().toUpperCase();
        String marca = etMarca.getText().toString().trim();
        String modelo = etModelo.getText().toString().trim();
        String anioStr = etAnio.getText().toString().trim();
        String kmStr = etKilometraje.getText().toString().trim();

        if (placa.isEmpty() || tipo.isEmpty() || marca.isEmpty() || modelo.isEmpty()) {
            Toast.makeText(this, "⚠️ Completa la placa y los campos principales", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegistrar.setEnabled(false);
        btnRegistrar.setText("Guardando...");

        try {
            String uid = mAuth.getCurrentUser().getUid();
            Map<String, Object> v = new HashMap<>();

            v.put("id_usuario", uid);
            v.put("placa", placa);
            v.put("tipo", tipo);
            v.put("marca", marca);
            v.put("modelo", modelo);

            // Convertimos a número para que coincida con la AKT en la DB
            v.put("anio", Integer.parseInt(anioStr));
            v.put("kilometraje", Integer.parseInt(kmStr));

            // --- CONVERSIÓN DE FECHAS A TIMESTAMP ---
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            if (!etFechaSoat.getText().toString().isEmpty()) {
                Date dateSoat = sdf.parse(etFechaSoat.getText().toString());
                v.put("vencimiento_soat", new Timestamp(dateSoat));
            }

            if (!etFechaMecanica.getText().toString().isEmpty()) {
                Date dateRtm = sdf.parse(etFechaMecanica.getText().toString());
                v.put("vencimiento_rtm", new Timestamp(dateRtm));
            }

            db.collection("vehiculos")
                    .add(v)
                    .addOnSuccessListener(doc -> {
                        // Sincronizamos el ID del documento con el campo id_vehiculo
                        doc.update("id_vehiculo", doc.getId());
                        Toast.makeText(this, "✅ ¡Vehículo registrado exitosamente!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnRegistrar.setEnabled(true);
                        btnRegistrar.setText("Registrar");
                        Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            btnRegistrar.setEnabled(true);
            btnRegistrar.setText("Registrar");
            Toast.makeText(this, "⚠️ Revisa los números y fechas", Toast.LENGTH_SHORT).show();
        }
    }
}