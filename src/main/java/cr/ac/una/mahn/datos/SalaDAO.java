package cr.ac.una.mahn.datos;

import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.modelo.Sala;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/** Acceso a la tabla salas. */
public class SalaDAO extends BaseDAO {

    private static final String SELECT = """
            SELECT s.id_sala, s.id_museo, m.nombre AS museo, s.id_area, a.nombre AS area,
                   s.nombre, s.descripcion
            FROM salas s
            JOIN museos m ON m.id_museo = s.id_museo
            JOIN areas a ON a.id_area = s.id_area""";

    public List<Sala> listar() throws DatosException {
        return buscar("", null);
    }

    /** Filtra por nombre, descripción o área y, opcionalmente, por museo. */
    public List<Sala> buscar(String texto, Integer idMuseo) throws DatosException {
        Consulta consulta = new Consulta(SELECT);
        if (!texto.isBlank()) {
            String p = patron(texto);
            consulta.y("(s.nombre LIKE ? OR s.descripcion LIKE ? OR a.nombre LIKE ?)", p, p, p);
        }
        if (idMuseo != null) {
            consulta.y("s.id_museo = ?", idMuseo);
        }
        consulta.ordenar("m.nombre, s.nombre");
        return consultar(consulta.sql(), this::mapear, consulta.parametros());
    }

    public Sala buscarPorId(int idSala) throws DatosException {
        return consultarUno(SELECT + " WHERE s.id_sala = ?", this::mapear, idSala);
    }

    /** Salas que todavía no tienen precio registrado (para el mantenimiento de precios). */
    public List<Sala> listarSinPrecio() throws DatosException {
        return consultar(SELECT + " WHERE NOT EXISTS (SELECT 1 FROM precios p WHERE p.id_sala = s.id_sala)"
                + " ORDER BY m.nombre, s.nombre", this::mapear);
    }

    public void insertar(Sala sala) throws DatosException {
        int id = insertarRegistro("INSERT INTO salas (id_museo, id_area, nombre, descripcion) VALUES (?, ?, ?, ?)",
                sala.getIdMuseo(), sala.getIdArea(), sala.getNombre(), sala.getDescripcion());
        sala.setId(id);
    }

    public void actualizar(Sala sala) throws DatosException {
        int filas = ejecutar("UPDATE salas SET id_museo = ?, id_area = ?, nombre = ?, descripcion = ? WHERE id_sala = ?",
                sala.getIdMuseo(), sala.getIdArea(), sala.getNombre(), sala.getDescripcion(), sala.getId());
        verificarEncontrado(filas, "la sala");
    }

    public void eliminar(int idSala) throws DatosException {
        verificarEncontrado(ejecutar("DELETE FROM salas WHERE id_sala = ?", idSala), "la sala");
    }

    private Sala mapear(ResultSet rs) throws SQLException {
        Sala sala = new Sala();
        sala.setId(rs.getInt("id_sala"));
        sala.setIdMuseo(rs.getInt("id_museo"));
        sala.setMuseo(rs.getString("museo"));
        sala.setIdArea(rs.getInt("id_area"));
        sala.setArea(rs.getString("area"));
        sala.setNombre(rs.getString("nombre"));
        sala.setDescripcion(rs.getString("descripcion"));
        return sala;
    }
}
