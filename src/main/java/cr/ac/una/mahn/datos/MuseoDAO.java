package cr.ac.una.mahn.datos;

import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.modelo.Museo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Acceso a la tabla museos. */
public class MuseoDAO extends BaseDAO {

    private static final String SELECT = """
            SELECT m.id_museo, m.id_tipo_museo, t.nombre AS tipo, m.nombre, m.ubicacion,
                   m.fecha_fundacion, m.director, m.sitio_web
            FROM museos m
            JOIN tipos_museo t ON t.id_tipo_museo = m.id_tipo_museo""";

    public List<Museo> listar() throws DatosException {
        return buscar("", null);
    }

    /** Filtra por nombre, director o ubicación y, opcionalmente, por tipo de museo. */
    public List<Museo> buscar(String texto, Integer idTipo) throws DatosException {
        Consulta consulta = new Consulta(SELECT);
        if (!texto.isBlank()) {
            String p = patron(texto);
            consulta.y("(m.nombre LIKE ? OR m.director LIKE ? OR m.ubicacion LIKE ?)", p, p, p);
        }
        if (idTipo != null) {
            consulta.y("m.id_tipo_museo = ?", idTipo);
        }
        consulta.ordenar("m.nombre");
        return consultar(consulta.sql(), this::mapear, consulta.parametros());
    }

    public void insertar(Museo museo) throws DatosException {
        int id = insertarRegistro("""
                INSERT INTO museos (id_tipo_museo, nombre, ubicacion, fecha_fundacion, director, sitio_web)
                VALUES (?, ?, ?, ?, ?, ?)""",
                museo.getIdTipo(), museo.getNombre(), museo.getUbicacion(), museo.getFechaFundacion(),
                museo.getDirector(), museo.getSitioWeb());
        museo.setId(id);
    }

    public void actualizar(Museo museo) throws DatosException {
        int filas = ejecutar("""
                UPDATE museos
                SET id_tipo_museo = ?, nombre = ?, ubicacion = ?, fecha_fundacion = ?, director = ?, sitio_web = ?
                WHERE id_museo = ?""",
                museo.getIdTipo(), museo.getNombre(), museo.getUbicacion(), museo.getFechaFundacion(),
                museo.getDirector(), museo.getSitioWeb(), museo.getId());
        verificarEncontrado(filas, "el museo");
    }

    public void eliminar(int idMuseo) throws DatosException {
        verificarEncontrado(ejecutar("DELETE FROM museos WHERE id_museo = ?", idMuseo), "el museo");
    }

    private Museo mapear(ResultSet rs) throws SQLException {
        Museo museo = new Museo();
        museo.setId(rs.getInt("id_museo"));
        museo.setIdTipo(rs.getInt("id_tipo_museo"));
        museo.setTipo(rs.getString("tipo"));
        museo.setNombre(rs.getString("nombre"));
        museo.setUbicacion(rs.getString("ubicacion"));
        museo.setFechaFundacion(rs.getObject("fecha_fundacion", LocalDate.class));
        museo.setDirector(rs.getString("director"));
        museo.setSitioWeb(rs.getString("sitio_web"));
        return museo;
    }
}
