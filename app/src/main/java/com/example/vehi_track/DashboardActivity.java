package com.example.vehi_track;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView tvHeaderName, tvHeaderEmail;

    // Elementos de la nueva tarjeta de alertas
    private CardView cardNotif;
    private LinearLayout layoutFondoNotif;
    private TextView tvEstadoNotif, tvBadgeCount;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        View headerView = navigationView.getHeaderView(0);
        tvHeaderName = headerView.findViewById(R.id.tvHeaderName);
        tvHeaderEmail = headerView.findViewById(R.id.tvHeaderEmail);

        // Referencias de la nueva tarjeta
        cardNotif = findViewById(R.id.cardNotificaciones);
        layoutFondoNotif = findViewById(R.id.layoutFondoNotif);
        tvEstadoNotif = findViewById(R.id.tvEstadoNotifDash);
        tvBadgeCount = findViewById(R.id.tvBadgeCount);

        obtenerDatosUsuario();
        configurarAccesosDirectos();
        verificarAlertas(); // <-- Llamamos a la función de alertas

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        });
    }

    private void verificarAlertas() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        // Escuchamos notificaciones de documentos (SOAT/Tecno)
        db.collection("notificaciones")
                .whereEqualTo("id_usuario", uid)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        int conteoNotif = value.size();

                        // También chequeamos mantenimientos pendientes (sin fecha_realizacion)
                        db.collection("mantenimientos")
                                .whereEqualTo("id_usuario", uid)
                                .whereEqualTo("fecha_realizacion", null)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    int totalAlertas = conteoNotif + queryDocumentSnapshots.size();
                                    actualizarInterfazAlertas(totalAlertas);
                                });
                    }
                });
    }

    private void actualizarInterfazAlertas(int total) {
        if (total > 0) {
            // ALERTA: Tienes pendientes (Color Rojo Oscuro)
            layoutFondoNotif.setBackgroundColor(Color.parseColor("#B71C1C"));
            tvEstadoNotif.setText("Tienes " + total + " tareas pendientes");
            tvEstadoNotif.setTextColor(Color.parseColor("#FFCDD2"));
            tvBadgeCount.setText(String.valueOf(total));
            tvBadgeCount.setVisibility(View.VISIBLE);
        } else {
            // OK: Todo al día (Color Azul Marca)
            layoutFondoNotif.setBackgroundColor(Color.parseColor("#0A2351"));
            tvEstadoNotif.setText("Todo se encuentra al día");
            tvEstadoNotif.setTextColor(Color.parseColor("#CCCCCC"));
            tvBadgeCount.setVisibility(View.GONE);
        }
    }

    private void configurarAccesosDirectos() {
        // Clic en la nueva tarjeta de alertas
        cardNotif.setOnClickListener(v ->
                startActivity(new Intent(this, NotificacionesActivity.class)));

        findViewById(R.id.cardMisVehiculos).setOnClickListener(v ->
                startActivity(new Intent(this, MisVehiculosActivity.class)));

        findViewById(R.id.cardRegistrar).setOnClickListener(v ->
                startActivity(new Intent(this, RegistrarVehiculoActivity.class)));

        findViewById(R.id.cardGastos).setOnClickListener(v ->
                startActivity(new Intent(this, ListarCombustibleActivity.class)));

        findViewById(R.id.cardTalleresCerca).setOnClickListener(v ->
                startActivity(new Intent(this, TalleresCercanosActivity.class)));

        findViewById(R.id.cardReportesPdf).setOnClickListener(v ->
                startActivity(new Intent(this, ReportesActivity.class)));

        findViewById(R.id.cardPerfil).setOnClickListener(v ->
                startActivity(new Intent(this, AjustesActivity.class)));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;

        if (id == R.id.nav_inicio) {
            // Ya estamos aquí
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

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void obtenerDatosUsuario() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            tvHeaderEmail.setText(mAuth.getCurrentUser().getEmail());

            db.collection("usuarios").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            tvHeaderName.setText(documentSnapshot.getString("nombre"));
                        }
                    });
        }
    }
}