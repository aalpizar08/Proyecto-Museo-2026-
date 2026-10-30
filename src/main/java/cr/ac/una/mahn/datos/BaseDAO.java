package cr.ac.una.mahn.datos;

import cr.ac.una.mahn.excepcion.DatosException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Operaciones JDBC comunes de los DAO. Todas usan sentencias preparadas y
 * convierten las SQLException en {@link DatosException} con mensajes en español.
 */
abstract class BaseDAO {

    /** Convierte la fila actual de un ResultSet en un objeto. */
    @FunctionalInterface
    protected interface Mapeador<T> {
        T mapear(ResultSet rs) throws SQLException;
    }

    protected <T> List<T> consultar(String sql, Mapeador<T> mapeador, Object... parametros) throws DatosException {
        try (Connection conexion = ConexionBD.obtener();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            asignarParametros(sentencia, parametros);
            try (ResultSet rs = sentencia.executeQuery()) {
                List<T> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapeador.mapear(rs));
                }
                return lista;
            }
        } catch (SQLException ex) {
            throw DatosException.desde(ex);
        }
    }

    protected <T> T consultarUno(String sql, Mapeador<T> mapeador, Object... parametros) throws DatosException {
        List<T> lista = consultar(sql, mapeador, parametros);
        return lista.isEmpty() ? null : lista.get(0);
    }

    /** Ejecuta un UPDATE o DELETE y devuelve la cantidad de filas encontradas. */
    protected int ejecutar(String sql, Object... parametros) throws DatosException {
        try (Connection conexion = ConexionBD.obtener();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            asignarParametros(sentencia, parametros);
            return sentencia.executeUpdate();
        } catch (SQLException ex) {
            throw DatosException.desde(ex);
        }
    }

    /** Ejecuta un INSERT y devuelve la llave autoincremental generada. */
    protected int insertarRegistro(String sql, Object... parametros) throws DatosException {
        try (Connection conexion = ConexionBD.obtener();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarParametros(sentencia, parametros);
            sentencia.executeUpdate();
            try (ResultSet llaves = sentencia.getGeneratedKeys()) {
                return llaves.next() ? llaves.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw DatosException.desde(ex);
        }
    }

    /** Lanza un error si un UPDATE o DELETE no encontró el registro (por ejemplo, si otro usuario lo borró). */
    protected static void verificarEncontrado(int filas, String registro) throws DatosException {
        if (filas == 0) {
            throw new DatosException("No se encontró " + registro + "; es posible que haya sido eliminado. "
                    + "Actualice la lista e intente de nuevo.", null);
        }
    }

    static void asignarParametros(PreparedStatement sentencia, Object... parametros) throws SQLException {
        for (int i = 0; i < parametros.length; i++) {
            sentencia.setObject(i + 1, parametros[i]);
        }
    }

    /** Patrón para LIKE que busca el texto en cualquier posición, escapando los comodines. */
    protected static String patron(String texto) {
        String escapado = texto.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escapado + "%";
    }

    /** Arma consultas con filtros opcionales sin concatenar valores del usuario. */
    protected static final class Consulta {

        private final StringBuilder sql;
        private final List<Object> parametros = new ArrayList<>();

        Consulta(String base) {
            sql = new StringBuilder(base).append(" WHERE 1 = 1");
        }

        Consulta y(String condicion, Object... valores) {
            sql.append(" AND ").append(condicion);
            parametros.addAll(Arrays.asList(valores));
            return this;
        }

        Consulta ordenar(String orden) {
            sql.append(" ORDER BY ").append(orden);
            return this;
        }

        Consulta limitar(int cantidad) {
            sql.append(" LIMIT ?");
            parametros.add(cantidad);
            return this;
        }

        String sql() {
            return sql.toString();
        }

        Object[] parametros() {
            return parametros.toArray();
        }
    }
}
