package cr.ac.una.mahn.datos;

import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.modelo.ResumenValoracion;
import cr.ac.una.mahn.modelo.Valoracion;
import java.time.LocalDateTime;
import java.util.List;

/** Acceso a la tabla valoraciones. */
public class ValoracionDAO extends BaseDAO {

    public void insertar(Valoracion valoracion) throws DatosException {
        int id = insertarRegistro("INSERT INTO valoraciones (id_sala, estrellas, observacion, fecha) VALUES (?, ?, ?, ?)",
                valoracion.getIdSala(), valoracion.getEstrellas(), valoracion.getObservacion(), valoracion.getFecha());
        valoracion.setId(id);
    }

    /** Promedio de estrellas y cantidad de valoraciones de la sala. */
    public ResumenValoracion resumen(int idSala) throws DatosException {
        return consultarUno("SELECT COALESCE(AVG(estrellas), 0) AS promedio, COUNT(*) AS cantidad "
                        + "FROM valoraciones WHERE id_sala = ?",
                rs -> new ResumenValoracion(rs.getDouble("promedio"), rs.getInt("cantidad")), idSala);
    }

    /** Últimas valoraciones con comentario, para mostrarlas en la ficha de la sala. */
    public List<Valoracion> recientesConComentario(int idSala, int cantidad) throws DatosException {
        return consultar("""
                SELECT id_valoracion, id_sala, estrellas, observacion, fecha
                FROM valoraciones
                WHERE id_sala = ? AND observacion IS NOT NULL
                ORDER BY fecha DESC
                LIMIT ?""", rs -> {
            Valoracion v = new Valoracion(rs.getInt("id_sala"), rs.getInt("estrellas"), rs.getString("observacion"),
                    rs.getObject("fecha", LocalDateTime.class));
            v.setId(rs.getInt("id_valoracion"));
            return v;
        }, idSala, cantidad);
    }
}
