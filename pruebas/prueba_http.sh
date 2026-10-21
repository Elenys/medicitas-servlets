#!/bin/sh
# Prueba el módulo web con peticiones HTTP reales (GET y POST), como lo haría un navegador.
# Requiere la aplicación desplegada en Tomcat y la base de datos de MediCitas en marcha.
BASE="${BASE:-http://localhost:8080/medicitas-especialidades/especialidades}"
COOKIES=$(mktemp); APROBADAS=0
verificar() { if echo "$2" | grep -q "$3"; then APROBADAS=$((APROBADAS+1)); echo "✓ $1"; else echo "✗ FALLÓ: $1"; exit 1; fi; }
filas() { echo "$1" | grep -c '<th scope="row">'; }

L=$(curl -s -b "$COOKIES" -c "$COOKIES" "$BASE"); INICIAL=$(filas "$L")
verificar "GET listado responde con la tabla" "$L" "Especialidades médicas"
echo "  especialidades al iniciar: $INICIAL"
verificar "GET búsqueda filtra por nombre" "$(curl -s "$BASE?buscar=pedia")" "Pediatría"
verificar "GET formulario de nueva especialidad" "$(curl -s "$BASE?accion=nuevo")" "Crear especialidad"

R=$(curl -s -o /dev/null -w "%{http_code} %{redirect_url}" -b "$COOKIES" -c "$COOKIES" --data-urlencode "accion=crear" \
    --data-urlencode "nombre=Neurología (prueba web)" --data-urlencode "descripcion=Sistema nervioso" "$BASE")
verificar "POST crear redirige (303/302) al listado" "$R" "^30[23] "
L=$(curl -s -b "$COOKIES" -c "$COOKIES" "$BASE")
verificar "el listado muestra el mensaje de creación" "$L" "creada con el id"
ID=$(echo "$L" | grep -o "creada con el id [0-9]*" | grep -o "[0-9]*$"); echo "  id creado: $ID"

verificar "POST crear con nombre repetido muestra el error" "$(curl -s -b "$COOKIES" -c "$COOKIES" --data-urlencode "accion=crear" \
    --data-urlencode "nombre=Neurología (prueba web)" "$BASE")" "Ya existe una especialidad"
verificar "GET editar trae los datos guardados" "$(curl -s "$BASE?accion=editar&id=$ID")" "Sistema nervioso"
curl -s -o /dev/null -b "$COOKIES" -c "$COOKIES" --data-urlencode "accion=actualizar" --data-urlencode "id=$ID" \
    --data-urlencode "nombre=Neurología (prueba web)" --data-urlencode "descripcion=Cerebro y nervios" "$BASE"
L=$(curl -s -b "$COOKIES" -c "$COOKIES" "$BASE")
verificar "POST actualizar guarda la nueva descripción" "$L" "Cerebro y nervios"

MG=$(echo "$L" | grep -B2 "Medicina General" | grep -o "<td>[0-9]*</td>" | head -1 | grep -o "[0-9]*")
curl -s -o /dev/null -b "$COOKIES" -c "$COOKIES" --data "accion=eliminar&id=$MG" "$BASE"
verificar "POST eliminar una especialidad con médicos se rechaza" "$(curl -s -b "$COOKIES" -c "$COOKIES" "$BASE")" "hay médicos asignados"
curl -s -o /dev/null -b "$COOKIES" -c "$COOKIES" --data "accion=eliminar&id=$ID" "$BASE"
L=$(curl -s -b "$COOKIES" -c "$COOKIES" "$BASE")
verificar "POST eliminar borra la especialidad de prueba" "$L" "Especialidad eliminada"
[ "$(filas "$L")" = "$INICIAL" ] && APROBADAS=$((APROBADAS+1)) && echo "✓ el listado volvió a $INICIAL especialidades"
rm -f "$COOKIES"; echo "Resultado: $APROBADAS verificaciones aprobadas."
