#!/bin/sh
# Compila el módulo, arma medicitas-especialidades.war y lo despliega en Tomcat 10.1.
# Requiere Java 17+ y Tomcat 10.1 (Jakarta EE 10). Uso:
#   TOMCAT_HOME=~/apache-tomcat-10.1.61 ./construir.sh
set -e
cd "$(dirname "$0")"
TOMCAT_HOME="${TOMCAT_HOME:-$HOME/apache-tomcat-10.1.61}"
APP=medicitas-especialidades
SALIDA="build/$APP"

[ -f config/db.properties ] || { echo "Falta config/db.properties (copie config/db.properties.ejemplo)"; exit 1; }
rm -rf build && mkdir -p "$SALIDA/WEB-INF/classes" "$SALIDA/WEB-INF/lib"

# 1. Compilar contra la API de Servlet que trae Tomcat
javac --release 17 -encoding UTF-8 -d "$SALIDA/WEB-INF/classes" \
      -cp "$TOMCAT_HOME/lib/servlet-api.jar:lib/*" $(find src/main/java -name "*.java")

# 2. Copiar vistas, estilos, librerías y configuración
cp -R src/main/webapp/. "$SALIDA/"
cp lib/*.jar "$SALIDA/WEB-INF/lib/"
cp config/db.properties "$SALIDA/WEB-INF/db.properties"

# 3. Empaquetar el WAR y desplegarlo
(cd "$SALIDA" && jar --create --file "../$APP.war" .)
cp "build/$APP.war" "$TOMCAT_HOME/webapps/"
echo "Desplegado: http://localhost:8080/$APP/especialidades"
