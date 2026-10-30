package cr.ac.una.mahn.controlador.mantenimiento;

import cr.ac.una.mahn.datos.MuseoDAO;
import cr.ac.una.mahn.datos.PrecioDAO;
import cr.ac.una.mahn.datos.SalaDAO;
import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.excepcion.ValidacionException;
import cr.ac.una.mahn.modelo.Opcion;
import cr.ac.una.mahn.modelo.Precio;
import cr.ac.una.mahn.modelo.Sala;
import cr.ac.una.mahn.util.Tablas;
import cr.ac.una.mahn.util.Validador;
import cr.ac.una.mahn.vista.Formulario;
import java.math.BigDecimal;
import java.util.List;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;

/**
 * Mantenimiento de precios: cada sala tiene un precio de lunes a sábado y un
 * precio diferido para los domingos.
 */
public class PreciosController extends MantenimientoController<Precio> {

    private static final BigDecimal PRECIO_MAXIMO = new BigDecimal("9999.99");

    private final PrecioDAO dao = new PrecioDAO();
    private final MuseoDAO museoDAO = new MuseoDAO();
    private final SalaDAO salaDAO = new SalaDAO();

    @Override
    protected String titulo() {
        return "Precios";
    }

    @Override
    protected String etiquetaFiltroTexto() {
        return "Sala:";
    }

    @Override
    protected String etiquetaFiltroCombo() {
        return "Museo:";
    }

    @Override
    protected List<Opcion> opcionesFiltro() throws DatosException {
        return museoDAO.listar().stream().map(m -> new Opcion(m.getId(), m.getNombre())).toList();
    }

    @Override
    protected List<TableColumn<Precio, ?>> columnas() {
        return List.of(
                Tablas.texto("Sala", Precio::getSala, 320),
                Tablas.texto("Museo", Precio::getMuseo, 260),
                Tablas.moneda("Lunes a sábado", Precio::getPrecioSemana, 150),
                Tablas.moneda("Domingo", Precio::getPrecioDomingo, 150));
    }

    @Override
    protected List<Precio> consultar(String texto, Integer idFiltro) throws DatosException {
        return dao.buscar(texto, idFiltro);
    }

    @Override
    protected Formulario<Precio> crearFormulario(Precio precio) throws DatosException, ValidacionException {
        boolean nuevo = precio == null;
        Formulario<Precio> f = new Formulario<>(nuevo ? "Insertar precio" : "Editar precio",
                nuevo ? "Precio de una sala" : "Precio de «" + precio.getSala() + "»");
        ComboBox<Sala> sala = null;
        if (nuevo) {
            List<Sala> sinPrecio = salaDAO.listarSinPrecio();
            if (sinPrecio.isEmpty()) {
                throw new ValidacionException("Todas las salas ya tienen precio. Para cambiar un precio selecciónelo "
                        + "en la tabla y presione EDITAR.");
            }
            sala = f.campo("Sala:", Formulario.combo(sinPrecio, Sala::getNombreConMuseo));
            f.nota("Solo se muestran las salas que todavía no tienen precio.");
        } else {
            f.campo("Sala:", new Label(precio.getSala() + " (" + precio.getMuseo() + ")"));
        }
        TextField semana = f.campo("Precio lunes a sábado ($):", Formulario.texto(
                nuevo ? "" : precio.getPrecioSemana().toPlainString(), "Ej.: 12.50"));
        TextField domingo = f.campo("Precio domingo ($):", Formulario.texto(
                nuevo ? "" : precio.getPrecioDomingo().toPlainString(), "Ej.: 15.00"));
        ComboBox<Sala> comboSala = sala;
        f.alConstruir(() -> {
            Precio p = new Precio();
            if (nuevo) {
                Sala s = Validador.seleccion(comboSala.getValue(), "Sala");
                p.setIdSala(s.getId());
                p.setSala(s.getNombre());
                p.setIdMuseo(s.getIdMuseo());
                p.setMuseo(s.getMuseo());
            } else {
                p.setId(precio.getId());
                p.setIdSala(precio.getIdSala());
                p.setSala(precio.getSala());
                p.setIdMuseo(precio.getIdMuseo());
                p.setMuseo(precio.getMuseo());
            }
            p.setPrecioSemana(Validador.decimal(semana.getText(), "Precio lunes a sábado", BigDecimal.ZERO, false,
                    PRECIO_MAXIMO, 2));
            p.setPrecioDomingo(Validador.decimal(domingo.getText(), "Precio domingo", BigDecimal.ZERO, false,
                    PRECIO_MAXIMO, 2));
            return p;
        });
        return f;
    }

    @Override
    protected void guardarNuevo(Precio precio) throws DatosException {
        dao.insertar(precio);
    }

    @Override
    protected void guardarCambios(Precio precio) throws DatosException {
        dao.actualizar(precio);
    }

    @Override
    protected void borrar(Precio precio) throws DatosException {
        dao.eliminar(precio.getId());
    }

    @Override
    protected String describir(Precio precio) {
        return "el precio de la sala «" + precio.getSala() + "»";
    }

    @Override
    protected int idDe(Precio precio) {
        return precio.getId();
    }
}
