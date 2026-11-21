package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class ValidadorMontoMaximo extends ValidadorSolicitud {
    private static final Map<CategoriaGasto, BigDecimal> TOPES = Map.of(
            CategoriaGasto.VIAJE, new BigDecimal("150000"),
            CategoriaGasto.REPRESENTACION, new BigDecimal("2000000"),
            CategoriaGasto.CAPACITACION, new BigDecimal("3000000")
    );

    @Override
    protected ResultadoValidacion validarPropio(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientes) {
        BigDecimal tope = TOPES.get(solicitud.getCategoria());
        if (solicitud.getCategoria() == CategoriaGasto.VIAJE && solicitud.getDiasViaje() > 0) {
            tope = tope.multiply(BigDecimal.valueOf(solicitud.getDiasViaje()));
        }
        return solicitud.getMontoSolicitado().compareTo(tope) > 0
                ? ResultadoValidacion.rechazar("Monto excede el tope de la categoria " + solicitud.getCategoria())
                : ResultadoValidacion.ok();
    }
}