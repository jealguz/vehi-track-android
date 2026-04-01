package com.example.vehi_track;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Usamos el layout principal para el Login
        setContentView(R.layout.activity_main);

        // 1. Enlazar el botón 'Registrarse'
        Button registrarseButton = findViewById(R.id.button2);

        // 2. Manejar la navegación del Login al Registro
        registrarseButton.setOnClickListener(v -> {
            // Navega a la Activity de Registro (RegisterActivity)
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}