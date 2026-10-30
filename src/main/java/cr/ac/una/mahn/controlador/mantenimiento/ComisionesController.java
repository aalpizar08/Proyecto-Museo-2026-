package cr.ac.una.mahn.controlador.mantenimiento;

import cr.ac.una.mahn.datos.TarjetaDAO;
import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.modelo.Opcion;
import cr.ac.una.mahn.modelo.Tarjeta;
import cr.ac.una.mahn.util.Tablas;
import cr.ac.una.mahn.util.Validador;
import cr.ac.una.mahn.vista.Formulario;
import java.math.BigDecimal;
import java.util.List;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;

/**
 * Mantenimiento de comisiones de tarjetas: tipo de tarjeta (VISA, Mastercard,
 * American Express, Diners Club, Union Pay) y comisión cobrada por transacción.
 */
public class ComisionesController extends MantenimientoController<Tarjeta> {

    private final TarjetaDAO dao = new TarjetaDAO();

    @Override
    protected String titulo() {
        return "Comisiones de tarjetas";
    }

    @Override
    protected String etiquetaFiltroTexto() {
        return "Nombre de la tarjeta:";
    }

    @Override
    protected String etiquetaFiltroCombo() {
        return "Tipo de tarjeta:";
    }

    @Override
    protected List<Opcion> opcionesFiltro() throws DatosException {
        return dao.listar().stream().map(t -> new Opcion(t.getId(), t.getNombre())).toList();
    }

    @Override
    protected List<TableColumn<Tarjeta, ?>> columnas() {
        return List.of(
                Tablas.texto("Tipo de tarjeta", Tarjeta::getNombre, 300),
                Tablas.porcentaje("Comisión por transacción", Tarjeta::getPorcentajeComision, 220),
                Tablas.texto("Estado", t -> t.isActiva() ? "Activa (se acepta en cajas)" : "Inactiva", 240));
    }

    @Override
    protected List<Tarjeta> consultar(String texto, Integer idFiltro) throws DatosException {
        return dao.buscar(texto, idFiltro);
    }

    @Override
    protected Formulario<Tarjeta> crearFormulario(Tarjeta tarjeta) {
        boolean nueva = tarjeta == null;
        Formulario<Tarjeta> f = new Formulario<>(nueva ? "Insertar tarjeta" : "Editar tarjeta",
                nueva ? "Nuevo tipo de tarjeta" : "Editar «" + tarjeta.getNombre() + "»");
        TextField nombre = f.campo("Tipo de tarjeta:", Formulario.texto(nueva ? "" : tarjeta.getNombre(),
                "Ej.: VISA"));
        TextField porcentaje = f.campo("Comisión (%):", Formulario.texto(
                nueva ? "" : tarjeta.getPorcentajeComision().toPlainString(), "Porcentaje por transacción, ej. 2.75"));
        CheckBox activa = f.campo("Estado:", new CheckBox("Activa (se acepta en las cajas)"));
        activa.setSelected(nueva || tarjeta.isActiva());
        f.nota("La comisión de cada venta se registra automáticamente con el porcentaje vigente al momento de vender.");
        f.alConstruir(() -> {
            Tarjeta t = new Tarjeta();
            t.setId(nueva ? 0 : tarjeta.getId());
            t.setNombre(Validador.texto(nombre.getText(), "Tipo de tarjeta", 2, 40));
            t.setPorcentajeComision(Validador.decimal(porcentaje.getText(), "Comisión (%)", BigDecimal.ZERO, false,
                    new BigDecimal("100"), 2));
            t.setActiva(activa.isSelected());
            return t;
        });
        return f;
    }

    @Override
    protected void guardarNuevo(Tarjeta tarjeta) throws DatosException {
        dao.insertar(tarjeta);
    }

    @Override
    protected void guardarCambios(Tarjeta tarjeta) throws DatosException {
        dao.actualizar(tarjeta);
    }

    @Override
    protected void borrar(Tarjeta tarjeta) throws DatosException {
        try {
            dao.eliminar(tarjeta.getId());
        } catch (DatosException ex) {
            if (ex.getMessage().startsWith("No se puede eliminar")) {
                throw new DatosException(ex.getMessage() + " Si la tarjeta ya no se acepta, edítela y desmarque «Activa».",
                        ex);
            }
            throw ex;
        }
    }

    @Override
    protected String describir(Tarjeta tarjeta) {
        return "la tarjeta «" + tarjeta.getNombre() + "»";
    }

    @Override
    protected int idDe(Tarjeta tarjeta) {
        return tarjeta.getId();
    }
}
