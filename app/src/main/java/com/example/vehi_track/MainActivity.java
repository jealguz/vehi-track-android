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

public class MainActivity extends AppCompatActivity {

    private EditText emailET, passwordET;
    private Button btnLogin, btnRegister;
    private ImageButton btnTogglePass;
    private boolean isPasswordVisible = false;

    // Instancia de Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 2. Enlazar con los IDs de tu XML
        emailET = findViewById(R.id.editTextTextEmail);
        passwordET = findViewById(R.id.editTextTextPassword);
        btnLogin = findViewById(R.id.buttoniniciar);
        btnRegister = findViewById(R.id.button2);
        btnTogglePass = findViewById(R.id.passwordToggle);

        // 3. Lógica para ver/ocultar contraseña
        btnTogglePass.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnTogglePass.setImageResource(R.drawable.ic_visibility_off_black_24dp);
            } else {
                passwordET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnTogglePass.setImageResource(R.drawable.icono_ojo); // Asegúrate de tener este icono
            }
            isPasswordVisible = !isPasswordVisible;
            passwordET.setSelection(passwordET.length()); // Mantiene el cursor al final
        });

        // 4. Lógica del Botón Iniciar (Login)
        btnLogin.setOnClickListener(v -> {
            String email = emailET.getText().toString().trim();
            String pass = passwordET.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                showError("Por favor, completa los campos.");
            } else {
                validarUsuario(email, pass);
            }
        });

        // 5. Lógica del Botón Registrarse
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void validarUsuario(String email, String pass) {
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Acceso concedido", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(MainActivity.this, DashboardActivity.class);
                        startActivity(i);

                        // Opcional: cerramos el login para que no se devuelva al darle atrás
                        finish();
                    } else {
                        showError("Correo o contraseña incorrectos.");
                    }
                });
    }



    private void showError(String message) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_errmail);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }

        TextView msg = dialog.findViewById(R.id.dialogMessage);
        Button btn = dialog.findViewById(R.id.dialogButtonContinue);
        msg.setText(message);

        btn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}