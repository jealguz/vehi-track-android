package com.example.vehi_track.models;

import java.io.Serializable;

/**
 * Modelo Favorito: Representa un taller guardado en el mapa.
 * @author Jeison Guzman
 */
public class Favorito implements Serializable {

    private String id_favorito; // ID de Firestore
    private String id_usuario;  // ID del dueño del favorito
    private String nombre_taller;
    private double latitud;
    private double longitud;
    private String direccion;

    // --- Constructor Vacío ---
    public Favorito() {
    }

    // --- Getters y Setters ---
    public String getId_favorito() { return id_favorito; }
    public void setId_favorito(String id_favorito) { this.id_favorito = id_favorito; }

    public String getId_usuario() { return id_usuario; }
    public void setId_usuario(String id_usuario) { this.id_usuario = id_usuario; }

    public String getNombre_taller() { return nombre_taller; }
    public void setNombre_taller(String nombre_taller) { this.nombre_taller = nombre_taller; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}