# Post-contenido — Unidad 12: Proyecto Integrador — Sistema de Solicitudes de Reembolso de Gastos Corporativos

![Validación Arquitectónica](https://github.com/brayanAlbT/albarracin-post1-u12/actions/workflows/arquitectura.yml/badge.svg)

**Curso:** Patrones de Diseño de Software (Código: 1156701-A) — Unidad 12  
**Semestre:** Sexto Semestre  
**Estudiante:** Brayan Smith Albarracín Tarazona  
**Institución:** Universidad Francisco de Paula Santander (UFPS)  

---

## 1. Diseño Propuesto (Parte 1 — Análisis previo al código)

| Requisito | Punto de variabilidad | Patrón elegido | Justificación (Test de explicabilidad) |
| :--- | :--- | :--- | :--- |
| **RF1 — Validación en varias etapas** | Número, orden y reglas independientes de verificación que deben ejecutarse secuencialmente con corte anticipado al primer fallo, permitiendo agregar futuras validaciones sin modificar las existentes. | **Chain of Responsibility** | Permite encadenar validadores independientes (`ValidadorSolicitud`) que abortan inmediatamente ante la primera regla incumplida y admiten añadir nuevas etapas sin alterar las actuales. |
| **RF2 — Cálculo distinto por categoría** | La fórmula de cálculo del monto aprobado varía según la `CategoriaGasto` (`VIAJE`, `REPRESENTACION`, `CAPACITACION`) y crecerá con nuevas categorías en próximos semestres. | **Strategy** (inyectado en mapa por Spring) | Encapsula la fórmula de cada categoría en una implementación intercambiable de `CalculadoraReembolso`, permitiendo agregar nuevas categorías registrando otro `@Component` sin tocar un `switch`. |
| **RF3 — Notificación simultánea a interesados** | Qué partes interesadas reaccionan y cómo lo hacen cada vez que una solicitud cambia de estado (empleado, Contabilidad, Auditoría, futuros paneles). | **Observer** | Desacopla al servicio de aplicación de las reacciones heterogéneas de cada interesado mediante `ObservadorCambioEstado`, permitiendo suscribir nuevos oyentes sin modificar `cambiarEstado()`. |

---

## 2. Arquitectura Resultante

```text
albarracin-post1-u12/
├── pom.xml
├── README.md
├── .github/workflows/arquitectura.yml
├── docs/adr/ADR-001.md
└── src/
    ├── main/
    │   ├── java/com/empresa/reembolsos/
    │   │   ├── ReembolsosApplication.java
    │   │   ├── dominio/                        <-- Java puro (sin Spring ni JPA; mapeo vía orm.xml)
    │   │   │   ├── SolicitudReembolso.java
    │   │   │   ├── CategoriaGasto.java
    │   │   │   └── EstadoSolicitud.java
    │   │   ├── procesamiento/
    │   │   │   ├── validacion/                 <-- RF1: Chain of Responsibility
    │   │   │   ├── calculo/                    <-- RF2: Strategy + SelectorCalculadora
    │   │   │   └── notificacion/               <-- RF3: Observer
    │   │   ├── aplicacion/
    │   │   │   ├── SolicitudReembolsoService.java
    │   │   │   └── SolicitudInvalidaException.java
    │   │   ├── infraestructura/
    │   │   │   └── RepositorioSolicitudesJpa.java
    │   │   └── adaptadores/rest/
    │   │       └── SolicitudReembolsoController.java
    │   └── resources/
    │       ├── application.properties
    │       └── META-INF/orm.xml
    └── test/java/com/empresa/reembolsos/
        ├── ReglasArquitectura.java             <-- 4 reglas ArchUnit
        ├── ReembolsosIntegrationTest.java      <-- Prueba @SpringBootTest
        └── procesamiento/
            ├── validacion/ValidadoresTest.java
            └── calculo/CalculadorasTest.java
```

---

## 3. Decisiones de Diseño (1 a 4)

### Decisión 1 — Orden de los eslabones en `CadenaValidadores`
Se encadenaron en el orden **`ValidadorDocumentosSoporte` → `ValidadorMontoMaximo` → `ValidadorDuplicados`**.
- **Justificación por eficiencia:** `ValidadorDocumentosSoporte` y `ValidadorMontoMaximo` son comprobaciones en memoria `O(1)` sobre los atributos de la solicitud entrante, mientras que `ValidadorDuplicados` recorre la colección `pendientesDelEmpleado` (`O(n)`). Al ubicar el validador más costoso al final de la cadena, cualquier solicitud sin documentos soporte o fuera de tope es rechazada en el primer o segundo eslabón sin gastar ciclos en recorrer el historial pendiente.
- **Qué se gana y qué se pierde:** Se minimiza el trabajo promedio ante solicitudes inválidas; la única restricción aceptada es que una solicitud que falle por duplicado y también carezca de documentos reportará primero el error de documentos.

### Decisión 2 — Selección automática de `CalculadoraReembolso` con `Map` inyectado por Spring vs. `switch` manual
En `SelectorCalculadora`, Spring inyecta `List<CalculadoraReembolso>` con todos los beans `@Component` que implementan la interfaz y los indexa en un `Map<CategoriaGasto, CalculadoraReembolso>`.
- **Justificación frente a RF2:** Si Finanzas agrega el próximo semestre la categoría `BIENESTAR`, basta con añadir el valor al enum y crear una clase `@Component CalculadoraBienestar implements CalculadoraReembolso`. No se modifica ni una sola línea de `SelectorCalculadora` ni de las calculadoras existentes (cumplimiento estricto de OCP), a diferencia de un `switch` manual que obligaría a abrir y editar el selector cada vez.

### Decisión 3 — Capas simples para persistencia vs. Hexagonal completa (resumen de `ADR-001`)
Documentada formalmente en [`docs/adr/ADR-001.md`](docs/adr/ADR-001.md) (2026-11-20):
- **Persistencia:** Se mantiene arquitectura en capas simples (`SolicitudReembolsoService` depende directamente de `RepositorioSolicitudesJpa`). No existe ningún plan de cambiar de motor de base de datos, por lo que crear un puerto espejo y un adaptador JPA sería una abstracción especulativa (YAGNI).
- **Notificación:** Aunque Contabilidad anunció una API REST externa para el próximo trimestre, `ObservadorCambioEstado` ya provee el *seam* necesario; `NotificadorContabilidad` podrá encapsular el cliente HTTP sin tocar `SolicitudReembolsoService` ni los otros observadores.

### Decisión 4 — Alcance de la Regla 1 de ArchUnit (`procesamientoSinPersistenciaNiWeb`)
La Regla 1 prohíbe que `..procesamiento..` dependa de `..infraestructura..`, `..adaptadores..`, `org.springframework.data..` y `jakarta.persistence..`, pero **permite** `@Component` de `org.springframework.stereotype`.
- **Justificación:** Excluir todo `org.springframework..` obligaría a crear una clase `@Configuration` separada con decenas de métodos `@Bean` para registrar manualmente cada validador, calculadora y observador, añadiendo código ceremonial sin beneficio arquitectónico en un proyecto Spring Boot. Distinguir entre anotaciones de estereotipo (metadatos pasivos de ciclo de vida) y dependencias de tecnología concreta (JPA, Web, Repositorios) protege la pureza funcional de `procesamiento` sin perder la ergonomía del *component scan*.

---

## 4. Qué se Descartó y Por Qué (Análisis de Impacto)

1. **`ApplicationEventPublisher` y `@EventListener` en lugar de `List<ObservadorCambioEstado>` (Referencia):**  
   Se consideró publicar eventos de dominio mediante el bus interno de Spring. Se descartó porque los tres observadores son síncronos, locales y conocidos en tiempo de compilación; la indirección del bus dificultaría rastrear en el IDE quién reacciona a cada cambio sin aportar desacoplamiento adicional al que ya ofrece `ObservadorCambioEstado`. *Señal para reconsiderar:* si en el futuro las notificaciones debieran ejecutarse asíncronamente en otro hilo o publicarse hacia un *broker* externo (Kafka/RabbitMQ).
2. **`Abstract Factory` / `Visitor` para exportar reportes en múltiples formatos (Referencia):**  
   Se consideró anticipar exportadores PDF/Excel además de CSV para Contabilidad. Se descartó por el principio **Rule of Three** (Guía, sección 4.2): hoy solo existe el requerimiento de exportación simple en CSV y no hay demanda real de un segundo formato. *Señal para reconsiderar:* cuando Finanzas o Contabilidad soliciten formalmente al menos dos o tres formatos de exportación distintos.
3. **Jerarquía de excepciones específicas por cada validador en lugar de `SolicitudInvalidaException(String)` (Decisión propia):**  
   Durante el diseño de RF1 se evaluó crear `DocumentosFaltantesException`, `MontoMaximoExcedidoException` y `SolicitudDuplicadaException` heredando de una base abstracta, junto con un `@ExceptionHandler` dedicado para cada una en el controlador. Se descartó aplicando **KISS** y **YAGNI**: hoy todas las fallas de validación producen exactamente la misma respuesta HTTP (`400 Bad Request` con un JSON `{"error": "<motivo>"}`) y `ResultadoValidacion` ya transporta el motivo descriptivo. *Señal para reconsiderar:* si el frontend o un cliente externo necesitara códigos HTTP distintos por tipo de rechazo (por ejemplo, `409 Conflict` solo para duplicados y `422 Unprocessable Entity` para topes de monto) o políticas de reintento diferenciadas.

---

## 5. Cómo Ejecutar

```bash
# Ejecutar reglas de arquitectura ArchUnit
mvn test -Dtest=ReglasArquitectura

# Ejecutar toda la suite (pruebas unitarias, integración @SpringBootTest y ArchUnit)
mvn verify

# Iniciar la aplicación
mvn spring-boot:run
```

---

## 6. Herramientas Utilizadas

- Java 17, Spring Boot 3.2.5 (Spring Web, Spring Data JPA, H2 Database)
- JUnit 5, ArchUnit 1.2.1, GitHub Actions
- Apache Maven, Git, GitHub