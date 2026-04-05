package com.example.vehi_track;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends BaseActivity {

    private EditText emailEditText, nombreEditText, apellidoEditText, passwordEditText;
    private Button continuarButton;
    private TextView loginLink;

    // Instancias de Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        // 1. Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 2. Enlazar vistas (Asegúrate que los IDs coincidan con tu XML)
        nombreEditText = findViewById(R.id.editTextNombre); // Cambiar ID si es diferente en tu XML
        apellidoEditText = findViewById(R.id.editTextApellido); // Cambiar ID si es diferente
        emailEditText = findViewById(R.id.editTextTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword); // Cambiar ID si es diferente
        continuarButton = findViewById(R.id.buttonContinuarRegistro);
        loginLink = findViewById(R.id.textView2);

        // 3. Lógica para el botón Continuar
        continuarButton.setOnClickListener(v -> {
            validarYRegistrar();
        });

        // 4. Lógica para el enlace de regreso a Login
        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void validarYRegistrar() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String nombre = nombreEditText.getText().toString().trim();
        String apellido = apellidoEditText.getText().toString().trim();

        // Validaciones básicas
        if (nombre.isEmpty() || apellido.isEmpty() || password.isEmpty()) {
            showCustomErrorModal("Por favor, completa todos los campos.");
            return;
        }

        if (!isValidEmail(email)) {
            showCustomErrorModal("El formato del correo no es correcto.");
            return;
        }

        if (password.length() < 6) {
            showCustomErrorModal("La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        // --- PROCESO DE REGISTRO EN FIREBASE ---

        // A. Crear usuario en Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Obtener el ID único (UID) generado por Firebase
                        String userId = mAuth.getCurrentUser().getUid();

                        // B. Crear mapa de datos para Firestore (Igual a tu estructura de consola)
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("nombre", nombre);
                        userMap.put("apellido", apellido);
                        userMap.put("email", email);

                        // C. Guardar en la colección "usuarios" usando el UID como ID del documento
                        db.collection("usuarios").document(userId)
                                .set(userMap)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(RegisterActivity.this, "¡Registro Exitoso!", Toast.LENGTH_SHORT).show();
                                    // Aquí podrías ir a la pantalla de "Registrar Vehículo"
                                    // Intent intent = new Intent(RegisterActivity.this, RegisterVehicleActivity.class);
                                    // startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    showCustomErrorModal("Error al guardar datos: " + e.getMessage());
                                });

                    } else {
                        // Error en Authentication (ej: el correo ya existe)
                        showCustomErrorModal("Error: " + task.getException().getMessage());
                    }
                });
    }

    private void showCustomErrorModal(String message) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_errmail);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);
        }

        TextView titleTextView = dialog.findViewById(R.id.dialogTitle);
        TextView messageTextView = dialog.findViewById(R.id.dialogMessage);
        Button continueButton = dialog.findViewById(R.id.dialogButtonContinue);

        titleTextView.setText("Atención");
        messageTextView.setText(message);

        continueButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return email != null && pattern.matcher(email).matches();
    }
}