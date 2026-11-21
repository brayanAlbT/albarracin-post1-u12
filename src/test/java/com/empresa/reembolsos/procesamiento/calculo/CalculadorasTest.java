package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadorasTest {
    @Test
    void calculadoraViaje_aplicaTopePorDias() {
        SolicitudReembolso s = new SolicitudReembolso();
        s.setCategoria(CategoriaGasto.VIAJE);
        s.setDiasViaje(2);
        s.setMontoSolicitado(new BigDecimal("400000"));
        assertEquals(0, new BigDecimal("300000").compareTo(new CalculadoraViaje().calcular(s)));
    }

    @Test
    void calculadoraRepresentacion_aplicaTopeMensual() {
        SolicitudReembolso s = new SolicitudReembolso();
        s.setCategoria(CategoriaGasto.REPRESENTACION);
        s.setMontoSolicitado(new BigDecimal("2500000"));
        assertEquals(0, new BigDecimal("2000000").compareTo(new CalculadoraRepresentacion().calcular(s)));
    }

    @Test
    void calculadoraCapacitacion_aplicaOchentaPorCientoConTope() {
        SolicitudReembolso s = new SolicitudReembolso();
        s.setCategoria(CategoriaGasto.CAPACITACION);
        s.setMontoSolicitado(new BigDecimal("1000000"));
        assertEquals(0, new BigDecimal("800000.0").compareTo(new CalculadoraCapacitacion().calcular(s)));
    }
}