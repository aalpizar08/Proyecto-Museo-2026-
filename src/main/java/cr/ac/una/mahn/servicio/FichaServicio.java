package cr.ac.una.mahn.servicio;

import cr.ac.una.mahn.datos.EspecieDAO;
import cr.ac.una.mahn.datos.SalaDAO;
import cr.ac.una.mahn.datos.TematicaDAO;
import cr.ac.una.mahn.datos.ValoracionDAO;
import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.excepcion.ValidacionException;
import cr.ac.una.mahn.modelo.ElementoExhibicion;
import cr.ac.una.mahn.modelo.Especie;
import cr.ac.una.mahn.modelo.FichaSala;
import cr.ac.una.mahn.modelo.Sala;
import cr.ac.una.mahn.modelo.Tematica;
import cr.ac.una.mahn.modelo.Valoracion;
import cr.ac.una.mahn.util.Validador;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Arma la ficha de una sala (especies o temáticas y su valoración) y registra valoraciones. */
public class FichaServicio {

    private static final int COMENTARIOS_RECIENTES = 4;

    private final SalaDAO salaDAO = new SalaDAO();
    private final EspecieDAO especieDAO = new EspecieDAO();
    private final TematicaDAO tematicaDAO = new TematicaDAO();
    private final ValoracionDAO valoracionDAO = new ValoracionDAO();

    public FichaSala obtener(int idSala) throws DatosException, ValidacionException {
        Sala sala = salaDAO.buscarPorId(idSala);
        if (sala == null) {
            throw new ValidacionException("No existe ninguna sala con el código " + idSala
                    + ". Verifique el código QR de la sala.");
        }
        List<ElementoExhibicion> elementos = new ArrayList<>();
        for (Especie especie : especieDAO.listarPorSala(idSala)) {
            elementos.add(ElementoExhibicion.de(especie));
        }
        for (Tematica tematica : tematicaDAO.listarPorSala(idSala)) {
            elementos.add(ElementoExhibicion.de(tematica));
        }
        return new FichaSala(sala, elementos, valoracionDAO.resumen(idSala),
                valoracionDAO.recientesConComentario(idSala, COMENTARIOS_RECIENTES));
    }

    /** Registra una valoración anónima de 1 a 5 estrellas con una observación opcional. */
    public Valoracion valorar(int idSala, double estrellas, String observacion)
            throws ValidacionException, DatosException {
        Valoracion valoracion = new Valoracion(idSala, Validador.estrellas(estrellas),
                Validador.textoOpcional(observacion, "Observación", 500), LocalDateTime.now().withNano(0));
        valoracionDAO.insertar(valoracion);
        return valoracion;
    }
}
