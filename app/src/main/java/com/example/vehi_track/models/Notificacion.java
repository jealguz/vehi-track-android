package com.example.vehi_track.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

public class Notificacion {
    private String id; // ID del documento en Firestore
    private String titulo;
    private String mensaje;

    // CAMBIO: De String a Timestamp para evitar el Crash
    private Timestamp fecha;

    private String idVehiculo;
    private String id_usuario;

    // 1. Constructor vacío (Obligatorio para Firebase)
    public Notificacion() {}

    // 2. Constructor completo (Actualizado con Timestamp)
    public Notificacion(String id, String titulo, String mensaje, Timestamp fecha, String idVehiculo) {
        this.id = id;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.fecha = fecha;
        this.idVehiculo = idVehiculo;
    }

    // --- Getters y Setters ---

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    // Getter y Setter actualizados
    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }

    public String getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(String idVehiculo) { this.idVehiculo = idVehiculo; }

    public String getId_usuario() { return id_usuario; }
    public void setId_usuario(String id_usuario) { this.id_usuario = id_usuario; }
}