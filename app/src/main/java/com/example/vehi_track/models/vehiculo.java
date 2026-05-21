package com.example.vehi_track.models;

import com.google.firebase.Timestamp;

public class vehiculo {

    private String id_vehiculo;
    private String id_usuario;
    private String tipo;
    private String marca;
    private String modelo;
    private int anio;
    private String placa;
    private int kilometraje;
    private Timestamp vencimiento_soat;
    private Timestamp vencimiento_rtm;
    private String estadoSoat;
    private String estadoRtm;

    public vehiculo() {}

    // --- GETTERS Y SETTERS QUE ESTÁN BIEN ---

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public Timestamp getVencimiento_soat() { return vencimiento_soat; }
    public void setVencimiento_soat(Timestamp vencimiento_soat) { this.vencimiento_soat = vencimiento_soat; }

    public Timestamp getVencimiento_rtm() { return vencimiento_rtm; }
    public void setVencimiento_rtm(Timestamp vencimiento_rtm) { this.vencimiento_rtm = vencimiento_rtm; }

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

    public String getEstadoSoat() { return estadoSoat; }
    public void setEstadoSoat(String estadoSoat) { this.estadoSoat = estadoSoat; }

    public String getEstadoRtm() { return estadoRtm; }
    public void setEstadoRtm(String estadoRtm) { this.estadoRtm = estadoRtm; }

    // --- AJUSTES DE SEGURIDAD (PARA EVITAR EL CRASH) ---

    public int getAnio() { return anio; }

    // Cambiamos a Object para que acepte el int64 de Firebase o Strings accidentales
    public void setAnio(Object anio) {
        if (anio instanceof Number) {
            this.anio = ((Number) anio).intValue();
        } else if (anio instanceof String) {
            try {
                this.anio = Integer.parseInt((String) anio);
            } catch (Exception e) {
                this.anio = 0;
            }
        }
    }

    public int getKilometraje() { return kilometraje; }

    // Cambiamos a Object por la misma razón: seguridad total al deserializar
    public void setKilometraje(Object kilometraje) {
        if (kilometraje instanceof Number) {
            this.kilometraje = ((Number) kilometraje).intValue();
        } else if (kilometraje instanceof String) {
            try {
                this.kilometraje = Integer.parseInt((String) kilometraje);
            } catch (Exception e) {
                this.kilometraje = 0;
            }
        }
    }
}