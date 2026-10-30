package cr.ac.una.mahn.datos;

import cr.ac.una.mahn.excepcion.DatosException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Punto único para obtener conexiones JDBC a la base de datos mahn_db. */
public final class ConexionBD {

    private static ConfiguracionBD configuracion = ConfiguracionBD.cargar();

    private ConexionBD() {
    }

    /** Abre una conexión nueva; quien la usa debe cerrarla (try-with-resources). */
    public static Connection obtener() throws DatosException {
        return abrir(configuracion);
    }

    /**
     * Verifica que el servidor responda y que el script de la base de datos se haya ejecutado.
     */
    public static void probar(ConfiguracionBD prueba) throws DatosException {
        try (Connection conexion = abrir(prueba);
             Statement sentencia = conexion.createStatement();
             ResultSet resultado = sentencia.executeQuery("SELECT COUNT(*) FROM museos")) {
            resultado.next();
        } catch (SQLException ex) {
            throw DatosException.desde(ex);
        }
    }

    private static Connection abrir(ConfiguracionBD datos) throws DatosException {
        try {
            DriverManager.setLoginTimeout(5);
            return DriverManager.getConnection(datos.url(), datos.getUsuario(), datos.getClave());
        } catch (SQLException ex) {
            throw DatosException.desde(ex);
        }
    }

    public static ConfiguracionBD getConfiguracion() {
        return configuracion;
    }

    public static void setConfiguracion(ConfiguracionBD nueva) {
        configuracion = nueva;
    }
}
