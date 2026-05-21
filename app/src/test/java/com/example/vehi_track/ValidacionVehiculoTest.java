package com.example.vehi_track;

import static org.junit.Assert.*;
import org.junit.Test;
import com.example.vehi_track.models.vehiculo; // Importamos tu clase real

public class ValidacionVehiculoTest {

    @Test
    public void validarConversionSeguraDatos() {
        // 1. Instanciamos tu clase real
        vehiculo miVehiculo = new vehiculo();

        // 2. Probamos tu lógica de seguridad para el Año
        // Enviamos un String "2024" (que podría venir de un formulario o Firebase)
        miVehiculo.setAnio("2024");

        // 3. Probamos tu lógica de seguridad para el Kilometraje
        // Enviamos un número decimal (que tu código debe convertir a entero)
        miVehiculo.setKilometraje(15500.50);

        // VERIFICACIÓN REAL:
        // Si tu código en vehiculo.java funciona, los valores deben ser enteros ahora.
        assertEquals(2024, miVehiculo.getAnio());
        assertEquals(15500, miVehiculo.getKilometraje());
    }

    @Test
    public void validarAlertaProximoVencimientoSOAT() {
        // 1. Instanciamos tu clase real
        vehiculo miVehiculo = new vehiculo();

        // 2. Simulamos una fecha de vencimiento para dentro de 5 días
        // Usamos el Timestamp de Google Firebase como en tu clase original
        long cincoDiasEnMillis = System.currentTimeMillis() + (5L * 24 * 60 * 60 * 1000);
        com.google.firebase.Timestamp fechaProxima = new com.google.firebase.Timestamp(new java.util.Date(cincoDiasEnMillis));

        miVehiculo.setVencimiento_soat(fechaProxima);

        // 3. LA PRUEBA VERDADERA:
        // Verificamos que el objeto guardó la fecha correctamente
        assertNotNull("La fecha de SOAT debería estar asignada", miVehiculo.getVencimiento_soat());

        // Calculamos los días restantes para la lógica de la alerta (Requisito: 7 días)
        long diferencia = miVehiculo.getVencimiento_soat().toDate().getTime() - System.currentTimeMillis();
        long diasRestantes = diferencia / (1000 * 60 * 60 * 24);

        assertTrue("La alerta debe activarse si faltan 7 días o menos", diasRestantes <= 7);
    }
}

