package cr.ac.una.mahn.controlador.mantenimiento;

import cr.ac.una.mahn.datos.ColeccionDAO;
import cr.ac.una.mahn.datos.EspecieDAO;
import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.modelo.Coleccion;
import cr.ac.una.mahn.modelo.Especie;
import cr.ac.una.mahn.modelo.Opcion;
import cr.ac.una.mahn.util.Formatos;
import cr.ac.una.mahn.util.Tablas;
import cr.ac.una.mahn.util.Validador;
import cr.ac.una.mahn.vista.Formulario;
import cr.ac.una.mahn.vista.SelectorImagen;
import java.math.BigDecimal;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Mantenimiento de especies: nombre científico y común, extinción, época, peso,
 * tamaño, características e imagen.
 */
public class EspeciesController extends MantenimientoController<Especie> {

    private final EspecieDAO dao = new EspecieDAO();
    private final ColeccionDAO coleccionDAO = new ColeccionDAO();

    @Override
    protected String titulo() {
        return "Especies";
    }

    @Override
    protected String etiquetaFiltroTexto() {
        return "Nombre común, científico o época:";
    }

    @Override
    protected String etiquetaFiltroCombo() {
        return "Colección:";
    }

    @Override
    protected List<Opcion> opcionesFiltro() throws DatosException {
        return coleccionDAO.listar().stream().map(c -> new Opcion(c.getId(), c.getNombre())).toList();
    }

    @Override
    protected List<TableColumn<Especie, ?>> columnas() {
        return List.of(
                Tablas.texto("Nombre común", Especie::getNombreComun, 190),
                Tablas.texto("Nombre científico", Especie::getNombreCientifico, 190),
                Tablas.texto("Colección", Especie::getColeccion, 200),
                Tablas.texto("Época", Especie::getEpoca, 140),
                Tablas.texto("Extinción", e -> e.isExtinta() ? e.getFechaExtincion() : "No extinta", 170),
                Tablas.texto("Peso", e -> Formatos.peso(e.getPesoKg()), 90),
                Tablas.texto("Tamaño", e -> Formatos.tamano(e.getTamanoM()), 80),
                Tablas.texto("Características", Especie::getCaracteristicas, 320));
    }

    @Override
    protected List<Especie> consultar(String texto, Integer idFiltro) throws DatosException {
        return dao.buscar(texto, idFiltro);
    }

    @Override
    protected Formulario<Especie> crearFormulario(Especie especie) throws DatosException {
        boolean nueva = especie == null;
        Formulario<Especie> f = new Formulario<>(nueva ? "Insertar especie" : "Editar especie",
                nueva ? "Nueva especie en exhibición" : "Editar «" + especie.getNombreComun() + "»");
        ComboBox<Coleccion> coleccion = f.campo("Colección:",
                Formulario.combo(coleccionDAO.listar(), c -> c.getNombre() + " — " + c.getSala()));
        TextField cientifico = f.campo("Nombre científico:", Formulario.texto(nueva ? "" : especie.getNombreCientifico(),
                "Ej.: Tyrannosaurus rex"));
        TextField comun = f.campo("Nombre común:", Formulario.texto(nueva ? "" : especie.getNombreComun(),
                "Ej.: Tiranosaurio rex"));

        CheckBox extinta = new CheckBox("Especie extinta");
        TextField fechaExtincion = Formulario.texto(nueva ? "" : especie.getFechaExtincion(),
                "Ej.: Hace 66 millones de años, 1914…");
        extinta.setSelected(!nueva && especie.isExtinta());
        fechaExtincion.disableProperty().bind(extinta.selectedProperty().not());
        HBox filaExtincion = new HBox(12, extinta, fechaExtincion);
        filaExtincion.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(fechaExtincion, Priority.ALWAYS);
        f.campo("Fecha de extinción:", filaExtincion);

        TextField epoca = f.campo("Época en que vivió:", Formulario.texto(nueva ? "" : especie.getEpoca(),
                "Ej.: Cretácico superior, Pleistoceno, Holoceno (actual)"));
        TextField peso = f.campo("Peso (kg):", Formulario.texto(nueva ? "" : especie.getPesoKg().stripTrailingZeros().toPlainString(),
                "Peso promedio de un adulto en kilogramos (ej. 8000 o 0.045)"));
        TextField tamano = f.campo("Tamaño (m):", Formulario.texto(nueva ? "" : especie.getTamanoM().stripTrailingZeros().toPlainString(),
                "Largo, altura o envergadura en metros (ej. 12.3)"));
        TextArea caracteristicas = f.campo("Características:", Formulario.areaTexto(nueva ? "" : especie.getCaracteristicas(),
                "Características principales de la especie", 4));
        SelectorImagen imagen = f.campo("Imagen:", new SelectorImagen(nueva ? null : especie.getRutaImagen(), "especies"));
        if (!nueva) {
            Formulario.seleccionar(coleccion, Coleccion::getId, especie.getIdColeccion());
        }
        f.alConstruir(() -> {
            Especie e = new Especie();
            e.setId(nueva ? 0 : especie.getId());
            Coleccion c = Validador.seleccion(coleccion.getValue(), "Colección");
            e.setIdColeccion(c.getId());
            e.setColeccion(c.getNombre());
            e.setNombreCientifico(Validador.texto(cientifico.getText(), "Nombre científico", 3, 120));
            e.setNombreComun(Validador.texto(comun.getText(), "Nombre común", 2, 120));
            if (extinta.isSelected()) {
                e.setFechaExtincion(Validador.texto(fechaExtincion.getText(), "Fecha de extinción", 3, 80));
            } else {
                e.setFechaExtincion(null);
            }
            e.setEpoca(Validador.texto(epoca.getText(), "Época en que vivió", 3, 100));
            e.setPesoKg(Validador.decimal(peso.getText(), "Peso (kg)", BigDecimal.ZERO, true,
                    new BigDecimal("9999999999"), 6));
            e.setTamanoM(Validador.decimal(tamano.getText(), "Tamaño (m)", BigDecimal.ZERO, true,
                    new BigDecimal("999999"), 4));
            e.setCaracteristicas(Validador.texto(caracteristicas.getText(), "Características", 10, 1000));
            e.setRutaImagen(imagen.resolverRuta());
            return e;
        });
        return f;
    }

    @Override
    protected void guardarNuevo(Especie especie) throws DatosException {
        dao.insertar(especie);
    }

    @Override
    protected void guardarCambios(Especie especie) throws DatosException {
        dao.actualizar(especie);
    }

    @Override
    protected void borrar(Especie especie) throws DatosException {
        dao.eliminar(especie.getId());
    }

    @Override
    protected String describir(Especie especie) {
        return "la especie «" + especie.getNombreComun() + "»";
    }

    @Override
    protected int idDe(Especie especie) {
        return especie.getId();
    }
}
