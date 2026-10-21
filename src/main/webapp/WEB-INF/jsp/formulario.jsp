<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Formulario HTML para crear o editar una especialidad. Se envía por POST al servlet. --%>
<c:set var="tituloPagina" value="${edicion ? 'Editar especialidad' : 'Nueva especialidad'}" />
<%@ include file="fragmentos/encabezado.jspf" %>

<a class="volver" href="especialidades">◀ Volver al listado</a>
<h1>${tituloPagina}</h1>

<c:if test="${not empty error}"><p class="alerta error" role="alert"><c:out value="${error}" /></p></c:if>

<form class="formulario" method="post" action="especialidades">
  <input type="hidden" name="accion" value="${edicion ? 'actualizar' : 'crear'}">
  <c:if test="${edicion}"><input type="hidden" name="id" value="${id}"></c:if>

  <label for="nombre">Nombre</label>
  <input type="text" id="nombre" name="nombre" required maxlength="80" value="<c:out value='${nombre}' />">

  <label for="descripcion">Descripción <span class="opcional">(opcional)</span></label>
  <textarea id="descripcion" name="descripcion" maxlength="255" rows="4"><c:out value="${descripcion}" /></textarea>

  <div class="botones">
    <a class="boton secundario" href="especialidades">Cancelar</a>
    <button type="submit" class="boton primario">${edicion ? 'Guardar cambios' : 'Crear especialidad'}</button>
  </div>
</form>

<%@ include file="fragmentos/pie.jspf" %>
