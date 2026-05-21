package com.example.vehi_track;

import static org.junit.Assert.*;
import org.junit.Test;
import com.example.vehi_track.models.vehiculo;

public class PruebaValidacionCampos {
    @Test
    public void validarPlacaNoVacia() {
        com.example.vehi_track.models.vehiculo miVehiculo = new com.example.vehi_track.models.vehiculo();
        miVehiculo.setPlaca(""); // Usamos TU función setPlaca

        // Verificamos si TU función getPlaca devuelve el vacío que asignamos
        String resultado = miVehiculo.getPlaca();
        assertTrue("Error: El sistema permitió una placa vacía", resultado.isEmpty());
    }
}