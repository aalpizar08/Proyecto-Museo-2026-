package cr.ac.una.mahn.datos;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Datos de conexión a MySQL. Se leen de {@code config/db.properties} (o del archivo
 * indicado con {@code -Dmahn.config=ruta}) y se pueden guardar desde la ventana de conexión.
 */
public class ConfiguracionBD {

    public static final Path ARCHIVO_POR_DEFECTO = Path.of("config", "db.properties");

    private String host = "localhost";
    private int puerto = 3306;
    private String baseDatos = "mahn_db";
    private String usuario = "root";
    private String clave = "";

    /** Lee la configuración del archivo; si no existe se usan los valores por defecto. */
    public static ConfiguracionBD cargar() {
        ConfiguracionBD configuracion = new ConfiguracionBD();
        Path archivo = archivo();
        if (Files.exists(archivo)) {
            Properties propiedades = new Properties();
            try (Reader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
                propiedades.load(lector);
                configuracion.host = propiedades.getProperty("db.host", configuracion.host).trim();
                configuracion.puerto = Integer.parseInt(propiedades.getProperty("db.puerto", "3306").trim());
                configuracion.baseDatos = propiedades.getProperty("db.nombre", configuracion.baseDatos).trim();
                configuracion.usuario = propiedades.getProperty("db.usuario", configuracion.usuario).trim();
                configuracion.clave = propiedades.getProperty("db.clave", "");
            } catch (IOException | NumberFormatException ex) {
                System.err.println("No se pudo leer " + archivo + ": " + ex.getMessage());
            }
        }
        return configuracion;
    }

    /** Guarda la configuración para no tener que escribirla en cada inicio. */
    public void guardar() throws IOException {
        Path archivo = archivo();
        if (archivo.getParent() != null) {
            Files.createDirectories(archivo.getParent());
        }
        Properties propiedades = new Properties();
        propiedades.setProperty("db.host", host);
        propiedades.setProperty("db.puerto", String.valueOf(puerto));
        propiedades.setProperty("db.nombre", baseDatos);
        propiedades.setProperty("db.usuario", usuario);
        propiedades.setProperty("db.clave", clave);
        try (Writer escritor = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
            propiedades.store(escritor, "Conexion a MySQL de MAHN 3.0 (no subir este archivo al repositorio)");
        }
    }

    private static Path archivo() {
        String ruta = System.getProperty("mahn.config");
        return ruta == null || ruta.isBlank() ? ARCHIVO_POR_DEFECTO : Path.of(ruta);
    }

    public String url() {
        return "jdbc:mysql://" + host + ":" + puerto + "/" + baseDatos
                + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&connectTimeout=5000";
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPuerto() {
        return puerto;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }

    public String getBaseDatos() {
        return baseDatos;
    }

    public void setBaseDatos(String baseDatos) {
        this.baseDatos = baseDatos;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }
}
