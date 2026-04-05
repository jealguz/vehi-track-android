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
 * Esta clase sirve como base para que todas las pantallas tengan el menú lateral.
 */
public abstract class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected FirebaseAuth mAuth; // La pongo protected para que tus otras clases la usen si quieren
    protected FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // INICIALIZACIÓN CORRECTA (Dentro de un método)
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // Este método "inyecta" tu página dentro del menú
    protected void establecerContenido(int layoutResID) {
        setContentView(R.layout.activity_base);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        FrameLayout contenedor = findViewById(R.id.contenedor_paginas);

        if (getLayoutInflater() != null) {
            getLayoutInflater().inflate(layoutResID, contenedor, true);
        }

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;

        if (id == R.id.nav_inicio) {
            // Ya estamos aquí o puedes ir al Home
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
            mAuth.signOut();
            intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }

        if (intent != null) {
            startActivity(intent);
        }

        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }
}