package co.edu.sena.medicitas.datos;

import co.edu.sena.medicitas.modelo.Especialidad;

import java.util.List;
import java.util.Optional;

/**
 * Operaciones de acceso a datos (CRUD) sobre las especialidades médicas.
 *
 * <p>Separar la interfaz de su implementación permite cambiar la tecnología de
 * persistencia sin modificar la capa que la usa (patrón DAO).</p>
 *
 * @author Carlas Elenys Montalvo Mendoza
 * @author Freyder López
 */
public interface EspecialidadDao {

    /** Inserta la especialidad y le asigna el identificador generado por la base de datos. */
    Especialidad insertar(Especialidad especialidad);

    /** Devuelve todas las especialidades ordenadas por nombre. */
    List<Especialidad> listarTodas();

    /** Busca una especialidad por su identificador. */
    Optional<Especialidad> buscarPorId(int id);

    /** Busca especialidades cuyo nombre contenga el texto, sin distinguir mayúsculas. */
    List<Especialidad> buscarPorNombre(String texto);

    /**
     * Actualiza el nombre y la descripción.
     *
     * @return {@code true} si existía y se actualizó
     */
    boolean actualizar(Especialidad especialidad);

    /**
     * Elimina la especialidad.
     *
     * @return {@code true} si existía y se eliminó
     */
    boolean eliminar(int id);
}
