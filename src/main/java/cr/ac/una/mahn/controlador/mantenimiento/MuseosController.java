package cr.ac.una.mahn.controlador.mantenimiento;

import cr.ac.una.mahn.datos.CatalogoDAO;
import cr.ac.una.mahn.datos.MuseoDAO;
import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.modelo.Museo;
import cr.ac.una.mahn.modelo.Opcion;
import cr.ac.una.mahn.modelo.TipoMuseo;
import cr.ac.una.mahn.util.Tablas;
import cr.ac.una.mahn.util.Validador;
import cr.ac.una.mahn.vista.Formulario;
import java.util.List;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;

/** Mantenimiento de museos: nombre, tipo, ubicación, fundación, director y sitio web. */
public class MuseosController extends MantenimientoController<Museo> {

    private final MuseoDAO dao = new MuseoDAO();
    private final CatalogoDAO catalogo = new CatalogoDAO();

    @Override
    protected String titulo() {
        return "Museos";
    }

    @Override
    protected String etiquetaFiltroTexto() {
        return "Nombre, director o ubicación:";
    }

    @Override
    protected String etiquetaFiltroCombo() {
        return "Tipo de museo:";
    }

    @Override
    protected List<Opcion> opcionesFiltro() throws DatosException {
        return catalogo.tiposMuseo().stream().map(t -> new Opcion(t.getId(), t.getNombre())).toList();
    }

    @Override
    protected List<TableColumn<Museo, ?>> columnas() {
        return List.of(
                Tablas.texto("Nombre", Museo::getNombre, 230),
                Tablas.texto("Tipo", Museo::getTipo, 85),
                Tablas.texto("Ubicación", Museo::getUbicacion, 270),
                Tablas.fecha("Fundación", Museo::getFechaFundacion, 95),
                Tablas.texto("Director(a)", Museo::getDirector, 160),
                Tablas.texto("Sitio web", Museo::getSitioWeb, 180));
    }

    @Override
    protected List<Museo> consultar(String texto, Integer idFiltro) throws DatosException {
        return dao.buscar(texto, idFiltro);
    }

    @Override
    protected Formulario<Museo> crearFormulario(Museo museo) throws DatosException {
        boolean nuevo = museo == null;
        Formulario<Museo> f = new Formulario<>(nuevo ? "Insertar museo" : "Editar museo",
                nuevo ? "Nuevo museo" : "Editar «" + museo.getNombre() + "»");
        TextField nombre = f.campo("Nombre:", Formulario.texto(nuevo ? "" : museo.getNombre(),
                "Ej.: Museo Americano de Historia Natural"));
        ComboBox<TipoMuseo> tipo = f.campo("Tipo de museo:", Formulario.combo(catalogo.tiposMuseo(), TipoMuseo::getNombre));
        TextField ubicacion = f.campo("Ubicación:", Formulario.texto(nuevo ? "" : museo.getUbicacion(),
                "Dirección, ciudad y país"));
        DatePicker fundacion = f.campo("Fecha de fundación:", Formulario.selectorFecha(nuevo ? null : museo.getFechaFundacion()));
        TextField director = f.campo("Director(a):", Formulario.texto(nuevo ? "" : museo.getDirector(), "Nombre completo"));
        TextField sitio = f.campo("Sitio web:", Formulario.texto(nuevo ? "" : museo.getSitioWeb(), "https://…"));
        if (!nuevo) {
            Formulario.seleccionar(tipo, TipoMuseo::getId, museo.getIdTipo());
        }
        f.alConstruir(() -> {
            Museo m = new Museo();
            m.setId(nuevo ? 0 : museo.getId());
            m.setNombre(Validador.texto(nombre.getText(), "Nombre", 3, 120));
            TipoMuseo t = Validador.seleccion(tipo.getValue(), "Tipo de museo");
            m.setIdTipo(t.getId());
            m.setTipo(t.getNombre());
            m.setUbicacion(Validador.texto(ubicacion.getText(), "Ubicación", 5, 200));
            m.setFechaFundacion(Validador.fechaPasada(fundacion, "Fecha de fundación"));
            m.setDirector(Validador.nombrePersona(director.getText(), "Director(a)"));
            m.setSitioWeb(Validador.url(sitio.getText(), "Sitio web"));
            return m;
        });
        return f;
    }

    @Override
    protected void guardarNuevo(Museo museo) throws DatosException {
        dao.insertar(museo);
    }

    @Override
    protected void guardarCambios(Museo museo) throws DatosException {
        dao.actualizar(museo);
    }

    @Override
    protected void borrar(Museo museo) throws DatosException {
        dao.eliminar(museo.getId());
    }

    @Override
    protected String describir(Museo museo) {
        return "el museo «" + museo.getNombre() + "»";
    }

    @Override
    protected int idDe(Museo museo) {
        return museo.getId();
    }
}
