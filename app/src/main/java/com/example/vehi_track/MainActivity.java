package com.example.vehi_track;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Actividad de Autenticación (Login).
 * Punto de entrada principal de la aplicación que gestiona el acceso de usuarios
 * mediante el servicio de Firebase Authentication.
 */
public class MainActivity extends AppCompatActivity {

    // Componentes de entrada de datos
    private EditText emailET, passwordET;
    private Button btnLogin, btnRegister;
    private ImageButton btnTogglePass;

    // Estado de control para la máscara de la contraseña
    private boolean isPasswordVisible = false;

    // Instancia del motor de autenticación de Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. INICIALIZACIÓN DE SERVICIOS
        // Se conecta con el nodo de autenticación de Google para validar credenciales.
        mAuth = FirebaseAuth.getInstance();

        // 2. VINCULACIÓN DE COMPONENTES (UI)
        emailET = findViewById(R.id.editTextTextEmail);
        passwordET = findViewById(R.id.editTextTextPassword);
        btnLogin = findViewById(R.id.buttoniniciar);
        btnRegister = findViewById(R.id.button2);
        btnTogglePass = findViewById(R.id.passwordToggle);

        // 3. LÓGICA DE USABILIDAD: VISIBILIDAD DE CONTRASEÑA
        // Permite al usuario verificar su entrada mediante el cambio dinámico del InputType.
        btnTogglePass.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Modo Oculto: Máscara de puntos
                passwordET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnTogglePass.setImageResource(R.drawable.ic_visibility_off_black_24dp);
            } else {
                // Modo Visible: Texto plano
                passwordET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnTogglePass.setImageResource(R.drawable.icono_ojo);
            }
            isPasswordVisible = !isPasswordVisible;
            // IMPORTANTE: Se resitúa el cursor al final del texto para no interrumpir al usuario.
            passwordET.setSelection(passwordET.length());
        });

        // 4. LÓGICA DE VALIDACIÓN DE FORMULARIO
        btnLogin.setOnClickListener(v -> {
            String email = emailET.getText().toString().trim();
            String pass = passwordET.getText().toString().trim();

            // Validación de campos vacíos antes de realizar peticiones de red (Ahorro de recursos)
            if (email.isEmpty() || pass.isEmpty()) {
                showError("Por favor, completa los campos.");
            } else {
                validarUsuario(email, pass);
            }
        });

        // 5. NAVEGACIÓN HACIA REGISTRO
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Proceso de Autenticación con Firebase:
     * Realiza una petición asíncrona al servidor para validar correo y clave.
     */
    private void validarUsuario(String email, String pass) {
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // ACCESO EXITOSO: Redirección al panel de control (Dashboard)
                        Toast.makeText(this, "Acceso concedido", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(MainActivity.this, DashboardActivity.class);
                        startActivity(i);

                        // SEGURIDAD: Se finaliza la actividad actual para que el usuario
                        // no pueda regresar al Login con el botón "Atrás" del sistema.
                        finish();
                    } else {
                        // FEEDBACK DE ERROR: Información clara en caso de fallo de credenciales
                        showError("Correo o contraseña incorrectos.");
                    }
                });
    }

    /**
     * Sistema de Notificación Personalizado (Custom Dialog):
     * Reemplaza el diseño estándar por un diálogo institucional con fondo translúcido.
     * Mejora la identidad visual de Vehi-Track.
     */
    private void showError(String message) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_errmail); // Layout personalizado del error

        if (dialog.getWindow() != null) {
            // Se aplica fondo transparente para respetar las esquinas redondeadas del XML
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Ajuste de dimensiones dinámico (85% del ancho de la pantalla)
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }

        // Asignación del mensaje dinámico en el diálogo
        TextView msg = dialog.findViewById(R.id.dialogMessage);
        Button btn = dialog.findViewById(R.id.dialogButtonContinue);
        msg.setText(message);

        // Acción para descartar el aviso
        btn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}