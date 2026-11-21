package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import java.math.BigDecimal;

/** Estrategia de cálculo del monto aprobado para una categoría de gasto específica. */
public interface CalculadoraReembolso {
    CategoriaGasto getCategoria();
    BigDecimal calcular(SolicitudReembolso solicitud);
}