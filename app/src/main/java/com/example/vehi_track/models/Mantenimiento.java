package com.example.vehi_track.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;

/**
 * Modelo Mantenimiento: Corregido para manejar Timestamps de Firebase.
 * @author Jeison Guzman
 */
public class Mantenimiento implements Serializable {

    private String id_mantenimiento;
    private String id_vehiculo;
    private String id_usuario; // Agregado para integridad con tu DB
    private String placa;

    // CAMBIO CLAVE: Usamos Timestamp para evitar el error de deserialización
    private Timestamp fecha_programada;
    private Timestamp fecha_realizacion;

    private String descripcion;
    private double costo;
    private int kilometraje_mantenimiento;

    // --- Constructor Vacío REQUERIDO por Firebase ---
    public Mantenimiento() {
    }

    // --- Constructor actualizado ---
    public Mantenimiento(String placa, Timestamp fecha_programada, String descripcion, double costo) {
        this.placa = placa;
        this.fecha_programada = fecha_programada;
        this.descripcion = descripcion;
        this.costo = costo;
    }

    // --- Getters y Setters ---

    public String getId_mantenimiento() { return id_mantenimiento; }
    public void setId_mantenimiento(String id_mantenimiento) { this.id_mantenimiento = id_mantenimiento; }

    public String getId_vehiculo() { return id_vehiculo; }
    public void setId_vehiculo(String id_vehiculo) { this.id_vehiculo = id_vehiculo; }

    public String getId_usuario() { return id_usuario; }
    public void setId_usuario(String id_usuario) { this.id_usuario = id_usuario; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    // Getters y Setters corregidos para Timestamp
    public Timestamp getFecha_programada() { return fecha_programada; }
    public void setFecha_programada(Timestamp fecha_programada) { this.fecha_programada = fecha_programada; }

    public Timestamp getFecha_realizacion() { return fecha_realizacion; }
    public void setFecha_realizacion(Timestamp fecha_realizacion) { this.fecha_realizacion = fecha_realizacion; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getCosto() { return costo; }
    public void setCosto(double costo) { this.costo = costo; }

    public int getKilometraje_mantenimiento() { return kilometraje_mantenimiento; }
    public void setKilometraje_mantenimiento(int kilometraje_mantenimiento) {
        this.kilometraje_mantenimiento = kilometraje_mantenimiento;
    }
}