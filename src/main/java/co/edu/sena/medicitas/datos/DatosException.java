package co.edu.sena.medicitas.datos;

/**
 * Error de acceso a datos con un mensaje comprensible para el usuario.
 *
 * <p>Envuelve la {@link java.sql.SQLException} original para no exponer detalles técnicos
 * en la interfaz, pero la conserva como causa para el diagnóstico.</p>
 *
 * @author Carlas Elenys Montalvo Mendoza
 * @author Freyder López
 */
public class DatosException extends RuntimeException {

    public DatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
