package co.edu.sena.medicitas.datos;

import co.edu.sena.medicitas.modelo.Especialidad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de {@link EspecialidadDao} sobre la tabla {@code specialties} de PostgreSQL.
 *
 * <p>Todas las consultas usan {@link PreparedStatement}: los valores viajan como parámetros
 * y nunca se concatenan al SQL, lo que evita la inyección de SQL. Las conexiones, sentencias y
 * resultados se cierran siempre con try-with-resources.</p>
 *
 * @author Carlas Elenys Montalvo Mendoza
 * @author Freyder López
 */
public class EspecialidadDaoJdbc implements EspecialidadDao {

    // Códigos SQLSTATE estándar de PostgreSQL
    private static final String VIOLACION_UNICIDAD = "23505";
    private static final String VIOLACION_LLAVE_FORANEA = "23503";

    private static final String SQL_INSERTAR =
            "INSERT INTO specialties (name, description) VALUES (?, ?)";
    private static final String SQL_LISTAR =
            "SELECT id, name, description FROM specialties ORDER BY name";
    private static final String SQL_BUSCAR_POR_ID =
            "SELECT id, name, description FROM specialties WHERE id = ?";
    private static final String SQL_BUSCAR_POR_NOMBRE =
            "SELECT id, name, description FROM specialties WHERE name ILIKE ? ORDER BY name";
    private static final String SQL_ACTUALIZAR =
            "UPDATE specialties SET name = ?, description = ? WHERE id = ?";
    private static final String SQL_ELIMINAR =
            "DELETE FROM specialties WHERE id = ?";

    private final ConexionBD conexionBD;

    public EspecialidadDaoJdbc(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    @Override
    public Especialidad insertar(Especialidad especialidad) {
        try (Connection conexion = conexionBD.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, especialidad.getNombre());
            asignarDescripcion(sentencia, 2, especialidad.getDescripcion());
            sentencia.executeUpdate();
            try (ResultSet llaves = sentencia.getGeneratedKeys()) {
                if (llaves.next()) {
                    especialidad.setId(llaves.getInt(1));
                }
            }
            return especialidad;
        } catch (SQLException error) {
            throw traducir(error, "insertar", especialidad.getNombre());
        }
    }

    @Override
    public List<Especialidad> listarTodas() {
        try (Connection conexion = conexionBD.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_LISTAR);
             ResultSet resultado = sentencia.executeQuery()) {
            return leerLista(resultado);
        } catch (SQLException error) {
            throw traducir(error, "listar", null);
        }
    }

    @Override
    public Optional<Especialidad> buscarPorId(int id) {
        try (Connection conexion = conexionBD.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {
            sentencia.setInt(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(leerFila(resultado)) : Optional.empty();
            }
        } catch (SQLException error) {
            throw traducir(error, "buscar", null);
        }
    }

    @Override
    public List<Especialidad> buscarPorNombre(String texto) {
        try (Connection conexion = conexionBD.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_POR_NOMBRE)) {
            sentencia.setString(1, "%" + texto.trim() + "%");
            try (ResultSet resultado = sentencia.executeQuery()) {
                return leerLista(resultado);
            }
        } catch (SQLException error) {
            throw traducir(error, "buscar", null);
        }
    }

    @Override
    public boolean actualizar(Especialidad especialidad) {
        try (Connection conexion = conexionBD.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR)) {
            sentencia.setString(1, especialidad.getNombre());
            asignarDescripcion(sentencia, 2, especialidad.getDescripcion());
            sentencia.setInt(3, especialidad.getId());
            return sentencia.executeUpdate() == 1;
        } catch (SQLException error) {
            throw traducir(error, "actualizar", especialidad.getNombre());
        }
    }

    @Override
    public boolean eliminar(int id) {
        try (Connection conexion = conexionBD.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ELIMINAR)) {
            sentencia.setInt(1, id);
            return sentencia.executeUpdate() == 1;
        } catch (SQLException error) {
            throw traducir(error, "eliminar", null);
        }
    }

    private static void asignarDescripcion(PreparedStatement sentencia, int posicion, String descripcion) throws SQLException {
        if (descripcion == null) {
            sentencia.setNull(posicion, Types.VARCHAR);
        } else {
            sentencia.setString(posicion, descripcion);
        }
    }

    private static List<Especialidad> leerLista(ResultSet resultado) throws SQLException {
        List<Especialidad> especialidades = new ArrayList<>();
        while (resultado.next()) {
            especialidades.add(leerFila(resultado));
        }
        return especialidades;
    }

    private static Especialidad leerFila(ResultSet resultado) throws SQLException {
        return new Especialidad(resultado.getInt("id"), resultado.getString("name"), resultado.getString("description"));
    }

    /** Convierte los errores de la base de datos en mensajes que el usuario entiende. */
    private static DatosException traducir(SQLException error, String operacion, String nombre) {
        if (VIOLACION_UNICIDAD.equals(error.getSQLState())) {
            return new DatosException("Ya existe una especialidad llamada «" + nombre + "».", error);
        }
        if (VIOLACION_LLAVE_FORANEA.equals(error.getSQLState())) {
            return new DatosException("No se puede eliminar: hay médicos asignados a esta especialidad.", error);
        }
        return new DatosException("No fue posible " + operacion + " la especialidad: " + error.getMessage(), error);
    }
}
