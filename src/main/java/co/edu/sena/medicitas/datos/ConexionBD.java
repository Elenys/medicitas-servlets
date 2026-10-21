package co.edu.sena.medicitas.datos;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre conexiones JDBC a la base de datos PostgreSQL de MediCitas.
 *
 * <p>En la aplicación web los datos de conexión se leen de {@code WEB-INF/db.properties}
 * (que no se sube al repositorio). Cada variable de entorno {@code MEDICITAS_DB_URL},
 * {@code MEDICITAS_DB_USUARIO} y {@code MEDICITAS_DB_CLAVE}, si existe, tiene prioridad.</p>
 *
 * @author Carlas Elenys Montalvo Mendoza
 * @author Freyder López
 */
public final class ConexionBD {

    static {
        // En un servidor de aplicaciones el driver se registra explícitamente con el cargador de la aplicación.
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException error) {
            throw new IllegalStateException("No se encontró el driver JDBC de PostgreSQL en WEB-INF/lib.", error);
        }
    }

    private final String url;
    private final String usuario;
    private final String clave;

    private ConexionBD(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    /**
     * Crea la configuración a partir de un archivo de propiedades (puede ser {@code null})
     * y de las variables de entorno.
     *
     * @throws DatosException si falta algún dato de conexión
     */
    public static ConexionBD desde(InputStream archivoPropiedades) {
        Properties propiedades = new Properties();
        if (archivoPropiedades != null) {
            try (InputStream entrada = archivoPropiedades) {
                propiedades.load(entrada);
            } catch (IOException error) {
                throw new DatosException("No se pudo leer la configuración de la base de datos.", error);
            }
        }
        return new ConexionBD(
                leerValor(propiedades, "db.url", "MEDICITAS_DB_URL"),
                leerValor(propiedades, "db.usuario", "MEDICITAS_DB_USUARIO"),
                leerValor(propiedades, "db.clave", "MEDICITAS_DB_CLAVE"));
    }

    /** Abre una conexión nueva. Quien la recibe debe cerrarla (try-with-resources). */
    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    private static String leerValor(Properties propiedades, String clavePropiedad, String variableEntorno) {
        String valor = System.getenv(variableEntorno);
        if (valor == null || valor.isBlank()) {
            valor = propiedades.getProperty(clavePropiedad);
        }
        if (valor == null || valor.isBlank()) {
            throw new DatosException("Falta el dato de conexión «" + clavePropiedad
                    + "». Copie config/db.properties.ejemplo como config/db.properties.", null);
        }
        return valor.trim();
    }
}
