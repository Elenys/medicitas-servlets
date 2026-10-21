package co.edu.sena.medicitas.web;

import co.edu.sena.medicitas.datos.ConexionBD;
import co.edu.sena.medicitas.datos.DatosException;
import co.edu.sena.medicitas.datos.EspecialidadDao;
import co.edu.sena.medicitas.datos.EspecialidadDaoJdbc;
import co.edu.sena.medicitas.modelo.Especialidad;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

/**
 * Controlador web del módulo de especialidades médicas de MediCitas.
 *
 * <ul>
 *   <li><b>GET</b> {@code /especialidades} — listado; con {@code ?buscar=} filtra por nombre;
 *       con {@code ?accion=nuevo} o {@code ?accion=editar&id=} muestra el formulario.</li>
 *   <li><b>POST</b> {@code /especialidades} — {@code accion=crear}, {@code actualizar} o {@code eliminar}
 *       desde los formularios HTML.</li>
 * </ul>
 *
 * <p>Después de cada POST se redirige con GET (patrón Post/Redirect/Get), de modo que recargar la
 * página no repite la operación. El mensaje del resultado viaja en la sesión y se muestra una vez.</p>
 *
 * @author Carlas Elenys Montalvo Mendoza
 * @author Freyder López
 */
@WebServlet(name = "EspecialidadServlet", urlPatterns = "/especialidades")
public class EspecialidadServlet extends HttpServlet {

    private static final String VISTA_LISTADO = "/WEB-INF/jsp/especialidades.jsp";
    private static final String VISTA_FORMULARIO = "/WEB-INF/jsp/formulario.jsp";
    private static final String ATRIBUTO_MENSAJE = "mensaje";
    private static final String ATRIBUTO_ERROR = "error";

    private transient EspecialidadDao especialidadDao;

    @Override
    public void init() {
        ConexionBD conexionBD = ConexionBD.desde(getServletContext().getResourceAsStream("/WEB-INF/db.properties"));
        especialidadDao = new EspecialidadDaoJdbc(conexionBD);
    }

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta) throws ServletException, IOException {
        moverMensajesDeSesion(solicitud);
        String accion = valorOVacio(solicitud.getParameter("accion"));
        try {
            switch (accion) {
                case "nuevo" -> mostrarFormulario(solicitud, respuesta, new Especialidad("Nueva especialidad", null), false);
                case "editar" -> editar(solicitud, respuesta);
                default -> listar(solicitud, respuesta);
            }
        } catch (DatosException error) {
            solicitud.setAttribute(ATRIBUTO_ERROR, error.getMessage());
            solicitud.getRequestDispatcher(VISTA_LISTADO).forward(solicitud, respuesta);
        }
    }

    @Override
    protected void doPost(HttpServletRequest solicitud, HttpServletResponse respuesta) throws ServletException, IOException {
        solicitud.setCharacterEncoding("UTF-8");
        String accion = valorOVacio(solicitud.getParameter("accion"));
        HttpSession sesion = solicitud.getSession();
        try {
            switch (accion) {
                case "crear" -> {
                    Especialidad nueva = especialidadDao.insertar(leerFormulario(solicitud, null));
                    sesion.setAttribute(ATRIBUTO_MENSAJE, "Especialidad «" + nueva.getNombre() + "» creada con el id " + nueva.getId() + ".");
                }
                case "actualizar" -> {
                    Especialidad editada = leerFormulario(solicitud, leerId(solicitud));
                    boolean actualizada = especialidadDao.actualizar(editada);
                    sesion.setAttribute(actualizada ? ATRIBUTO_MENSAJE : ATRIBUTO_ERROR,
                            actualizada ? "Especialidad «" + editada.getNombre() + "» actualizada." : "La especialidad ya no existe.");
                }
                case "eliminar" -> {
                    boolean eliminada = especialidadDao.eliminar(leerId(solicitud));
                    sesion.setAttribute(eliminada ? ATRIBUTO_MENSAJE : ATRIBUTO_ERROR,
                            eliminada ? "Especialidad eliminada." : "La especialidad ya no existe.");
                }
                default -> sesion.setAttribute(ATRIBUTO_ERROR, "Acción no reconocida.");
            }
        } catch (DatosException | IllegalArgumentException error) {
            if (!"eliminar".equals(accion)) {
                // Se vuelve a mostrar el formulario con lo que escribió el usuario y el error
                solicitud.setAttribute(ATRIBUTO_ERROR, error.getMessage());
                mostrarFormulario(solicitud, respuesta, null, "actualizar".equals(accion));
                return;
            }
            sesion.setAttribute(ATRIBUTO_ERROR, error.getMessage());
        }
        respuesta.sendRedirect(solicitud.getContextPath() + "/especialidades");
    }

    private void listar(HttpServletRequest solicitud, HttpServletResponse respuesta) throws ServletException, IOException {
        String buscar = valorOVacio(solicitud.getParameter("buscar")).trim();
        solicitud.setAttribute("buscar", buscar);
        solicitud.setAttribute("especialidades",
                buscar.isEmpty() ? especialidadDao.listarTodas() : especialidadDao.buscarPorNombre(buscar));
        solicitud.getRequestDispatcher(VISTA_LISTADO).forward(solicitud, respuesta);
    }

    private void editar(HttpServletRequest solicitud, HttpServletResponse respuesta) throws ServletException, IOException {
        Optional<Especialidad> encontrada = especialidadDao.buscarPorId(leerId(solicitud));
        if (encontrada.isEmpty()) {
            solicitud.getSession().setAttribute(ATRIBUTO_ERROR, "La especialidad no existe.");
            respuesta.sendRedirect(solicitud.getContextPath() + "/especialidades");
            return;
        }
        mostrarFormulario(solicitud, respuesta, encontrada.get(), true);
    }

    /**
     * Muestra el formulario. Si {@code especialidad} es {@code null} se reutilizan los valores enviados
     * (para no perder lo escrito cuando hay un error de validación).
     */
    private void mostrarFormulario(HttpServletRequest solicitud, HttpServletResponse respuesta,
                                   Especialidad especialidad, boolean edicion) throws ServletException, IOException {
        solicitud.setAttribute("edicion", edicion);
        if (especialidad != null) {
            solicitud.setAttribute("id", especialidad.getId());
            solicitud.setAttribute("nombre", edicion ? especialidad.getNombre() : "");
            solicitud.setAttribute("descripcion", valorOVacio(especialidad.getDescripcion()));
        } else {
            solicitud.setAttribute("id", solicitud.getParameter("id"));
            solicitud.setAttribute("nombre", solicitud.getParameter("nombre"));
            solicitud.setAttribute("descripcion", solicitud.getParameter("descripcion"));
        }
        solicitud.getRequestDispatcher(VISTA_FORMULARIO).forward(solicitud, respuesta);
    }

    private static Especialidad leerFormulario(HttpServletRequest solicitud, Integer id) {
        return new Especialidad(id, solicitud.getParameter("nombre"), solicitud.getParameter("descripcion"));
    }

    private static int leerId(HttpServletRequest solicitud) {
        try {
            return Integer.parseInt(valorOVacio(solicitud.getParameter("id")));
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("Identificador de especialidad no válido.");
        }
    }

    /** Pasa a la petición los mensajes guardados en la sesión por el POST anterior y los borra. */
    private static void moverMensajesDeSesion(HttpServletRequest solicitud) {
        HttpSession sesion = solicitud.getSession(false);
        if (sesion == null) {
            return;
        }
        for (String atributo : new String[] {ATRIBUTO_MENSAJE, ATRIBUTO_ERROR}) {
            Object valor = sesion.getAttribute(atributo);
            if (valor != null) {
                solicitud.setAttribute(atributo, valor);
                sesion.removeAttribute(atributo);
            }
        }
    }

    private static String valorOVacio(String valor) {
        return valor == null ? "" : valor;
    }
}
