package com.example.vehi_track;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;

public class AjustesActivity extends BaseActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inflar el contenido usando el método de la BaseActivity
        establecerContenido(R.layout.activity_ajustes);

        mAuth = FirebaseAuth.getInstance();

        // 2. Configurar la Toolbar con el menú de hamburguesa
        Toolbar toolbar = findViewById(R.id.toolbarAjustes);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Ajustes y Perfil");
            }

            // Vincular con el DrawerLayout de la BaseActivity
            androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        // --- LÓGICA DE BOTONES (Tu código original impecable) ---

        // 1. Restablecer Contraseña
        if (findViewById(R.id.btnCambiarClave) != null) {
            findViewById(R.id.btnCambiarClave).setOnClickListener(v -> {
                if (mAuth.getCurrentUser() != null) {
                    String email = mAuth.getCurrentUser().getEmail();
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Se ha enviado un correo a " + email + " para restablecer tu clave.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Error al enviar correo: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        // 2. Acerca de
        if (findViewById(R.id.tvAcercaDe) != null) {
            findViewById(R.id.tvAcercaDe).setOnClickListener(v -> mostrarAcercaDe());
        }

        // 3. Editar Perfil
        if (findViewById(R.id.btnEditarPerfil) != null) {
            findViewById(R.id.btnEditarPerfil).setOnClickListener(v ->
                    Toast.makeText(this, "Función disponible en la próxima actualización", Toast.LENGTH_SHORT).show());
        }
    }

    private void mostrarAcercaDe() {
        new AlertDialog.Builder(this)
                .setTitle("Vehi-Track")
                .setIcon(R.mipmap.ic_launcher) // Opcional: añade el icono de tu app
                .setMessage("Desarrollado por Jeison Alvin Guzman\n\nSoftware para el control preventivo de vehículos y gestión de gastos.\nGuavatá, Santander - 2026")
                .setPositiveButton("Cerrar", null)
                .show();
    }
}