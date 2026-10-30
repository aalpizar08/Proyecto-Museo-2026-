package cr.ac.una.mahn.excepcion;

import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Error de acceso a la base de datos. Traduce los códigos de error de MySQL a
 * mensajes en español que el usuario pueda entender.
 */
public class DatosException extends Exception {

    private static final Pattern TABLA_HIJA = Pattern.compile("`[^`]+`\\.`([^`]+)`");
    private static final Pattern RESTRICCION = Pattern.compile("'(?:[^.']+\\.)?([a-z_]+)'");

    /** Nombre amigable de las tablas que pueden impedir una eliminación. */
    private static final Map<String, String> TABLAS = Map.ofEntries(
            Map.entry("museos", "museos"),
            Map.entry("salas", "salas"),
            Map.entry("colecciones", "colecciones"),
            Map.entry("especies", "especies"),
            Map.entry("tematicas", "temáticas"),
            Map.entry("precios", "precios"),
            Map.entry("entradas", "entradas vendidas"),
            Map.entry("detalles_entrada", "entradas vendidas"),
            Map.entry("comisiones_cobradas", "comisiones registradas"),
            Map.entry("valoraciones", "valoraciones"));

    /** Mensajes para las restricciones UNIQUE y CHECK definidas en el script. */
    private static final Map<String, String> RESTRICCIONES = Map.ofEntries(
            Map.entry("uq_tipos_museo_nombre", "Ya existe un tipo de museo con ese nombre."),
            Map.entry("uq_museos_nombre", "Ya existe un museo con ese nombre."),
            Map.entry("uq_salas_museo_nombre", "Ese museo ya tiene una sala con ese nombre."),
            Map.entry("uq_colecciones_sala_nombre", "Esa sala ya tiene una colección con ese nombre."),
            Map.entry("uq_especies_coleccion_nombre", "Esa especie ya está registrada en la colección seleccionada."),
            Map.entry("uq_tematicas_sala_nombre", "Esa sala ya tiene una temática con ese nombre."),
            Map.entry("uq_precios_sala", "La sala seleccionada ya tiene un precio registrado; edítelo en lugar de insertar uno nuevo."),
            Map.entry("uq_tarjetas_nombre", "Ya existe una tarjeta con ese nombre."),
            Map.entry("uq_entradas_codigo", "El código de la entrada ya existe; intente realizar la venta de nuevo."),
            Map.entry("uq_detalles_entrada_sala_fecha", "La entrada tiene una sala repetida para el mismo día."),
            Map.entry("ck_museos_sitio_web", "El sitio web debe iniciar con http:// o https://."),
            Map.entry("ck_colecciones_siglo", "El siglo debe estar entre el C a. C. y el XXII d. C. y no puede ser cero."),
            Map.entry("ck_especies_peso", "El peso de la especie debe ser mayor que cero."),
            Map.entry("ck_especies_tamano", "El tamaño de la especie debe ser mayor que cero."),
            Map.entry("ck_precios_semana", "El precio de lunes a sábado no puede ser negativo."),
            Map.entry("ck_precios_domingo", "El precio del domingo no puede ser negativo."),
            Map.entry("ck_tarjetas_porcentaje", "El porcentaje de comisión debe estar entre 0 y 100."),
            Map.entry("ck_entradas_montos", "Los montos de la entrada no son consistentes (total = subtotal + IVA)."),
            Map.entry("ck_detalles_entrada_precio", "El precio de una sala no puede ser negativo."),
            Map.entry("ck_valoraciones_estrellas", "La valoración debe estar entre 1 y 5 estrellas."));

    public DatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    /** Convierte una SQLException en un mensaje comprensible. */
    public static DatosException desde(SQLException ex) {
        return new DatosException(traducir(ex), ex);
    }

    static String traducir(SQLException ex) {
        String detalle = ex.getMessage() == null ? "" : ex.getMessage();
        String estado = ex.getSQLState() == null ? "" : ex.getSQLState();
        switch (ex.getErrorCode()) {
            case 1062:
            case 3819:
                return mensajeRestriccion(detalle);
            case 1451:
                return "No se puede eliminar el registro porque tiene " + tablaRelacionada(detalle)
                        + " asociadas. Elimine o cambie primero esa información.";
            case 1452:
                return "El registro relacionado que seleccionó ya no existe. Actualice la lista e intente de nuevo.";
            case 1406:
                return "Uno de los campos supera la longitud máxima permitida.";
            case 1048:
                return "Falta un dato obligatorio.";
            case 1045:
                return "El usuario o la contraseña de MySQL son incorrectos.";
            case 1049:
                return "La base de datos no existe. Ejecute primero el script database/MAHN_DB.sql en MySQL.";
            case 1146:
                return "Falta una tabla en la base de datos. Vuelva a ejecutar el script database/MAHN_DB.sql.";
            default:
                break;
        }
        if (estado.startsWith("08")) {
            return "No se pudo conectar con el servidor MySQL. Verifique que el servicio esté iniciado "
                    + "y que los datos de conexión sean correctos.";
        }
        return "Error de base de datos: " + detalle;
    }

    private static String mensajeRestriccion(String detalle) {
        Matcher m = RESTRICCION.matcher(detalle);
        while (m.find()) {
            String mensaje = RESTRICCIONES.get(m.group(1));
            if (mensaje != null) {
                return mensaje;
            }
        }
        return "Los datos no cumplen una regla de la base de datos: " + detalle;
    }

    private static String tablaRelacionada(String detalle) {
        Matcher m = TABLA_HIJA.matcher(detalle);
        if (m.find()) {
            return TABLAS.getOrDefault(m.group(1), m.group(1));
        }
        return "otros registros";
    }
}
