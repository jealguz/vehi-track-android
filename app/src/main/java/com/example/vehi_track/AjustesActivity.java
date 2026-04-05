package com.example.vehi_track;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Actividad encargada de la configuración del perfil, seguridad y metadatos de la aplicación.
 * Hereda de BaseActivity para mantener la persistencia del menú lateral (Navigation Drawer).
 */
public class AjustesActivity extends BaseActivity {

    // Instancia para gestionar la autenticación y seguridad con Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. INFLADO DINÁMICO
        // Utiliza el método heredado de BaseActivity para insertar el diseño de ajustes
        // dentro del contenedor principal que tiene el menú de hamburguesa.
        establecerContenido(R.layout.activity_ajustes);

        // Inicialización del servicio de autenticación de Google Firebase
        mAuth = FirebaseAuth.getInstance();

        // 2. CONFIGURACIÓN DE LA BARRA DE HERRAMIENTAS (TOOLBAR)
        Toolbar toolbar = findViewById(R.id.toolbarAjustes);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Ajustes y Perfil");
            }

            // VINCULACIÓN CON EL DRAWER (MENÚ LATERAL)
            // Se configura el 'Toggle' que permite abrir/cerrar el menú desde el icono de hamburguesa.
            androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState(); // Sincroniza el estado del icono con el estado del menú
        }

        // --- LÓGICA DE COMPONENTES Y EVENTOS ---

        // 1. GESTIÓN DE SEGURIDAD: RESTABLECER CONTRASEÑA
        // Lógica para enviar un correo de recuperación directamente desde la plataforma de Firebase.
        if (findViewById(R.id.btnCambiarClave) != null) {
            findViewById(R.id.btnCambiarClave).setOnClickListener(v -> {
                if (mAuth.getCurrentUser() != null) {
                    // Obtiene el email del usuario logueado actualmente
                    String email = mAuth.getCurrentUser().getEmail();
                    // Invoca el servicio de recuperación de Firebase Auth
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Feedback positivo al usuario tras el envío exitoso
                            Toast.makeText(this, "Se ha enviado un correo a " + email + " para restablecer tu clave.", Toast.LENGTH_LONG).show();
                        } else {
                            // Captura y muestra errores de red o de servicio
                            Toast.makeText(this, "Error al enviar correo: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        // 2. INFORMACIÓN DEL SISTEMA: ACERCA DE
        // Evento para desplegar la información de autoría y versión de la aplicación.
        if (findViewById(R.id.tvAcercaDe) != null) {
            findViewById(R.id.tvAcercaDe).setOnClickListener(v -> mostrarAcercaDe());
        }

        // 3. ESCALABILIDAD: EDITAR PERFIL
        // Implementación de marcador de posición (Placeholder) para futuras versiones del software.
        if (findViewById(R.id.btnEditarPerfil) != null) {
            findViewById(R.id.btnEditarPerfil).setOnClickListener(v ->
                    Toast.makeText(this, "Función disponible en la próxima actualización", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Genera un cuadro de diálogo (AlertDialog) con los créditos del desarrollador.
     * Mejora la transparencia y el soporte técnico hacia el usuario final.
     */
    private void mostrarAcercaDe() {
        new AlertDialog.Builder(this)
                .setTitle("Vehi-Track")
                .setIcon(R.mipmap.vehitrack4) // Icono institucional de la app
                .setMessage("Desarrollado por SENA ADSO\n\nSoftware para el control preventivo de vehículos y gestión de gastos.\nCOLOMBIA - 2026")
                .setPositiveButton("Cerrar", null) // Botón neutro para descartar el diálogo
                .show();
    }
}