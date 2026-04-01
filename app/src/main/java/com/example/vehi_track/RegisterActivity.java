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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button continuarButton;
    private TextView loginLink; // El enlace "Tienes una cuenta ingresa aquí"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Usamos el layout que indicaste para el Registro
        setContentView(R.layout.register);

        // 1. Enlazar vistas del layout de Registro
        emailEditText = findViewById(R.id.editTextTextEmail);
        continuarButton = findViewById(R.id.buttonContinuarRegistro);
        loginLink = findViewById(R.id.textView2);

        // 2. Lógica para el botón Continuar (Validación y Modal)
        continuarButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            if (!isValidEmail(email)) {
                // Si el email es inválido, mostrar el modal
                showCustomErrorModal("La dirección de correo ingresada no tiene el formato correcto. Por favor, revisa e intenta de nuevo.");
            } else {
                // Email válido
                Toast.makeText(RegisterActivity.this, "¡Email válido! Continuar con el registro.", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. Lógica para el enlace de regreso a Login
        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Cierra esta actividad de registro
        });
    }

    /**
     * Muestra el modal de error.
     */
    private void showCustomErrorModal(String message) {
        // 1. Instanciar el Dialog. Usamos 'this' (la Activity) como contexto.
        final Dialog dialog = new Dialog(this);

        // 2. IMPORTANTE: Esconder el título por defecto del diálogo.
        // Esto DEBE ir antes de setContentView().
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 3. Asignar el layout personalizado (Tu layout del modal)
        dialog.setContentView(R.layout.dialog_errmail);

        // 4. Control de TAMAÑO y POSICIÓN
        if (dialog.getWindow() != null) {

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());

            layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

            dialog.getWindow().setAttributes(layoutParams);
        }

        // 5. Enlazar y configurar los elementos del modal
        TextView titleTextView = dialog.findViewById(R.id.dialogTitle);
        TextView messageTextView = dialog.findViewById(R.id.dialogMessage);
        Button continueButton = dialog.findViewById(R.id.dialogButtonContinue);

        titleTextView.setText("Error de Correo Electrónico");
        messageTextView.setText(message);

        // 6. Configurar la acción del botón "CONTINUAR" dentro del modal
        continueButton.setOnClickListener(v -> {
            emailEditText.requestFocus();
            dialog.dismiss();
        });

        // 7. Mostrar el diálogo
        dialog.show();
    }

    /**
     * Verifica si la cadena de texto tiene un formato de correo electrónico válido.
     */
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(emailRegex);

        if (email == null || email.isEmpty()) {
            return false;
        }

        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
