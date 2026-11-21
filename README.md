# Sistema de Solicitudes de Reembolso de Gastos Corporativos

## Diseño Propuesto (Parte 1 — Análisis previo a la implementación)

| Requisito | Punto de variabilidad | Patrón elegido | Justificación (test de explicabilidad) |
| :--- | :--- | :--- | :--- |
| **RF1 — Validación en varias etapas** | Número, orden y reglas independientes de verificación que deben ejecutarse secuencialmente con corte anticipado al primer fallo, permitiendo agregar futuras validaciones sin modificar las existentes. | **Chain of Responsibility** | Permite encadenar validadores independientes (`ValidadorSolicitud`) que abortan inmediatamente ante la primera regla incumplida y admiten añadir nuevas etapas sin alterar las actuales. |
| **RF2 — Cálculo distinto por categoría** | La fórmula de cálculo del monto aprobado varía según la `CategoriaGasto` (`VIAJE`, `REPRESENTACION`, `CAPACITACION`) y crecerá con nuevas categorías en próximos semestres. | **Strategy** (con registro automático vía Spring) | Encapsula la fórmula de cada categoría en una implementación intercambiable de `CalculadoraReembolso`, permitiendo agregar nuevas categorías registrando otro `@Component` sin tocar un `switch`. |
| **RF3 — Notificación simultánea a interesados** | Qué partes interesadas reaccionan y cómo lo hacen cada vez que una solicitud cambia de estado (empleado, Contabilidad, Auditoría, futuros paneles). | **Observer** | Desacopla al servicio de aplicación de las reacciones heterogéneas de cada interesado mediante `ObservadorCambioEstado`, permitiendo suscribir nuevos oyentes sin modificar `cambiarEstado()`. |