package cr.ac.una.mahn.controlador.mantenimiento;

import cr.ac.una.mahn.datos.SalaDAO;
import cr.ac.una.mahn.datos.TematicaDAO;
import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.modelo.Opcion;
import cr.ac.una.mahn.modelo.Sala;
import cr.ac.una.mahn.modelo.Tematica;
import cr.ac.una.mahn.util.Tablas;
import cr.ac.una.mahn.util.Validador;
import cr.ac.una.mahn.vista.Formulario;
import cr.ac.una.mahn.vista.SelectorImagen;
import java.util.List;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/** Mantenimiento de temáticas de las salas que no exhiben especies. */
public class TematicasController extends MantenimientoController<Tematica> {

    private final TematicaDAO dao = new TematicaDAO();
    private final SalaDAO salaDAO = new SalaDAO();

    @Override
    protected String titulo() {
        return "Temáticas";
    }

    @Override
    protected String etiquetaFiltroTexto() {
        return "Nombre, época o características:";
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
    protected List<TableColumn<Tematica, ?>> columnas() {
        return List.of(
                Tablas.texto("Temática", Tematica::getNombre, 240),
                Tablas.texto("Sala", Tematica::getSala, 240),
                Tablas.texto("Museo", Tematica::getMuseo, 200),
                Tablas.texto("Época", Tematica::getEpoca, 170),
                Tablas.texto("Características", Tematica::getCaracteristicas, 380));
    }

    @Override
    protected List<Tematica> consultar(String texto, Integer idFiltro) throws DatosException {
        return dao.buscar(texto, idFiltro);
    }

    @Override
    protected Formulario<Tematica> crearFormulario(Tematica tematica) throws DatosException {
        boolean nueva = tematica == null;
        Formulario<Tematica> f = new Formulario<>(nueva ? "Insertar temática" : "Editar temática",
                nueva ? "Nueva temática" : "Editar «" + tematica.getNombre() + "»");
        ComboBox<Sala> sala = f.campo("Sala:", Formulario.combo(salaDAO.listar(), Sala::getNombreConMuseo));
        TextField nombre = f.campo("Nombre de la temática:", Formulario.texto(nueva ? "" : tematica.getNombre(),
                "Ej.: El meteorito Willamette"));
        TextField epoca = f.campo("Época de la temática:", Formulario.texto(nueva ? "" : tematica.getEpoca(),
                "Ej.: Siglo XIX, 1200 - 400 a. C., Actual"));
        TextArea caracteristicas = f.campo("Características:", Formulario.areaTexto(nueva ? "" : tematica.getCaracteristicas(),
                "Características de la temática", 5));
        SelectorImagen imagen = f.campo("Imagen:", new SelectorImagen(nueva ? null : tematica.getRutaImagen(), "tematicas"));
        if (!nueva) {
            Formulario.seleccionar(sala, Sala::getId, tematica.getIdSala());
        }
        f.alConstruir(() -> {
            Tematica t = new Tematica();
            t.setId(nueva ? 0 : tematica.getId());
            Sala s = Validador.seleccion(sala.getValue(), "Sala");
            t.setIdSala(s.getId());
            t.setSala(s.getNombre());
            t.setMuseo(s.getMuseo());
            t.setNombre(Validador.texto(nombre.getText(), "Nombre de la temática", 3, 120));
            t.setEpoca(Validador.texto(epoca.getText(), "Época de la temática", 2, 100));
            t.setCaracteristicas(Validador.texto(caracteristicas.getText(), "Características", 10, 1000));
            t.setRutaImagen(imagen.resolverRuta());
            return t;
        });
        return f;
    }

    @Override
    protected void guardarNuevo(Tematica tematica) throws DatosException {
        dao.insertar(tematica);
    }

    @Override
    protected void guardarCambios(Tematica tematica) throws DatosException {
        dao.actualizar(tematica);
    }

    @Override
    protected void borrar(Tematica tematica) throws DatosException {
        dao.eliminar(tematica.getId());
    }

    @Override
    protected String describir(Tematica tematica) {
        return "la temática «" + tematica.getNombre() + "»";
    }

    @Override
    protected int idDe(Tematica tematica) {
        return tematica.getId();
    }
}
