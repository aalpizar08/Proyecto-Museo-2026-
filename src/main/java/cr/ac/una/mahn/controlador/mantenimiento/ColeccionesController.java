package cr.ac.una.mahn.controlador.mantenimiento;

import cr.ac.una.mahn.datos.ColeccionDAO;
import cr.ac.una.mahn.datos.SalaDAO;
import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.excepcion.ValidacionException;
import cr.ac.una.mahn.modelo.Coleccion;
import cr.ac.una.mahn.modelo.Opcion;
import cr.ac.una.mahn.modelo.Sala;
import cr.ac.una.mahn.util.NumerosRomanos;
import cr.ac.una.mahn.util.Tablas;
import cr.ac.una.mahn.util.Validador;
import cr.ac.una.mahn.vista.Formulario;
import java.math.BigDecimal;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/** Mantenimiento de colecciones: nombre, siglo y descripción de lo que representa. */
public class ColeccionesController extends MantenimientoController<Coleccion> {

    private static final String DESPUES_DE_CRISTO = "d. C. (después de Cristo)";
    private static final String ANTES_DE_CRISTO = "a. C. (antes de Cristo)";

    private final ColeccionDAO dao = new ColeccionDAO();
    private final SalaDAO salaDAO = new SalaDAO();

    @Override
    protected String titulo() {
        return "Colecciones";
    }

    @Override
    protected String etiquetaFiltroTexto() {
        return "Nombre o descripción:";
    }

    @Override
    protected String etiquetaFiltroCombo() {
        return "Sala:";
    }

    @Override
    protected List<Opcion> opcionesFiltro() throws DatosException {
        return salaDAO.listar().stream().map(s -> new Opcion(s.getId(), s.getNombreConMuseo())).toList();
    }

    @Override
    protected List<TableColumn<Coleccion, ?>> columnas() {
        return List.of(
                Tablas.texto("Colección", Coleccion::getNombre, 240),
                Tablas.texto("Siglo", Coleccion::getSigloTexto, 90),
                Tablas.texto("Sala", Coleccion::getSala, 240),
                Tablas.texto("Museo", Coleccion::getMuseo, 200),
                Tablas.texto("Descripción", Coleccion::getDescripcion, 380));
    }

    @Override
    protected List<Coleccion> consultar(String texto, Integer idFiltro) throws DatosException {
        return dao.buscar(texto, idFiltro);
    }

    @Override
    protected Formulario<Coleccion> crearFormulario(Coleccion coleccion) throws DatosException {
        boolean nueva = coleccion == null;
        Formulario<Coleccion> f = new Formulario<>(nueva ? "Insertar colección" : "Editar colección",
                nueva ? "Nueva colección" : "Editar «" + coleccion.getNombre() + "»");
        ComboBox<Sala> sala = f.campo("Sala:", Formulario.combo(salaDAO.listar(), Sala::getNombreConMuseo));
        TextField nombre = f.campo("Nombre:", Formulario.texto(nueva ? "" : coleccion.getNombre(),
                "Ej.: Colección Barnum Brown"));

        Spinner<Integer> numeroSiglo = new Spinner<>(1, 30, nueva ? 20 : Math.abs(coleccion.getSiglo()));
        numeroSiglo.setEditable(true);
        numeroSiglo.setPrefWidth(90);
        ComboBox<String> era = new ComboBox<>();
        era.getItems().setAll(DESPUES_DE_CRISTO, ANTES_DE_CRISTO);
        era.setValue(!nueva && coleccion.getSiglo() < 0 ? ANTES_DE_CRISTO : DESPUES_DE_CRISTO);
        Label romano = new Label();
        romano.getStyleClass().add("texto-suave");
        Runnable actualizarRomano = () -> romano.setText("Siglo " + NumerosRomanos.siglo(
                (ANTES_DE_CRISTO.equals(era.getValue()) ? -1 : 1) * numeroSiglo.getValue()));
        numeroSiglo.valueProperty().addListener((o, a, b) -> actualizarRomano.run());
        era.valueProperty().addListener((o, a, b) -> actualizarRomano.run());
        actualizarRomano.run();
        HBox siglo = new HBox(10, numeroSiglo, era, romano);
        siglo.setAlignment(Pos.CENTER_LEFT);
        f.campo("Siglo:", siglo);

        TextArea descripcion = f.campo("Descripción:", Formulario.areaTexto(nueva ? "" : coleccion.getDescripcion(),
                "Observación o descripción detallada de lo que representa la colección", 5));
        if (!nueva) {
            Formulario.seleccionar(sala, Sala::getId, coleccion.getIdSala());
        }
        f.alConstruir(() -> {
            Coleccion c = new Coleccion();
            c.setId(nueva ? 0 : coleccion.getId());
            Sala s = Validador.seleccion(sala.getValue(), "Sala");
            c.setIdSala(s.getId());
            c.setSala(s.getNombre());
            c.setMuseo(s.getMuseo());
            c.setNombre(Validador.texto(nombre.getText(), "Nombre", 3, 120));
            int numero = Validador.decimal(numeroSiglo.getEditor().getText(), "Siglo", BigDecimal.ONE, false,
                    new BigDecimal("30"), 0).intValue();
            c.setSiglo(ANTES_DE_CRISTO.equals(era.getValue()) ? -numero : numero);
            if (c.getSiglo() > 22) {
                throw new ValidacionException("El siglo no puede ser posterior al XXII d. C.");
            }
            c.setDescripcion(Validador.texto(descripcion.getText(), "Descripción", 10, 1000));
            return c;
        });
        return f;
    }

    @Override
    protected void guardarNuevo(Coleccion coleccion) throws DatosException {
        dao.insertar(coleccion);
    }

    @Override
    protected void guardarCambios(Coleccion coleccion) throws DatosException {
        dao.actualizar(coleccion);
    }

    @Override
    protected void borrar(Coleccion coleccion) throws DatosException {
        dao.eliminar(coleccion.getId());
    }

    @Override
    protected String describir(Coleccion coleccion) {
        return "la colección «" + coleccion.getNombre() + "»";
    }

    @Override
    protected int idDe(Coleccion coleccion) {
        return coleccion.getId();
    }
}
