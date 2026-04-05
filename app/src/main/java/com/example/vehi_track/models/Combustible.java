package com.example.vehi_track.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;

/**
 * Modelo Combustible: Optimizado para Firebase Firestore.
 * Registra el consumo de gasolina/diésel vinculado a un vehículo.
 * @author Jeison Guzman
 */
public class Combustible implements Serializable {

    private String id_gasto_combustible;
    private String id_vehiculo;
    private Timestamp fecha;
    private float cantidad;
    private float costo;
    private int kilometraje;
    private String id_usuario;

    // --- Constructor Vacío (Obligatorio para Firebase) ---
    public Combustible() {}

    // --- Constructor para facilitar el registro ---
    public Combustible(Timestamp fecha, float cantidad, float costo, int kilometraje) {
        this.fecha = fecha;
        this.cantidad = cantidad;
        this.costo = costo;
        this.kilometraje = kilometraje;
    }

    // --- Getters y Setters ---

    public String getId_gasto_combustible() { return id_gasto_combustible; }
    public void setId_gasto_combustible(String id_gasto_combustible) { this.id_gasto_combustible = id_gasto_combustible; }

    public String getId_vehiculo() { return id_vehiculo; }
    public void setId_vehiculo(String id_vehiculo) { this.id_vehiculo = id_vehiculo; }

    // CORRECCIÓN: Cambiado de int a Timestamp
    public Timestamp getFecha() { return fecha; }

    // CORRECCIÓN: El parámetro ya estaba bien como Timestamp
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }

    public float getCantidad() { return cantidad; }
    public void setCantidad(float cantidad) { this.cantidad = cantidad; }

    public float getCosto() { return costo; }
    public void setCosto(float costo) { this.costo = costo; }

    public int getKilometraje() { return kilometraje; }
    public void setKilometraje(int kilometraje) { this.kilometraje = kilometraje; }

    public String getId_usuario() { return id_usuario; }
    public void setId_usuario(String id_usuario) { this.id_usuario = id_usuario; }
}