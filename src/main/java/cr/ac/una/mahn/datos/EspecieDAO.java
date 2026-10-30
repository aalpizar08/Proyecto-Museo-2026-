package cr.ac.una.mahn.datos;

import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.modelo.Especie;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/** Acceso a la tabla especies. */
public class EspecieDAO extends BaseDAO {

    private static final String SELECT = """
            SELECT e.id_especie, e.id_coleccion, c.nombre AS coleccion, c.siglo, c.id_sala, s.nombre AS sala,
                   e.nombre_cientifico, e.nombre_comun, e.fecha_extincion, e.epoca, e.peso_kg, e.tamano_m,
                   e.caracteristicas, e.ruta_imagen
            FROM especies e
            JOIN colecciones c ON c.id_coleccion = e.id_coleccion
            JOIN salas s ON s.id_sala = c.id_sala""";

    /** Filtra por nombre común, nombre científico o época y, opcionalmente, por colección. */
    public List<Especie> buscar(String texto, Integer idColeccion) throws DatosException {
        Consulta consulta = new Consulta(SELECT);
        if (!texto.isBlank()) {
            String p = patron(texto);
            consulta.y("(e.nombre_comun LIKE ? OR e.nombre_cientifico LIKE ? OR e.epoca LIKE ?)", p, p, p);
        }
        if (idColeccion != null) {
            consulta.y("e.id_coleccion = ?", idColeccion);
        }
        consulta.ordenar("e.nombre_comun");
        return consultar(consulta.sql(), this::mapear, consulta.parametros());
    }

    /** Especies de todas las colecciones de una sala (para la ficha de la sala). */
    public List<Especie> listarPorSala(int idSala) throws DatosException {
        return consultar(SELECT + " WHERE c.id_sala = ? ORDER BY c.nombre, e.nombre_comun", this::mapear, idSala);
    }

    public void insertar(Especie especie) throws DatosException {
        int id = insertarRegistro("""
                INSERT INTO especies (id_coleccion, nombre_cientifico, nombre_comun, fecha_extincion, epoca,
                                      peso_kg, tamano_m, caracteristicas, ruta_imagen)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                especie.getIdColeccion(), especie.getNombreCientifico(), especie.getNombreComun(),
                especie.getFechaExtincion(), especie.getEpoca(), especie.getPesoKg(), especie.getTamanoM(),
                especie.getCaracteristicas(), especie.getRutaImagen());
        especie.setId(id);
    }

    public void actualizar(Especie especie) throws DatosException {
        int filas = ejecutar("""
                UPDATE especies
                SET id_coleccion = ?, nombre_cientifico = ?, nombre_comun = ?, fecha_extincion = ?, epoca = ?,
                    peso_kg = ?, tamano_m = ?, caracteristicas = ?, ruta_imagen = ?
                WHERE id_especie = ?""",
                especie.getIdColeccion(), especie.getNombreCientifico(), especie.getNombreComun(),
                especie.getFechaExtincion(), especie.getEpoca(), especie.getPesoKg(), especie.getTamanoM(),
                especie.getCaracteristicas(), especie.getRutaImagen(), especie.getId());
        verificarEncontrado(filas, "la especie");
    }

    public void eliminar(int idEspecie) throws DatosException {
        verificarEncontrado(ejecutar("DELETE FROM especies WHERE id_especie = ?", idEspecie), "la especie");
    }

    private Especie mapear(ResultSet rs) throws SQLException {
        Especie especie = new Especie();
        especie.setId(rs.getInt("id_especie"));
        especie.setIdColeccion(rs.getInt("id_coleccion"));
        especie.setColeccion(rs.getString("coleccion"));
        especie.setSigloColeccion(rs.getInt("siglo"));
        especie.setIdSala(rs.getInt("id_sala"));
        especie.setSala(rs.getString("sala"));
        especie.setNombreCientifico(rs.getString("nombre_cientifico"));
        especie.setNombreComun(rs.getString("nombre_comun"));
        especie.setFechaExtincion(rs.getString("fecha_extincion"));
        especie.setEpoca(rs.getString("epoca"));
        especie.setPesoKg(rs.getBigDecimal("peso_kg"));
        especie.setTamanoM(rs.getBigDecimal("tamano_m"));
        especie.setCaracteristicas(rs.getString("caracteristicas"));
        especie.setRutaImagen(rs.getString("ruta_imagen"));
        return especie;
    }
}
