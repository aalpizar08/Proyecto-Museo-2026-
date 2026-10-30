package cr.ac.una.mahn.datos;

import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.modelo.Area;
import cr.ac.una.mahn.modelo.TipoMuseo;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Consultas de los catálogos tipos_museo y areas. */
public class CatalogoDAO extends BaseDAO {

    public List<TipoMuseo> tiposMuseo() throws DatosException {
        return consultar("SELECT id_tipo_museo, nombre FROM tipos_museo ORDER BY nombre",
                rs -> new TipoMuseo(rs.getInt("id_tipo_museo"), rs.getString("nombre")));
    }

    public List<Area> areas() throws DatosException {
        return consultar("SELECT id_area, nombre, descripcion FROM areas ORDER BY nombre",
                rs -> new Area(rs.getInt("id_area"), rs.getString("nombre"), rs.getString("descripcion")));
    }

    /** Cantidad de salas de cada área, ordenadas de mayor a menor. */
    public Map<String, Integer> salasPorArea() throws DatosException {
        List<Map.Entry<String, Integer>> filas = consultar("""
                SELECT a.nombre, COUNT(s.id_sala) AS salas
                FROM areas a
                LEFT JOIN salas s ON s.id_area = a.id_area
                GROUP BY a.id_area, a.nombre
                ORDER BY salas DESC, a.nombre""",
                rs -> Map.entry(rs.getString("nombre"), rs.getInt("salas")));
        Map<String, Integer> resultado = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> fila : filas) {
            resultado.put(fila.getKey(), fila.getValue());
        }
        return resultado;
    }
}
