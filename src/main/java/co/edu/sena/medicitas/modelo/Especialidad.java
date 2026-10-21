package co.edu.sena.medicitas.modelo;

import java.util.Objects;

/**
 * Especialidad médica del catálogo de MediCitas (tabla {@code specialties}).
 *
 * <p>Cada médico pertenece a una especialidad, y el paciente elige la especialidad
 * como primer paso para agendar una cita.</p>
 *
 * @author Carlas Elenys Montalvo Mendoza
 * @author Freyder López
 */
public class Especialidad {

    /** Longitud máxima del nombre, igual a la columna {@code name VARCHAR(80)}. */
    public static final int LONGITUD_MAXIMA_NOMBRE = 80;

    /** Longitud máxima de la descripción, igual a la columna {@code description VARCHAR(255)}. */
    public static final int LONGITUD_MAXIMA_DESCRIPCION = 255;

    private Integer id;
    private String nombre;
    private String descripcion;

    /** Crea una especialidad nueva, todavía sin identificador. */
    public Especialidad(String nombre, String descripcion) {
        this(null, nombre, descripcion);
    }

    /** Crea una especialidad con todos sus datos, por ejemplo al leerla de la base de datos. */
    public Especialidad(Integer id, String nombre, String descripcion) {
        this.id = id;
        setNombre(nombre);
        setDescripcion(descripcion);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    /**
     * Asigna el nombre, sin espacios sobrantes.
     *
     * @throws IllegalArgumentException si está vacío o supera {@value #LONGITUD_MAXIMA_NOMBRE} caracteres
     */
    public void setNombre(String nombre) {
        String nombreLimpio = nombre == null ? "" : nombre.trim();
        if (nombreLimpio.isEmpty()) {
            throw new IllegalArgumentException("El nombre de la especialidad es obligatorio.");
        }
        if (nombreLimpio.length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new IllegalArgumentException("El nombre no puede superar " + LONGITUD_MAXIMA_NOMBRE + " caracteres.");
        }
        this.nombre = nombreLimpio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Asigna la descripción (opcional). Una descripción vacía se guarda como {@code null}.
     *
     * @throws IllegalArgumentException si supera {@value #LONGITUD_MAXIMA_DESCRIPCION} caracteres
     */
    public void setDescripcion(String descripcion) {
        String descripcionLimpia = descripcion == null ? "" : descripcion.trim();
        if (descripcionLimpia.length() > LONGITUD_MAXIMA_DESCRIPCION) {
            throw new IllegalArgumentException("La descripción no puede superar " + LONGITUD_MAXIMA_DESCRIPCION + " caracteres.");
        }
        this.descripcion = descripcionLimpia.isEmpty() ? null : descripcionLimpia;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof Especialidad especialidad)) {
            return false;
        }
        return Objects.equals(id, especialidad.id) && Objects.equals(nombre, especialidad.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre);
    }

    @Override
    public String toString() {
        return String.format("%3d | %-20s | %s", id, nombre, descripcion == null ? "—" : descripcion);
    }
}
