package com.example.vehi_track;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Clase base abstracta que implementa el patrón de diseño "Template Method".
 * Centraliza el menú de navegación lateral (Drawer) y la conexión a servicios en la nube
 * para que todas las actividades hijas hereden estas funcionalidades automáticamente.
 */
public abstract class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Componentes de la interfaz de usuario para la navegación
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;

    // Servicios de Firebase protegidos para ser accedidos directamente por las actividades hijas
    protected FirebaseAuth mAuth;
    protected FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // INICIALIZACIÓN GLOBAL DE SERVICIOS
        // Al inicializar aquí Firebase, garantizamos que cualquier pantalla de la app
        // tenga acceso a la base de datos y autenticación desde su nacimiento.
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Método de inyección de contenido.
     * Reemplaza el sistema estándar de setContentView para permitir que el layout
     * de cada actividad se inserte dentro del FrameLayout global de la base.
     * @param layoutResID Identificador del recurso layout de la pantalla hija.
     */
    protected void establecerContenido(int layoutResID) {
        // Se establece el layout base que contiene el DrawerLayout y el FrameLayout
        setContentView(R.layout.activity_base);

        // Vinculación de los componentes del menú lateral
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // El contenedor es donde se "dibujará" el contenido específico de cada actividad
        FrameLayout contenedor = findViewById(R.id.contenedor_paginas);

        // Inyección dinámica del layout hijo dentro del contenedor base
        if (getLayoutInflater() != null) {
            getLayoutInflater().inflate(layoutResID, contenedor, true);
        }

        // Asignación del escuchador de eventos para las opciones del menú
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * Manejador centralizado de eventos de navegación.
     * Gestiona el enrutamiento de la aplicación hacia los diferentes módulos.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;

        // --- LÓGICA DE ENRUTAMIENTO POR MÓDULOS ---

        if (id == R.id.nav_inicio) {
            // Acción para el inicio (Home)
        } else if (id == R.id.nav_reportes || id == R.id.nav_reporte_pdf) {
            intent = new Intent(this, ReportesActivity.class);
        } else if (id == R.id.nav_vehiculo) {
            intent = new Intent(this, MisVehiculosActivity.class);
        } else if (id == R.id.nav_registrar_vehiculo) {
            intent = new Intent(this, RegistrarVehiculoActivity.class);
        } else if (id == R.id.nav_mis_mantenimientos) {
            intent = new Intent(this, ListarMantenimientosActivity.class);
        } else if (id == R.id.nav_registrar_mantenimiento) {
            intent = new Intent(this, RegistrarMantenimientoActivity.class);
        } else if (id == R.id.nav_gasto_combustible) {
            intent = new Intent(this, ListarCombustibleActivity.class);
        } else if (id == R.id.nav_registrar_combustible) {
            intent = new Intent(this, RegistrarCombustibleActivity.class);
        } else if (id == R.id.nav_talleres_cercanos) {
            intent = new Intent(this, TalleresCercanosActivity.class);
        } else if (id == R.id.nav_notificaciones) {
            intent = new Intent(this, NotificacionesActivity.class);
        } else if (id == R.id.nav_ajustes) {
            intent = new Intent(this, AjustesActivity.class);
        } else if (id == R.id.nav_cerrar_sesion) {
            // --- CIERRE DE SESIÓN SEGURO ---
            mAuth.signOut();
            intent = new Intent(this, MainActivity.class);
            // Flags para limpiar el historial de actividades y evitar que el usuario regrese con el botón atrás
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }

        // Ejecución del cambio de pantalla si se seleccionó una opción válida
        if (intent != null) {
            startActivity(intent);
        }

        // Cierre automático del panel lateral tras la selección
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }
}