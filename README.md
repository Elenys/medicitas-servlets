# MediCitas · Módulo web de especialidades (servlets + JSP)

Evidencia **GA7-220501096-AA2-EV02 — Módulos de software codificados y probados** · SENA, Análisis y Desarrollo de Software, ficha 3186702.
Autores: Carlas Elenys Montalvo Mendoza y Freyder López.

Versión web del módulo de especialidades médicas de **MediCitas** (evidencia AA2-EV01): formularios HTML
procesados por un **servlet** con los métodos **GET** y **POST**, vistas **JSP** con JSTL y acceso a la base
de datos PostgreSQL del proyecto por **JDBC** (se reutilizan el modelo y el DAO de la evidencia anterior).

## Cómo funciona

| Método | URL | Qué hace |
|---|---|---|
| GET | `/especialidades` | Listado de especialidades |
| GET | `/especialidades?buscar=texto` | Búsqueda por nombre (formulario con `method="get"`) |
| GET | `/especialidades?accion=nuevo` | Formulario para crear |
| GET | `/especialidades?accion=editar&id=N` | Formulario con los datos de la especialidad N |
| POST | `/especialidades` · `accion=crear` | Inserta y redirige al listado con un mensaje |
| POST | `/especialidades` · `accion=actualizar` | Actualiza y redirige |
| POST | `/especialidades` · `accion=eliminar` | Elimina (con confirmación) y redirige |

Las consultas usan GET (no cambian datos y se pueden repetir o compartir); las operaciones que modifican
datos usan POST. Después de cada POST el servlet redirige con GET (patrón *Post/Redirect/Get*), de modo que
recargar la página no repite la operación. Si hay un error de validación (nombre vacío o repetido) se
vuelve a mostrar el formulario con lo escrito y el mensaje.

## Elementos de JSP utilizados

- **Directivas:** `<%@ page %>` (codificación), `<%@ taglib %>` (JSTL) e `<%@ include %>` (encabezado y pie comunes en `fragmentos/`).
- **Lenguaje de expresiones (EL):** `${especialidad.nombre}`, `${pageContext.request.contextPath}`, `${edicion ? … : …}`.
- **JSTL core:** `c:forEach`, `c:if`, `c:choose/c:when/c:otherwise`, `c:set` y `c:out` (escapa el texto para evitar inyección de HTML).
- **Expresión y scriptlet JSP:** `<%= java.time.Year.now() %>` en el pie; `response.sendRedirect` en `index.jsp`.
- **Comentarios JSP** `<%-- … --%>`, que no llegan al navegador.

## Estructura

```
src/main/java/co/edu/sena/medicitas/
├── modelo/Especialidad.java              entidad con validaciones
├── datos/ConexionBD.java                 conexión JDBC (lee WEB-INF/db.properties)
├── datos/EspecialidadDao(.java|Jdbc.java) CRUD con PreparedStatement
├── datos/DatosException.java             errores con mensajes comprensibles
└── web/EspecialidadServlet.java          controlador: doGet y doPost
src/main/webapp/
├── WEB-INF/web.xml                        configuración (UTF-8, página de inicio, sesión)
├── WEB-INF/jsp/especialidades.jsp         listado + buscador + eliminar
├── WEB-INF/jsp/formulario.jsp             crear / editar
├── WEB-INF/jsp/fragmentos/*.jspf          encabezado y pie comunes
├── css/estilos.css                        colores del sistema de diseño de MediCitas
└── index.jsp                              redirige al listado
pruebas/prueba_http.sh                     prueba con peticiones GET y POST reales
pruebas/capturar_pantallas.js              recorre el módulo en Chrome y guarda capturas
lib/                                       driver PostgreSQL y JSTL (Maven Central, checksum verificado)
construir.sh · pom.xml                     construcción sin Maven o con Maven
```

## Cómo ejecutarlo

Requisitos: Java 17+, Apache Tomcat 10.1 y la base de datos de MediCitas (`docker compose up` en el proyecto MediCitas).

```bash
cp config/db.properties.ejemplo config/db.properties
TOMCAT_HOME=~/apache-tomcat-10.1.61 ./construir.sh     # compila, arma el WAR y lo despliega
~/apache-tomcat-10.1.61/bin/startup.sh                  # si Tomcat no está en marcha
open http://localhost:8080/medicitas-especialidades/
```

## Pruebas (21/10/2026)

`pruebas/prueba_http.sh`: **11 verificaciones aprobadas** — listado, búsqueda y formularios por GET;
crear, actualizar y eliminar por POST con redirección; error de nombre repetido; bloqueo al eliminar una
especialidad con médicos; y la base queda igual que al inicio. Las capturas están en `pruebas/capturas/`.

## Estándares de codificación

Paquetes en minúscula (`co.edu.sena.medicitas.web`), clases en PascalCase (`EspecialidadServlet`),
métodos y variables en camelCase (`mostrarFormulario`, `especialidadDao`), constantes en MAYÚSCULAS
(`VISTA_LISTADO`), Javadoc en clases y métodos, consultas parametrizadas y credenciales fuera del repositorio.
