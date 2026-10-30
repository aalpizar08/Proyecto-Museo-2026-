package cr.ac.una.mahn.datos;

import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.modelo.DetalleEntrada;
import cr.ac.una.mahn.modelo.Entrada;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Acceso a las tablas entradas y detalles_entrada. */
public class EntradaDAO extends BaseDAO {

    private static final String SELECT = """
            SELECT e.id_entrada, e.codigo, e.nombre_visitante, e.id_tarjeta, t.nombre AS tarjeta,
                   e.fecha_compra, e.subtotal, e.iva, e.total,
                   (SELECT COUNT(*) FROM detalles_entrada d WHERE d.id_entrada = e.id_entrada) AS cantidad_salas,
                   (SELECT MIN(d.fecha_visita) FROM detalles_entrada d WHERE d.id_entrada = e.id_entrada) AS primera_visita,
                   (SELECT MAX(d.fecha_visita) FROM detalles_entrada d WHERE d.id_entrada = e.id_entrada) AS ultima_visita
            FROM entradas e
            JOIN tarjetas t ON t.id_tarjeta = e.id_tarjeta""";

    private static final String SELECT_DETALLES = """
            SELECT d.id_detalle, d.id_sala, s.nombre AS sala, m.nombre AS museo, d.fecha_visita, d.precio
            FROM detalles_entrada d
            JOIN salas s ON s.id_sala = d.id_sala
            JOIN museos m ON m.id_museo = s.id_museo
            WHERE d.id_entrada = ?
            ORDER BY d.fecha_visita, m.nombre, s.nombre""";

    /**
     * Guarda la entrada y sus detalles en una sola transacción. La comisión de la
     * tarjeta la registra automáticamente el disparador tg_entradas_ai.
     */
    public void registrar(Entrada entrada) throws DatosException {
        String sqlEntrada = """
                INSERT INTO entradas (codigo, nombre_visitante, id_tarjeta, fecha_compra, subtotal, iva, total)
                VALUES (?, ?, ?, ?, ?, ?, ?)""";
        String sqlDetalle = "INSERT INTO detalles_entrada (id_entrada, id_sala, fecha_visita, precio) VALUES (?, ?, ?, ?)";
        try (Connection conexion = ConexionBD.obtener()) {
            conexion.setAutoCommit(false);
            try (PreparedStatement sentencia = conexion.prepareStatement(sqlEntrada, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement sentenciaDetalle = conexion.prepareStatement(sqlDetalle)) {
                asignarParametros(sentencia, entrada.getCodigo(), entrada.getNombreVisitante(), entrada.getIdTarjeta(),
                        entrada.getFechaCompra(), entrada.getSubtotal(), entrada.getIva(), entrada.getTotal());
                sentencia.executeUpdate();
                try (ResultSet llaves = sentencia.getGeneratedKeys()) {
                    llaves.next();
                    entrada.setId(llaves.getInt(1));
                }
                for (DetalleEntrada detalle : entrada.getDetalles()) {
                    asignarParametros(sentenciaDetalle, entrada.getId(), detalle.getIdSala(), detalle.getFechaVisita(),
                            detalle.getPrecio());
                    sentenciaDetalle.addBatch();
                }
                sentenciaDetalle.executeBatch();
                conexion.commit();
            } catch (SQLException ex) {
                conexion.rollback();
                entrada.setId(0);
                throw ex;
            }
        } catch (SQLException ex) {
            throw DatosException.desde(ex);
        }
    }

    public boolean existeCodigo(String codigo) throws DatosException {
        return consultarUno("SELECT 1 FROM entradas WHERE codigo = ?", rs -> Boolean.TRUE, codigo) != null;
    }

    /** Busca una entrada por el código del QR, con todas sus salas y fechas. */
    public Entrada buscarPorCodigo(String codigo) throws DatosException {
        Entrada entrada = consultarUno(SELECT + " WHERE e.codigo = ?", this::mapear, codigo);
        if (entrada != null) {
            entrada.setDetalles(consultar(SELECT_DETALLES, this::mapearDetalle, entrada.getId()));
        }
        return entrada;
    }

    /** Historial de entradas filtrado por visitante o código y, opcionalmente, por tarjeta. */
    public List<Entrada> buscar(String texto, Integer idTarjeta) throws DatosException {
        Consulta consulta = new Consulta(SELECT);
        if (!texto.isBlank()) {
            String p = patron(texto);
            consulta.y("(e.nombre_visitante LIKE ? OR e.codigo LIKE ?)", p, p);
        }
        if (idTarjeta != null) {
            consulta.y("e.id_tarjeta = ?", idTarjeta);
        }
        consulta.ordenar("e.fecha_compra DESC");
        return consultar(consulta.sql(), this::mapear, consulta.parametros());
    }

    private Entrada mapear(ResultSet rs) throws SQLException {
        Entrada entrada = new Entrada();
        entrada.setId(rs.getInt("id_entrada"));
        entrada.setCodigo(rs.getString("codigo"));
        entrada.setNombreVisitante(rs.getString("nombre_visitante"));
        entrada.setIdTarjeta(rs.getInt("id_tarjeta"));
        entrada.setTarjeta(rs.getString("tarjeta"));
        entrada.setFechaCompra(rs.getObject("fecha_compra", LocalDateTime.class));
        entrada.setSubtotal(rs.getBigDecimal("subtotal"));
        entrada.setIva(rs.getBigDecimal("iva"));
        entrada.setTotal(rs.getBigDecimal("total"));
        entrada.setCantidadSalas(rs.getInt("cantidad_salas"));
        entrada.setPrimeraVisita(rs.getObject("primera_visita", LocalDate.class));
        entrada.setUltimaVisita(rs.getObject("ultima_visita", LocalDate.class));
        return entrada;
    }

    private DetalleEntrada mapearDetalle(ResultSet rs) throws SQLException {
        DetalleEntrada detalle = new DetalleEntrada(rs.getInt("id_sala"), rs.getString("sala"), rs.getString("museo"),
                rs.getObject("fecha_visita", LocalDate.class), rs.getBigDecimal("precio"));
        detalle.setId(rs.getInt("id_detalle"));
        return detalle;
    }
}
