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

/**
 * Actividad de Registro de Nuevos Usuarios.
 * Gestiona la creación de credenciales en Firebase Auth y la persistencia
 * de perfiles detallados en la colección 'usuarios' de Firestore.
 */
public class RegisterActivity extends BaseActivity {

    // Campos de captura de información
    private EditText emailEditText, nombreEditText, apellidoEditText, passwordEditText;
    private Button continuarButton;
    private TextView loginLink;

    // Instancias de los servicios de Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        // 1. INICIALIZACIÓN DE FIREBASE
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 2. VINCULACIÓN DE VISTAS (Mapeo de IDs del XML)
        nombreEditText = findViewById(R.id.editTextNombre);
        apellidoEditText = findViewById(R.id.editTextApellido);
        emailEditText = findViewById(R.id.editTextTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        continuarButton = findViewById(R.id.buttonContinuarRegistro);
        loginLink = findViewById(R.id.textView2);

        // 3. EVENTO DE REGISTRO
        continuarButton.setOnClickListener(v -> {
            validarYRegistrar();
        });

        // 4. FLUJO DE RETORNO AL LOGIN
        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Finaliza para no dejar esta actividad en el historial
        });
    }

    /**
     * Lógica de Validación y Registro:
     * Verifica la integridad de los datos en el cliente antes de procesar en la nube.
     */
    private void validarYRegistrar() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String nombre = nombreEditText.getText().toString().trim();
        String apellido = apellidoEditText.getText().toString().trim();

        // VALIDACIÓN DE CAMPOS OBLIGATORIOS
        if (nombre.isEmpty() || apellido.isEmpty() || password.isEmpty()) {
            showCustomErrorModal("Por favor, completa todos los campos.");
            return;
        }

        // VALIDACIÓN DE FORMATO DE CORREO (Uso de Expresiones Regulares)
        if (!isValidEmail(email)) {
            showCustomErrorModal("El formato del correo no es correcto.");
            return;
        }

        // POLÍTICA DE SEGURIDAD: Mínimo de caracteres para la contraseña
        if (password.length() < 6) {
            showCustomErrorModal("La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        // --- PROCESO ASÍNCRONO EN FIREBASE ---

        // PASO A: Crear la identidad del usuario en Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Se recupera el UID (Identificador Único) generado automáticamente
                        String userId = mAuth.getCurrentUser().getUid();

                        // PASO B: Estructuración del Perfil (Uso de Map para NoSQL)
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("nombre", nombre);
                        userMap.put("apellido", apellido);
                        userMap.put("email", email);

                        // PASO C: Persistencia en Firestore
                        // Se utiliza el mismo UID de Auth como ID del documento para mantener la integridad.
                        db.collection("usuarios").document(userId)
                                .set(userMap)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(RegisterActivity.this, "¡Registro Exitoso!", Toast.LENGTH_SHORT).show();
                                    finish(); // Retorna al Login o Dashboard según el flujo deseado
                                })
                                .addOnFailureListener(e -> {
                                    showCustomErrorModal("Error al guardar datos: " + e.getMessage());
                                });

                    } else {
                        // Manejo de errores de Auth (ej: correo ya registrado)
                        showCustomErrorModal("Error: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Modal de Error Personalizado:
     * Mejora la identidad visual de la app mediante un diálogo con diseño institucional.
     */
    private void showCustomErrorModal(String message) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_errmail);

        if (dialog.getWindow() != null) {
            // Fondo transparente para aplicar el diseño de bordes redondeados del XML
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

    /**
     * Utilidad de Validación de Email:
     * Implementa un patrón Regex estándar para asegurar la validez del dominio y formato.
     */
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return email != null && pattern.matcher(email).matches();
    }
}