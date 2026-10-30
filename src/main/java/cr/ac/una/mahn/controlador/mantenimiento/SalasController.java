package cr.ac.una.mahn.controlador.mantenimiento;

import cr.ac.una.mahn.App;
import cr.ac.una.mahn.datos.CatalogoDAO;
import cr.ac.una.mahn.datos.MuseoDAO;
import cr.ac.una.mahn.datos.SalaDAO;
import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.excepcion.ValidacionException;
import cr.ac.una.mahn.modelo.Area;
import cr.ac.una.mahn.modelo.Museo;
import cr.ac.una.mahn.modelo.Opcion;
import cr.ac.una.mahn.modelo.Sala;
import cr.ac.una.mahn.servicio.ComprobanteServicio;
import cr.ac.una.mahn.util.Mensajes;
import cr.ac.una.mahn.util.Tablas;
import cr.ac.una.mahn.util.Validador;
import cr.ac.una.mahn.vista.DialogoArchivo;
import cr.ac.una.mahn.vista.Formulario;
import java.nio.file.Path;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/** Mantenimiento de salas. Además permite generar el código QR de cada sala. */
public class SalasController extends MantenimientoController<Sala> {

    private final SalaDAO dao = new SalaDAO();
    private final MuseoDAO museoDAO = new MuseoDAO();
    private final CatalogoDAO catalogo = new CatalogoDAO();
    private final ComprobanteServicio comprobantes = new ComprobanteServicio();

    @Override
    protected String titulo() {
        return "Salas";
    }

    @Override
    protected String etiquetaFiltroTexto() {
        return "Nombre, descripción o área:";
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
    protected List<TableColumn<Sala, ?>> columnas() {
        return List.of(
                Tablas.entero("Código", Sala::getId, 60),
                Tablas.texto("Sala", Sala::getNombre, 270),
                Tablas.texto("Museo", Sala::getMuseo, 220),
                Tablas.texto("Área", Sala::getArea, 150),
                Tablas.texto("Descripción", Sala::getDescripcion, 380));
    }

    @Override
    protected List<Sala> consultar(String texto, Integer idFiltro) throws DatosException {
        return dao.buscar(texto, idFiltro);
    }

    @Override
    protected void configurarAcciones(HBox panel) {
        Button qr = new Button("Generar QR de la sala");
        qr.getStyleClass().add("boton-dorado");
        qr.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        qr.setOnAction(e -> generarQr(tabla.getSelectionModel().getSelectedItem()));
        Button todas = new Button("QR de las salas listadas");
        todas.getStyleClass().add("boton-secundario");
        todas.setOnAction(e -> generarQrListadas());
        panel.getChildren().addAll(qr, todas);
    }

    private void generarQr(Sala sala) {
        Mensajes.intentar(() -> {
            Path imagen = comprobantes.generarQRSala(sala);
            DialogoArchivo.mostrar("Código QR de la sala", sala.getNombre(),
                    "Imprima este código y colóquelo en la entrada de la sala. Al leerlo en el módulo de "
                            + "Valoración se muestra la ficha de la sala.", imagen, null);
        });
    }

    private void generarQrListadas() {
        Mensajes.intentar(() -> {
            if (tabla.getItems().isEmpty()) {
                throw new ValidacionException("No hay salas en la lista para generar sus códigos QR.");
            }
            for (Sala sala : tabla.getItems()) {
                comprobantes.generarQRSala(sala);
            }
            Path carpeta = ComprobanteServicio.CARPETA_SALAS.toAbsolutePath();
            Mensajes.informacion("Códigos QR generados", "Se generaron " + tabla.getItems().size()
                    + " códigos QR en la carpeta:\n" + carpeta);
            App.abrirArchivo(carpeta);
        });
    }

    @Override
    protected Formulario<Sala> crearFormulario(Sala sala) throws DatosException {
        boolean nueva = sala == null;
        Formulario<Sala> f = new Formulario<>(nueva ? "Insertar sala" : "Editar sala",
                nueva ? "Nueva sala" : "Editar «" + sala.getNombre() + "»");
        ComboBox<Museo> museo = f.campo("Museo:", Formulario.combo(museoDAO.listar(), Museo::getNombre));
        ComboBox<Area> area = f.campo("Área:", Formulario.combo(catalogo.areas(), Area::getNombre));
        TextField nombre = f.campo("Nombre de la sala:", Formulario.texto(nueva ? "" : sala.getNombre(),
                "Ej.: Salón de Dinosaurios Saurisquios"));
        TextArea descripcion = f.campo("Descripción:", Formulario.areaTexto(nueva ? "" : sala.getDescripcion(),
                "¿Qué se exhibe en la sala?", 4));
        if (!nueva) {
            Formulario.seleccionar(museo, Museo::getId, sala.getIdMuseo());
            Formulario.seleccionar(area, Area::getId, sala.getIdArea());
        }
        f.alConstruir(() -> {
            Sala s = new Sala();
            s.setId(nueva ? 0 : sala.getId());
            Museo m = Validador.seleccion(museo.getValue(), "Museo");
            s.setIdMuseo(m.getId());
            s.setMuseo(m.getNombre());
            Area a = Validador.seleccion(area.getValue(), "Área");
            s.setIdArea(a.getId());
            s.setArea(a.getNombre());
            s.setNombre(Validador.texto(nombre.getText(), "Nombre de la sala", 3, 120));
            s.setDescripcion(Validador.texto(descripcion.getText(), "Descripción", 10, 600));
            return s;
        });
        return f;
    }

    @Override
    protected void guardarNuevo(Sala sala) throws DatosException {
        dao.insertar(sala);
    }

    @Override
    protected void guardarCambios(Sala sala) throws DatosException {
        dao.actualizar(sala);
    }

    @Override
    protected void borrar(Sala sala) throws DatosException {
        dao.eliminar(sala.getId());
    }

    @Override
    protected String describir(Sala sala) {
        return "la sala «" + sala.getNombre() + "»";
    }

    @Override
    protected int idDe(Sala sala) {
        return sala.getId();
    }
}
