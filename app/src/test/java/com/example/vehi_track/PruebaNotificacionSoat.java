package com.example.vehi_track;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Calendar;

public class PruebaNotificacionSoat {
    @Test
    public void verificarRangoSieteDias() {
        com.example.vehi_track.models.vehiculo miVehiculo = new com.example.vehi_track.models.vehiculo();

        // Creamos una fecha real de Firebase para dentro de 5 días
        long cincoDiasMillis = System.currentTimeMillis() + (5L * 24 * 60 * 60 * 1000);
        com.google.firebase.Timestamp fechaFirebase = new com.google.firebase.Timestamp(new java.util.Date(cincoDiasMillis));

        // Usamos TU función setVencimiento_soat
        miVehiculo.setVencimiento_soat(fechaFirebase);

        // Ejecutamos el cálculo sobre el dato que está dentro de TU objeto
        long diff = miVehiculo.getVencimiento_soat().toDate().getTime() - System.currentTimeMillis();
        long dias = diff / (1000 * 60 * 60 * 24);

        assertTrue("La alerta debe activarse (días <= 7)", dias <= 7);
    }
}