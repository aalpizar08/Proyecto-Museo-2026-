package cr.ac.una.mahn.datos;

import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.modelo.Tematica;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/** Acceso a la tabla tematicas. */
public class TematicaDAO extends BaseDAO {

    private static final String SELECT = """
            SELECT t.id_tematica, t.id_sala, s.nombre AS sala, m.nombre AS museo,
                   t.nombre, t.caracteristicas, t.epoca, t.ruta_imagen
            FROM tematicas t
            JOIN salas s ON s.id_sala = t.id_sala
            JOIN museos m ON m.id_museo = s.id_museo""";

    /** Filtra por nombre, época o características y, opcionalmente, por sala. */
    public List<Tematica> buscar(String texto, Integer idSala) throws DatosException {
        Consulta consulta = new Consulta(SELECT);
        if (!texto.isBlank()) {
            String p = patron(texto);
            consulta.y("(t.nombre LIKE ? OR t.epoca LIKE ? OR t.caracteristicas LIKE ?)", p, p, p);
        }
        if (idSala != null) {
            consulta.y("t.id_sala = ?", idSala);
        }
        consulta.ordenar("s.nombre, t.nombre");
        return consultar(consulta.sql(), this::mapear, consulta.parametros());
    }

    public List<Tematica> listarPorSala(int idSala) throws DatosException {
        return consultar(SELECT + " WHERE t.id_sala = ? ORDER BY t.id_tematica", this::mapear, idSala);
    }

    public void insertar(Tematica tematica) throws DatosException {
        int id = insertarRegistro("""
                INSERT INTO tematicas (id_sala, nombre, caracteristicas, epoca, ruta_imagen)
                VALUES (?, ?, ?, ?, ?)""",
                tematica.getIdSala(), tematica.getNombre(), tematica.getCaracteristicas(), tematica.getEpoca(),
                tematica.getRutaImagen());
        tematica.setId(id);
    }

    public void actualizar(Tematica tematica) throws DatosException {
        int filas = ejecutar("""
                UPDATE tematicas
                SET id_sala = ?, nombre = ?, caracteristicas = ?, epoca = ?, ruta_imagen = ?
                WHERE id_tematica = ?""",
                tematica.getIdSala(), tematica.getNombre(), tematica.getCaracteristicas(), tematica.getEpoca(),
                tematica.getRutaImagen(), tematica.getId());
        verificarEncontrado(filas, "la temática");
    }

    public void eliminar(int idTematica) throws DatosException {
        verificarEncontrado(ejecutar("DELETE FROM tematicas WHERE id_tematica = ?", idTematica), "la temática");
    }

    private Tematica mapear(ResultSet rs) throws SQLException {
        Tematica tematica = new Tematica();
        tematica.setId(rs.getInt("id_tematica"));
        tematica.setIdSala(rs.getInt("id_sala"));
        tematica.setSala(rs.getString("sala"));
        tematica.setMuseo(rs.getString("museo"));
        tematica.setNombre(rs.getString("nombre"));
        tematica.setCaracteristicas(rs.getString("caracteristicas"));
        tematica.setEpoca(rs.getString("epoca"));
        tematica.setRutaImagen(rs.getString("ruta_imagen"));
        return tematica;
    }
}
