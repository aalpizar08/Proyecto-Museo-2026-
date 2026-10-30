package cr.ac.una.mahn.datos;

import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.modelo.Precio;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/** Acceso a la tabla precios. */
public class PrecioDAO extends BaseDAO {

    private static final String SELECT = """
            SELECT p.id_precio, p.id_sala, s.nombre AS sala, s.id_museo, m.nombre AS museo,
                   p.precio_semana, p.precio_domingo
            FROM precios p
            JOIN salas s ON s.id_sala = p.id_sala
            JOIN museos m ON m.id_museo = s.id_museo""";

    /** Filtra por nombre de sala y, opcionalmente, por museo. */
    public List<Precio> buscar(String texto, Integer idMuseo) throws DatosException {
        Consulta consulta = new Consulta(SELECT);
        if (!texto.isBlank()) {
            consulta.y("s.nombre LIKE ?", patron(texto));
        }
        if (idMuseo != null) {
            consulta.y("s.id_museo = ?", idMuseo);
        }
        consulta.ordenar("m.nombre, s.nombre");
        return consultar(consulta.sql(), this::mapear, consulta.parametros());
    }

    /** Salas de un museo que tienen precio, para la venta de entradas. */
    public List<Precio> listarPorMuseo(int idMuseo) throws DatosException {
        return buscar("", idMuseo);
    }

    public void insertar(Precio precio) throws DatosException {
        int id = insertarRegistro("INSERT INTO precios (id_sala, precio_semana, precio_domingo) VALUES (?, ?, ?)",
                precio.getIdSala(), precio.getPrecioSemana(), precio.getPrecioDomingo());
        precio.setId(id);
    }

    public void actualizar(Precio precio) throws DatosException {
        int filas = ejecutar("UPDATE precios SET precio_semana = ?, precio_domingo = ? WHERE id_precio = ?",
                precio.getPrecioSemana(), precio.getPrecioDomingo(), precio.getId());
        verificarEncontrado(filas, "el precio");
    }

    public void eliminar(int idPrecio) throws DatosException {
        verificarEncontrado(ejecutar("DELETE FROM precios WHERE id_precio = ?", idPrecio), "el precio");
    }

    private Precio mapear(ResultSet rs) throws SQLException {
        Precio precio = new Precio();
        precio.setId(rs.getInt("id_precio"));
        precio.setIdSala(rs.getInt("id_sala"));
        precio.setSala(rs.getString("sala"));
        precio.setIdMuseo(rs.getInt("id_museo"));
        precio.setMuseo(rs.getString("museo"));
        precio.setPrecioSemana(rs.getBigDecimal("precio_semana"));
        precio.setPrecioDomingo(rs.getBigDecimal("precio_domingo"));
        return precio;
    }
}
