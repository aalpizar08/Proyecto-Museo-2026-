package cr.ac.una.mahn.datos;

import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.modelo.Coleccion;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/** Acceso a la tabla colecciones. */
public class ColeccionDAO extends BaseDAO {

    private static final String SELECT = """
            SELECT c.id_coleccion, c.id_sala, s.nombre AS sala, m.nombre AS museo,
                   c.nombre, c.siglo, c.descripcion
            FROM colecciones c
            JOIN salas s ON s.id_sala = c.id_sala
            JOIN museos m ON m.id_museo = s.id_museo""";

    public List<Coleccion> listar() throws DatosException {
        return buscar("", null);
    }

    /** Filtra por nombre o descripción y, opcionalmente, por sala. */
    public List<Coleccion> buscar(String texto, Integer idSala) throws DatosException {
        Consulta consulta = new Consulta(SELECT);
        if (!texto.isBlank()) {
            String p = patron(texto);
            consulta.y("(c.nombre LIKE ? OR c.descripcion LIKE ?)", p, p);
        }
        if (idSala != null) {
            consulta.y("c.id_sala = ?", idSala);
        }
        consulta.ordenar("c.nombre");
        return consultar(consulta.sql(), this::mapear, consulta.parametros());
    }

    public void insertar(Coleccion coleccion) throws DatosException {
        int id = insertarRegistro("INSERT INTO colecciones (id_sala, nombre, siglo, descripcion) VALUES (?, ?, ?, ?)",
                coleccion.getIdSala(), coleccion.getNombre(), coleccion.getSiglo(), coleccion.getDescripcion());
        coleccion.setId(id);
    }

    public void actualizar(Coleccion coleccion) throws DatosException {
        int filas = ejecutar("UPDATE colecciones SET id_sala = ?, nombre = ?, siglo = ?, descripcion = ? WHERE id_coleccion = ?",
                coleccion.getIdSala(), coleccion.getNombre(), coleccion.getSiglo(), coleccion.getDescripcion(),
                coleccion.getId());
        verificarEncontrado(filas, "la colección");
    }

    public void eliminar(int idColeccion) throws DatosException {
        verificarEncontrado(ejecutar("DELETE FROM colecciones WHERE id_coleccion = ?", idColeccion), "la colección");
    }

    private Coleccion mapear(ResultSet rs) throws SQLException {
        Coleccion coleccion = new Coleccion();
        coleccion.setId(rs.getInt("id_coleccion"));
        coleccion.setIdSala(rs.getInt("id_sala"));
        coleccion.setSala(rs.getString("sala"));
        coleccion.setMuseo(rs.getString("museo"));
        coleccion.setNombre(rs.getString("nombre"));
        coleccion.setSiglo(rs.getInt("siglo"));
        coleccion.setDescripcion(rs.getString("descripcion"));
        return coleccion;
    }
}
