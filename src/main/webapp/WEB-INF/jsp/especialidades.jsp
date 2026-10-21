<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Listado de especialidades con buscador (GET) y botones de editar (GET) y eliminar (POST). --%>
<c:set var="tituloPagina" value="Especialidades" />
<%@ include file="fragmentos/encabezado.jspf" %>

<div class="encabezado">
  <h1>Especialidades médicas</h1>
  <a class="boton primario" href="especialidades?accion=nuevo">+ Nueva especialidad</a>
</div>

<c:if test="${not empty mensaje}"><p class="alerta exito" role="status"><c:out value="${mensaje}" /></p></c:if>
<c:if test="${not empty error}"><p class="alerta error" role="alert"><c:out value="${error}" /></p></c:if>

<%-- Formulario de búsqueda: método GET, el filtro queda en la URL y se puede compartir --%>
<form class="buscador" method="get" action="especialidades">
  <label for="buscar" class="oculto">Buscar especialidad</label>
  <input type="search" id="buscar" name="buscar" placeholder="Buscar por nombre" value="<c:out value='${buscar}' />">
  <button type="submit" class="boton secundario">Buscar</button>
  <c:if test="${not empty buscar}"><a href="especialidades">Ver todas</a></c:if>
</form>

<c:choose>
  <c:when test="${empty especialidades}">
    <p class="vacio">No hay especialidades que coincidan con la búsqueda.</p>
  </c:when>
  <c:otherwise>
    <table class="tabla">
      <caption>${especialidades.size()} especialidad(es)</caption>
      <thead><tr><th scope="col">Id</th><th scope="col">Nombre</th><th scope="col">Descripción</th><th scope="col">Acciones</th></tr></thead>
      <tbody>
      <c:forEach var="especialidad" items="${especialidades}">
        <tr>
          <td>${especialidad.id}</td>
          <th scope="row"><c:out value="${especialidad.nombre}" /></th>
          <td><c:out value="${especialidad.descripcion}" default="—" /></td>
          <td class="acciones">
            <a class="boton secundario" href="especialidades?accion=editar&amp;id=${especialidad.id}">Editar</a>
            <%-- Eliminar cambia datos: se envía por POST y pide confirmación --%>
            <form method="post" action="especialidades" onsubmit="return confirm('¿Eliminar la especialidad ${especialidad.id}?');">
              <input type="hidden" name="accion" value="eliminar">
              <input type="hidden" name="id" value="${especialidad.id}">
              <button type="submit" class="boton peligro">Eliminar</button>
            </form>
          </td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
  </c:otherwise>
</c:choose>

<%@ include file="fragmentos/pie.jspf" %>
