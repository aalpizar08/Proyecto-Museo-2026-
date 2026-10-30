package cr.ac.una.mahn.servicio;

import cr.ac.una.mahn.datos.EntradaDAO;
import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.excepcion.ValidacionException;
import cr.ac.una.mahn.modelo.DetalleEntrada;
import cr.ac.una.mahn.modelo.Entrada;
import cr.ac.una.mahn.modelo.Precio;
import cr.ac.una.mahn.modelo.Tarjeta;
import cr.ac.una.mahn.modelo.Totales;
import cr.ac.una.mahn.util.CodigoQR;
import cr.ac.una.mahn.util.Formatos;
import cr.ac.una.mahn.util.Validador;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Reglas de negocio de la venta de entradas: precio según el día, IVA del 13 %,
 * validaciones y registro de la venta.
 */
public class VentaServicio {

    public static final BigDecimal TASA_IVA = new BigDecimal("0.13");
    public static final int DIAS_MAXIMOS_ANTICIPACION = 365;

    private final EntradaDAO entradaDAO;

    public VentaServicio() {
        this(new EntradaDAO());
    }

    VentaServicio(EntradaDAO entradaDAO) {
        this.entradaDAO = entradaDAO;
    }

    /** Subtotal (suma de precios), IVA del 13 % redondeado a dos decimales y total. */
    public static Totales calcularTotales(Collection<DetalleEntrada> detalles) {
        BigDecimal subtotal = detalles.stream()
                .map(DetalleEntrada::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal iva = subtotal.multiply(TASA_IVA).setScale(2, RoundingMode.HALF_UP);
        return new Totales(subtotal, iva, subtotal.add(iva));
    }

    /** La visita debe ser hoy o en el futuro, con un máximo de un año de anticipación. */
    public void validarFechaVisita(LocalDate fecha) throws ValidacionException {
        if (fecha == null) {
            throw new ValidacionException("Seleccione en el calendario el día de la visita.");
        }
        LocalDate hoy = LocalDate.now();
        if (fecha.isBefore(hoy)) {
            throw new ValidacionException("La fecha de visita " + Formatos.fecha(fecha)
                    + " ya pasó; seleccione hoy o un día posterior.");
        }
        if (fecha.isAfter(hoy.plusDays(DIAS_MAXIMOS_ANTICIPACION))) {
            throw new ValidacionException("Solo se venden entradas con un máximo de un año de anticipación.");
        }
    }

    /** Indica si la sala ya está en la entrada para ese mismo día. */
    public static boolean estaAgregada(Collection<DetalleEntrada> detalles, int idSala, LocalDate fecha) {
        return detalles.stream().anyMatch(d -> d.getIdSala() == idSala && d.getFechaVisita().equals(fecha));
    }

    /** Crea la línea de la entrada con el precio de lunes a sábado o el de domingo. */
    public DetalleEntrada crearDetalle(Precio precio, LocalDate fecha) throws ValidacionException {
        validarFechaVisita(fecha);
        return new DetalleEntrada(precio.getIdSala(), precio.getSala(), precio.getMuseo(), fecha, precio.precioPara(fecha));
    }

    /** Valida los datos de la venta y devuelve el nombre del visitante limpio. */
    public String validarVenta(String visitante, Tarjeta tarjeta, Collection<DetalleEntrada> detalles)
            throws ValidacionException {
        String nombre = Validador.nombrePersona(visitante, "Nombre del visitante");
        Validador.seleccion(tarjeta, "Tipo de tarjeta");
        if (detalles.isEmpty()) {
            throw new ValidacionException("Agregue al menos una sala y un día de visita antes de vender la entrada.");
        }
        for (DetalleEntrada detalle : detalles) {
            // Por si la pantalla quedó abierta de un día para otro
            validarFechaVisita(detalle.getFechaVisita());
        }
        return nombre;
    }

    /** Registra la venta en la base de datos y devuelve la entrada con su código único. */
    public Entrada vender(String visitante, Tarjeta tarjeta, Collection<DetalleEntrada> detalles)
            throws ValidacionException, DatosException {
        String nombre = validarVenta(visitante, tarjeta, detalles);
        Totales totales = calcularTotales(detalles);
        Entrada entrada = new Entrada();
        entrada.setCodigo(generarCodigoUnico());
        entrada.setNombreVisitante(nombre);
        entrada.setIdTarjeta(tarjeta.getId());
        entrada.setTarjeta(tarjeta.getNombre());
        entrada.setFechaCompra(LocalDateTime.now().withNano(0));
        entrada.setSubtotal(totales.subtotal());
        entrada.setIva(totales.iva());
        entrada.setTotal(totales.total());
        entrada.setDetalles(new ArrayList<>(detalles));
        entradaDAO.registrar(entrada);
        return entrada;
    }

    private String generarCodigoUnico() throws DatosException {
        String codigo;
        do {
            codigo = CodigoQR.generarCodigoEntrada();
        } while (entradaDAO.existeCodigo(codigo));
        return codigo;
    }
}
