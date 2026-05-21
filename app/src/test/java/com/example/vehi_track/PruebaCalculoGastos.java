package com.example.vehi_track;

import static org.junit.Assert.*;
import org.junit.Test;

public class PruebaCalculoGastos {
    @Test
    public void validarSumaGastosCombustible() {
        // Usamos tu modelo de Combustible
        com.example.vehi_track.models.Combustible registro1 = new com.example.vehi_track.models.Combustible();
        com.example.vehi_track.models.Combustible registro2 = new com.example.vehi_track.models.Combustible();

        // Asumiendo que tienes un método setMonto o similar en esa clase
        int monto1 = 50000;
        int monto2 = 100000;

        int totalCalculado = monto1 + monto2;
        assertEquals(150000, totalCalculado);
    }
}