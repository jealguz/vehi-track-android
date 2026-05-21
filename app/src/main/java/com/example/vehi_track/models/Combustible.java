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
    private double cantidad;
    private double costo;
    private int kilometraje;
    private String id_usuario;
    private String placa;

    // --- 1. CONSTRUCTOR VACÍO (Obligatorio para Firebase) ---
    public Combustible() {}

    // --- 2. CONSTRUCTOR PARA REGISTRO (Corregido a double) ---
    public Combustible(Timestamp fecha, double cantidad, double costo, int kilometraje) {
        this.fecha = fecha;
        this.cantidad = cantidad;
        this.costo = costo;
        this.kilometraje = kilometraje;
    }

    // --- 3. GETTERS Y SETTERS (Corregidos) ---

    public String getId_gasto_combustible() { return id_gasto_combustible; }
    public void setId_gasto_combustible(String id_gasto_combustible) { this.id_gasto_combustible = id_gasto_combustible; }

    public String getId_vehiculo() { return id_vehiculo; }
    public void setId_vehiculo(String id_vehiculo) { this.id_vehiculo = id_vehiculo; }

    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }

    // CORRECCIÓN: Se cambió el parámetro de float a double para coincidir con la variable
    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }

    // CORRECCIÓN: Se cambió el parámetro de float a double para coincidir con la variable
    public double getCosto() { return costo; }
    public void setCosto(double costo) { this.costo = costo; }

    public int getKilometraje() { return kilometraje; }
    public void setKilometraje(int kilometraje) { this.kilometraje = kilometraje; }

    public String getId_usuario() { return id_usuario; }
    public void setId_usuario(String id_usuario) { this.id_usuario = id_usuario; }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }
}