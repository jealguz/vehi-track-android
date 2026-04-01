package com.example.vehi_track.models;

/**
 * Clase Modelo Vehiculo: Adaptada para Android y Firebase Firestore.
 * @author Jeison Guzman
 */
public class vehiculo {

    // En Firebase usamos String para los IDs para mayor flexibilidad
    private String id_vehiculo;
    private String id_usuario;        // El UID del dueño que viene de Firebase Auth
    private String tipo;
    private String marca;
    private String modelo;
    private int anio;
    private String placa;
    private int kilometraje_actual;

    // REGLA DE ORO: En Firebase es mejor usar String (ISO 8601) o Long para fechas
    // Evitamos java.sql.Date porque da errores de compatibilidad en Android
    private String vencimiento_soat;
    private String vencimiento_rtm;

    // Atributos para lógica de alertas
    private String estadoSoat;
    private String estadoRtm;

    /**
     * Constructor vacío: Obligatorio para Firebase
     */
    public vehiculo() {}

    // --- GETTERS Y SETTERS ---

    public String getId_vehiculo() { return id_vehiculo; }
    public void setId_vehiculo(String id_vehiculo) { this.id_vehiculo = id_vehiculo; }

    public String getId_usuario() { return id_usuario; }
    public void setId_usuario(String id_usuario) { this.id_usuario = id_usuario; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public int getKilometraje_actual() { return kilometraje_actual; }
    public void setKilometraje_actual(int kilometraje_actual) { this.kilometraje_actual = kilometraje_actual; }

    public String getVencimiento_soat() { return vencimiento_soat; }
    public void setVencimiento_soat(String vencimiento_soat) { this.vencimiento_soat = vencimiento_soat; }

    public String getVencimiento_rtm() { return vencimiento_rtm; }
    public void setVencimiento_rtm(String vencimiento_rtm) { this.vencimiento_rtm = vencimiento_rtm; }

    public String getEstadoSoat() { return estadoSoat; }
    public void setEstadoSoat(String estadoSoat) { this.estadoSoat = estadoSoat; }

    public String getEstadoRtm() { return estadoRtm; }
    public void setEstadoRtm(String estadoRtm) { this.estadoRtm = estadoRtm; }
}