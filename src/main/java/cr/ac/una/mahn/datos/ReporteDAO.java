package cr.ac.una.mahn.datos;

import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.modelo.Estadisticas;
import cr.ac.una.mahn.modelo.FilaReporteComision;
import cr.ac.una.mahn.modelo.FilaReporteValoracion;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Consultas de los reportes y de los indicadores de la pantalla de inicio. */
public class ReporteDAO extends BaseDAO {

    /**
     * Total de comisiones que se le debe pagar a cada marca de tarjeta entre dos fechas
     * (ambas inclusive). Incluye las marcas sin ventas en el periodo.
     */
    public List<FilaReporteComision> comisiones(LocalDate desde, LocalDate hasta) throws DatosException {
        return consultar("""
                SELECT t.nombre, t.porcentaje_comision,
                       COUNT(c.id_comision) AS transacciones,
                       COALESCE(SUM(c.monto_transaccion), 0) AS monto_procesado,
                       COALESCE(SUM(c.monto_comision), 0) AS comision
                FROM tarjetas t
                LEFT JOIN comisiones_cobradas c
                       ON c.id_tarjeta = t.id_tarjeta
                      AND c.fecha_registro >= ? AND c.fecha_registro < ?
                GROUP BY t.id_tarjeta, t.nombre, t.porcentaje_comision
                ORDER BY comision DESC, t.nombre""",
                rs -> new FilaReporteComision(rs.getString("nombre"), rs.getBigDecimal("porcentaje_comision"),
                        rs.getInt("transacciones"), rs.getBigDecimal("monto_procesado"), rs.getBigDecimal("comision")),
                desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
    }

    /** Fecha de la primera comisión registrada (para proponer el rango del reporte). */
    public LocalDate primeraFechaComision() throws DatosException {
        LocalDateTime primera = consultarUno("SELECT MIN(fecha_registro) AS primera FROM comisiones_cobradas",
                rs -> rs.getObject("primera", LocalDateTime.class));
        return primera == null ? null : primera.toLocalDate();
    }

    /**
     * Salas mejor o peor valoradas.
     *
     * @param idMuseo museo a consultar o {@code null} para todos
     * @param mejores {@code true} para las mejor valoradas, {@code false} para las peores
     * @param limite  cantidad de salas del listado
     */
    public List<FilaReporteValoracion> valoraciones(Integer idMuseo, boolean mejores, int limite) throws DatosException {
        Consulta consulta = new Consulta("""
                SELECT id_sala, sala, museo, promedio, cantidad_valoraciones
                FROM vw_valoracion_salas""").y("cantidad_valoraciones > 0");
        if (idMuseo != null) {
            consulta.y("id_museo = ?", idMuseo);
        }
        consulta.ordenar(mejores ? "promedio DESC, cantidad_valoraciones DESC, sala"
                : "promedio ASC, cantidad_valoraciones DESC, sala").limitar(limite);
        List<FilaReporteValoracion> resultado = consultar(consulta.sql(),
                rs -> new FilaReporteValoracion(0, rs.getInt("id_sala"), rs.getString("sala"), rs.getString("museo"),
                        rs.getDouble("promedio"), rs.getInt("cantidad_valoraciones")),
                consulta.parametros());
        List<FilaReporteValoracion> filas = new ArrayList<>();
        for (int i = 0; i < resultado.size(); i++) {
            FilaReporteValoracion f = resultado.get(i);
            filas.add(new FilaReporteValoracion(i + 1, f.idSala(), f.sala(), f.museo(), f.promedio(), f.cantidad()));
        }
        return filas;
    }

    /** Indicadores generales para la pantalla de inicio. */
    public Estadisticas estadisticas(LocalDate hoy) throws DatosException {
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        return consultarUno("""
                SELECT (SELECT COUNT(*) FROM museos) AS museos,
                       (SELECT COUNT(*) FROM salas) AS salas,
                       (SELECT COUNT(*) FROM colecciones) AS colecciones,
                       (SELECT COUNT(*) FROM especies) AS especies,
                       (SELECT COUNT(*) FROM tematicas) AS tematicas,
                       (SELECT COUNT(*) FROM entradas WHERE fecha_compra >= ? AND fecha_compra < ?) AS entradas_hoy,
                       (SELECT COUNT(*) FROM detalles_entrada WHERE fecha_visita = ?) AS visitas_hoy,
                       (SELECT COALESCE(SUM(total), 0) FROM entradas WHERE fecha_compra >= ? AND fecha_compra < ?) AS ventas_mes,
                       (SELECT COALESCE(AVG(estrellas), 0) FROM valoraciones) AS promedio,
                       (SELECT COUNT(*) FROM valoraciones) AS total_valoraciones""",
                rs -> new Estadisticas(rs.getInt("museos"), rs.getInt("salas"), rs.getInt("colecciones"),
                        rs.getInt("especies"), rs.getInt("tematicas"), rs.getInt("entradas_hoy"),
                        rs.getInt("visitas_hoy"), rs.getBigDecimal("ventas_mes"), rs.getDouble("promedio"),
                        rs.getInt("total_valoraciones")),
                hoy.atStartOfDay(), hoy.plusDays(1).atStartOfDay(), hoy,
                inicioMes.atStartOfDay(), inicioMes.plusMonths(1).atStartOfDay());
    }
}
