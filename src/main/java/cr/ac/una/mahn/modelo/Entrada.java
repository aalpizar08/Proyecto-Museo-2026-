package cr.ac.una.mahn.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Entrada vendida: encabezado de la venta y las salas/días que autoriza. */
public class Entrada {

    private int id;
    private String codigo;
    private String nombreVisitante;
    private int idTarjeta;
    private String tarjeta;
    private LocalDateTime fechaCompra;
    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal total;
    private List<DetalleEntrada> detalles = new ArrayList<>();

    // Datos de resumen que se usan en el historial de entradas
    private int cantidadSalas;
    private LocalDate primeraVisita;
    private LocalDate ultimaVisita;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombreVisitante() {
        return nombreVisitante;
    }

    public void setNombreVisitante(String nombreVisitante) {
        this.nombreVisitante = nombreVisitante;
    }

    public int getIdTarjeta() {
        return idTarjeta;
    }

    public void setIdTarjeta(int idTarjeta) {
        this.idTarjeta = idTarjeta;
    }

    public String getTarjeta() {
        return tarjeta;
    }

    public void setTarjeta(String tarjeta) {
        this.tarjeta = tarjeta;
    }

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDateTime fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getIva() {
        return iva;
    }

    public void setIva(BigDecimal iva) {
        this.iva = iva;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<DetalleEntrada> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleEntrada> detalles) {
        this.detalles = detalles;
    }

    public int getCantidadSalas() {
        return detalles.isEmpty() ? cantidadSalas : detalles.size();
    }

    public void setCantidadSalas(int cantidadSalas) {
        this.cantidadSalas = cantidadSalas;
    }

    public LocalDate getPrimeraVisita() {
        return primeraVisita;
    }

    public void setPrimeraVisita(LocalDate primeraVisita) {
        this.primeraVisita = primeraVisita;
    }

    public LocalDate getUltimaVisita() {
        return ultimaVisita;
    }

    public void setUltimaVisita(LocalDate ultimaVisita) {
        this.ultimaVisita = ultimaVisita;
    }
}
