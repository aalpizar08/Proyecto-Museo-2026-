package cr.ac.una.mahn.datos;

import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.modelo.Tarjeta;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/** Acceso a la tabla tarjetas (tipos de tarjeta y su comisión). */
public class TarjetaDAO extends BaseDAO {

    private static final String SELECT = "SELECT id_tarjeta, nombre, porcentaje_comision, activa FROM tarjetas t";

    public List<Tarjeta> listar() throws DatosException {
        return buscar("", null);
    }

    /** Tarjetas que se aceptan actualmente en las cajas. */
    public List<Tarjeta> listarActivas() throws DatosException {
        return consultar(SELECT + " WHERE t.activa = TRUE ORDER BY t.nombre", this::mapear);
    }

    public List<Tarjeta> buscar(String texto, Integer idTarjeta) throws DatosException {
        Consulta consulta = new Consulta(SELECT);
        if (!texto.isBlank()) {
            consulta.y("t.nombre LIKE ?", patron(texto));
        }
        if (idTarjeta != null) {
            consulta.y("t.id_tarjeta = ?", idTarjeta);
        }
        consulta.ordenar("t.nombre");
        return consultar(consulta.sql(), this::mapear, consulta.parametros());
    }

    public void insertar(Tarjeta tarjeta) throws DatosException {
        int id = insertarRegistro("INSERT INTO tarjetas (nombre, porcentaje_comision, activa) VALUES (?, ?, ?)",
                tarjeta.getNombre(), tarjeta.getPorcentajeComision(), tarjeta.isActiva());
        tarjeta.setId(id);
    }

    public void actualizar(Tarjeta tarjeta) throws DatosException {
        int filas = ejecutar("UPDATE tarjetas SET nombre = ?, porcentaje_comision = ?, activa = ? WHERE id_tarjeta = ?",
                tarjeta.getNombre(), tarjeta.getPorcentajeComision(), tarjeta.isActiva(), tarjeta.getId());
        verificarEncontrado(filas, "la tarjeta");
    }

    public void eliminar(int idTarjeta) throws DatosException {
        verificarEncontrado(ejecutar("DELETE FROM tarjetas WHERE id_tarjeta = ?", idTarjeta), "la tarjeta");
    }

    private Tarjeta mapear(ResultSet rs) throws SQLException {
        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setId(rs.getInt("id_tarjeta"));
        tarjeta.setNombre(rs.getString("nombre"));
        tarjeta.setPorcentajeComision(rs.getBigDecimal("porcentaje_comision"));
        tarjeta.setActiva(rs.getBoolean("activa"));
        return tarjeta;
    }
}
