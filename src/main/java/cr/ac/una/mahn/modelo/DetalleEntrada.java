package cr.ac.una.mahn.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Sala y día de visita incluidos en una entrada, con el precio cobrado. */
public class DetalleEntrada {

    private int id;
    private int idSala;
    private String sala;
    private String museo;
    private LocalDate fechaVisita;
    private BigDecimal precio;

    public DetalleEntrada() {
    }

    public DetalleEntrada(int idSala, String sala, String museo, LocalDate fechaVisita, BigDecimal precio) {
        this.idSala = idSala;
        this.sala = sala;
        this.museo = museo;
        this.fechaVisita = fechaVisita;
        this.precio = precio;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdSala() {
        return idSala;
    }

    public void setIdSala(int idSala) {
        this.idSala = idSala;
    }

    public String getSala() {
        return sala;
    }

    public void setSala(String sala) {
        this.sala = sala;
    }

    public String getMuseo() {
        return museo;
    }

    public void setMuseo(String museo) {
        this.museo = museo;
    }

    public LocalDate getFechaVisita() {
        return fechaVisita;
    }

    public void setFechaVisita(LocalDate fechaVisita) {
        this.fechaVisita = fechaVisita;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }
}
