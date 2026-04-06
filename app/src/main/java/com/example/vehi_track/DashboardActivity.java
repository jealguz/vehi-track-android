package com.example.vehi_track;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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

/**
 * Actividad Principal (Panel de Control).
 * Centraliza el acceso a todos los módulos y gestiona el sistema de alertas tempranas
 * mediante consultas reactivas a Firebase Firestore.
 */
public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Componentes de navegación lateral
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView tvHeaderName, tvHeaderEmail;

    // Componentes visuales de la tarjeta dinámica de alertas (Dashboard)
    private CardView cardNotif;
    private LinearLayout layoutFondoNotif;
    private TextView tvEstadoNotif, tvBadgeCount;

    // Servicios de Backend (Autenticación y Base de Datos)
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Inicialización de servicios en la nube
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configuración de la barra de herramientas superior
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configuración del Navigation Drawer (Menú lateral)
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Toggle para sincronizar el icono de hamburguesa con el menú
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Acceso a la cabecera del menú para mostrar datos del perfil
        View headerView = navigationView.getHeaderView(0);
        tvHeaderName = headerView.findViewById(R.id.tvHeaderName);
        tvHeaderEmail = headerView.findViewById(R.id.tvHeaderEmail);

        // Vinculación de los elementos de la tarjeta de notificaciones inteligente
        cardNotif = findViewById(R.id.cardNotificaciones);
        layoutFondoNotif = findViewById(R.id.layoutFondoNotif);
        tvEstadoNotif = findViewById(R.id.tvEstadoNotifDash);
        tvBadgeCount = findViewById(R.id.tvBadgeCount);

        // Ejecución de procesos de carga de datos al iniciar
        obtenerDatosUsuario();
        configurarAccesosDirectos();
        verificarAlertas(); // Monitoreo activo de documentos y mantenimientos

        // Gestión moderna del botón "Atrás": Cierra el menú o sale de la app directamente
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    // Al estar en el Dashboard, atrás cierra la app para no acumular historial
                    finish();
                }
            }
        });
    }

    /**
     * Sistema de Monitoreo Proactivo (Dashboard):
     * Escucha cambios en tiempo real en Vehículos, Notificaciones y Mantenimientos.
     * Calcula automáticamente documentos por vencer (15 días) para el conteo total.
     */
    private void verificarAlertas() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        // 1. Configuración de tiempos (Hoy y margen de 15 días)
        long hoy = System.currentTimeMillis();
        long margen15Dias = 15L * 24 * 60 * 60 * 1000;

        // --- BLOQUE A: MONITOR DE VEHÍCULOS (SOAT y RTM) ---
        db.collection("vehiculos")
                .whereEqualTo("id_usuario", uid)
                .addSnapshotListener((vehiculosSnap, error) -> {
                    if (vehiculosSnap != null) {
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : vehiculosSnap) {
                            String placa = doc.getString("placa");
                            String idVehiculo = doc.getId();
                            verificarYCrearNotif(doc, "vencimiento_soat", "SOAT", placa, idVehiculo, hoy, margen15Dias);
                            verificarYCrearNotif(doc, "vencimiento_rtm", "Tecnomecánica", placa, idVehiculo, hoy, margen15Dias);
                        }
                    }
                });

        // --- BLOQUE B: MONITOR DE MANTENIMIENTOS ---
        db.collection("mantenimientos")
                .whereEqualTo("id_usuario", uid)
                .whereEqualTo("fecha_realizacion", null)
                .addSnapshotListener((mantoSnap, e) -> {
                    if (mantoSnap != null) {
                        for (com.google.firebase.firestore.QueryDocumentSnapshot docManto : mantoSnap) {
                            java.util.Map<String, Object> regManto = new java.util.HashMap<>();
                            regManto.put("titulo", "Mantenimiento: " + docManto.getString("servicio"));
                            regManto.put("mensaje", "Pendiente para la placa: " + docManto.getString("placa"));
                            regManto.put("fecha", docManto.getTimestamp("fecha_programada"));
                            regManto.put("idVehiculo", docManto.getId());
                            regManto.put("id_usuario", uid);
                            regManto.put("tipo", "TALLER");
                            db.collection("notificaciones").document(docManto.getId()).set(regManto);
                        }
                    }
                });

        // --- BLOQUE C: ACTUALIZACIÓN DEL BADGE (CONTADOR ROJO) ---
        db.collection("notificaciones")
                .whereEqualTo("id_usuario", uid)
                .addSnapshotListener((notifSnap, e) -> {
                    int totalAlertas = (notifSnap != null) ? notifSnap.size() : 0;
                    actualizarInterfazAlertas(totalAlertas);
                });
    }

    /**
     * Sistema de Persistencia de Alertas:
     * Si detecta un vencimiento próximo, genera o actualiza el documento en la colección global.
     */
    private void verificarYCrearNotif(com.google.firebase.firestore.QueryDocumentSnapshot doc, String campo, String tipo, String placa, String idVeh, long hoy, long margen) {
        if (doc.contains(campo) && doc.getTimestamp(campo) != null) {
            com.google.firebase.Timestamp fechaDoc = doc.getTimestamp(campo);
            if ((fechaDoc.toDate().getTime() - hoy) <= margen) {
                String idAlerta = idVeh + "_" + tipo;
                java.util.Map<String, Object> nuevaNotif = new java.util.HashMap<>();
                nuevaNotif.put("titulo", "Vencimiento de " + tipo + " [" + placa + "]");
                nuevaNotif.put("mensaje", "Pendiente de renovación para la placa: " + placa);
                nuevaNotif.put("fecha", fechaDoc);
                nuevaNotif.put("idVehiculo", placa);
                nuevaNotif.put("id_usuario", mAuth.getUid());
                nuevaNotif.put("tipo", "LEGAL");
                db.collection("notificaciones").document(idAlerta).set(nuevaNotif);
            }
        }
    }

    /**
     * Actualización Dinámica de la UI:
     * Muestra el total de alertas (Legales + Manuales + Mantenimientos).
     */
    private void actualizarInterfazAlertas(int total) {
        if (total > 0) {
            layoutFondoNotif.setBackgroundColor(Color.parseColor("#B71C1C"));
            tvEstadoNotif.setText("Tienes " + total + " alertas pendientes");
            tvEstadoNotif.setTextColor(Color.parseColor("#FFCDD2"));
            tvBadgeCount.setText(String.valueOf(total));
            tvBadgeCount.setVisibility(View.VISIBLE);
        } else {
            layoutFondoNotif.setBackgroundColor(Color.parseColor("#0A2351"));
            tvEstadoNotif.setText("Todo se encuentra al día");
            tvEstadoNotif.setTextColor(Color.parseColor("#CCCCCC"));
            tvBadgeCount.setVisibility(View.GONE);
        }
    }

    /**
     * Método de Navegación Centralizado:
     * Utiliza Flags para limpiar el historial de pantallas y mejorar la fluidez (UX).
     */
    private void navegarA(Class<?> destino) {
        Intent intent = new Intent(this, destino);
        // CLEAR_TOP: Cierra pantallas previas para que el Dashboard sea siempre la base.
        // SINGLE_TOP: Evita duplicar la pantalla si ya está visible.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    /**
     * Gestión de Eventos de Clic:
     * Vincula cada CardView del Dashboard con su respectiva Actividad del sistema.
     */
    private void configurarAccesosDirectos() {
        cardNotif.setOnClickListener(v -> navegarA(NotificacionesActivity.class));
        findViewById(R.id.cardMisVehiculos).setOnClickListener(v -> navegarA(MisVehiculosActivity.class));
        findViewById(R.id.cardRegistrar).setOnClickListener(v -> navegarA(RegistrarVehiculoActivity.class));
        findViewById(R.id.cardGastos).setOnClickListener(v -> navegarA(ListarCombustibleActivity.class));
        findViewById(R.id.cardTalleresCerca).setOnClickListener(v -> navegarA(TalleresCercanosActivity.class));
        findViewById(R.id.cardReportesPdf).setOnClickListener(v -> navegarA(ListaReportesActivity.class));
        findViewById(R.id.cardPerfil).setOnClickListener(v -> navegarA(AjustesActivity.class));
    }

    /**
     * Navegación del Menú Lateral:
     * Centraliza el flujo de redireccionamiento y cierre de sesión.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_inicio) {
            // Sin acción: Ya estamos en el inicio
        } else if (id == R.id.nav_reportes) navegarA(ListaReportesActivity.class);
        else if (id == R.id.nav_vehiculo) navegarA(MisVehiculosActivity.class);
        else if (id == R.id.nav_registrar_vehiculo) navegarA(RegistrarVehiculoActivity.class);
        else if (id == R.id.nav_mis_mantenimientos) navegarA(ListarMantenimientosActivity.class);
        else if (id == R.id.nav_registrar_mantenimiento) navegarA(RegistrarMantenimientoActivity.class);
        else if (id == R.id.nav_gasto_combustible) navegarA(ListarCombustibleActivity.class);
        else if (id == R.id.nav_registrar_combustible) navegarA(RegistrarCombustibleActivity.class);
        else if (id == R.id.nav_talleres_cercanos) navegarA(TalleresCercanosActivity.class);
        else if (id == R.id.nav_notificaciones) navegarA(NotificacionesActivity.class);
        else if (id == R.id.nav_ajustes) navegarA(AjustesActivity.class);
        else if (id == R.id.nav_reporte_pdf) navegarA(ReportesActivity.class);
        else if (id == R.id.nav_cerrar_sesion) {
            mAuth.signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Sincronización de Perfil:
     * Recupera datos del usuario desde Firestore para personalizar la interfaz.
     */
    private void obtenerDatosUsuario() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            tvHeaderEmail.setText(mAuth.getCurrentUser().getEmail());
            db.collection("usuarios").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) tvHeaderName.setText(doc.getString("nombre"));
                    });
        }
    }
}