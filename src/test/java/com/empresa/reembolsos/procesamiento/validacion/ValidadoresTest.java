package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ValidadoresTest {
    private SolicitudReembolso crearSolicitud(boolean soporte, BigDecimal monto, LocalDate fecha) {
        SolicitudReembolso s = new SolicitudReembolso();
        s.setEmpleadoId("EMP-1");
        s.setCategoria(CategoriaGasto.REPRESENTACION);
        s.setDocumentosSoporte(soporte);
        s.setMontoSolicitado(monto);
        s.setFechaGasto(fecha);
        s.setEstado(EstadoSolicitud.PENDIENTE);
        return s;
    }

    @Test
    void validadorDocumentosSoporte_rechazaSinDocumentos() {
        var res = new ValidadorDocumentosSoporte().validar(crearSolicitud(false, new BigDecimal("100000"), LocalDate.now()), List.of());
        assertFalse(res.valida());
    }

    @Test
    void validadorMontoMaximo_rechazaCuandoExcedeTope() {
        var res = new ValidadorMontoMaximo().validar(crearSolicitud(true, new BigDecimal("2500000"), LocalDate.now()), List.of());
        assertFalse(res.valida());
    }

    @Test
    void validadorDuplicados_rechazaMismaCategoriaEnMismoMes() {
        var existente = crearSolicitud(true, new BigDecimal("500000"), LocalDate.of(2026, 8, 5));
        var nueva = crearSolicitud(true, new BigDecimal("300000"), LocalDate.of(2026, 8, 20));
        var res = new ValidadorDuplicados().validar(nueva, List.of(existente));
        assertFalse(res.valida());
    }
}