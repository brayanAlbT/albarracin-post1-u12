package com.empresa.reembolsos;

import com.empresa.reembolsos.aplicacion.SolicitudInvalidaException;
import com.empresa.reembolsos.aplicacion.SolicitudReembolsoService;
import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReembolsosIntegrationTest {
    @Autowired
    private SolicitudReembolsoService service;

    @Test
    void flujoCompleto_crearYAprobarSolicitud() {
        SolicitudReembolso s = new SolicitudReembolso();
        s.setEmpleadoId("EMP-100");
        s.setCategoria(CategoriaGasto.CAPACITACION);
        s.setMontoSolicitado(new BigDecimal("1000000"));
        s.setFechaGasto(LocalDate.of(2026, 9, 1));
        s.setDocumentosSoporte(true);

        SolicitudReembolso creada = service.crear(s);
        assertNotNull(creada.getId());
        assertEquals(EstadoSolicitud.PENDIENTE, creada.getEstado());
        assertEquals(0, new BigDecimal("800000.0").compareTo(creada.getMontoAprobado()));

        SolicitudReembolso aprobada = service.cambiarEstado(creada.getId(), EstadoSolicitud.APROBADA);
        assertEquals(EstadoSolicitud.APROBADA, aprobada.getEstado());
    }

    @Test
    void crearSolicitud_sinDocumentosSoporte_lanzaExcepcion() {
        SolicitudReembolso s = new SolicitudReembolso();
        s.setEmpleadoId("EMP-200");
        s.setCategoria(CategoriaGasto.REPRESENTACION);
        s.setMontoSolicitado(new BigDecimal("500000"));
        s.setFechaGasto(LocalDate.of(2026, 9, 2));
        s.setDocumentosSoporte(false);

        assertThrows(SolicitudInvalidaException.class, () -> service.crear(s));
    }
}