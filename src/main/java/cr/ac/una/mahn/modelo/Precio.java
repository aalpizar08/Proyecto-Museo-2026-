package cr.ac.una.mahn.modelo;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

/** Precio de una sala: uno de lunes a sábado y otro diferido para los domingos. */
public class Precio {

    private int id;
    private int idSala;
    private String sala;
    private int idMuseo;
    private String museo;
    private BigDecimal precioSemana;
    private BigDecimal precioDomingo;

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

    public int getIdMuseo() {
        return idMuseo;
    }

    public void setIdMuseo(int idMuseo) {
        this.idMuseo = idMuseo;
    }

    public String getMuseo() {
        return museo;
    }

    public void setMuseo(String museo) {
        this.museo = museo;
    }

    public BigDecimal getPrecioSemana() {
        return precioSemana;
    }

    public void setPrecioSemana(BigDecimal precioSemana) {
        this.precioSemana = precioSemana;
    }

    public BigDecimal getPrecioDomingo() {
        return precioDomingo;
    }

    public void setPrecioDomingo(BigDecimal precioDomingo) {
        this.precioDomingo = precioDomingo;
    }

    /** Precio que corresponde al día de la visita. */
    public BigDecimal precioPara(LocalDate fecha) {
        return fecha.getDayOfWeek() == DayOfWeek.SUNDAY ? precioDomingo : precioSemana;
    }

    @Override
    public String toString() {
        return sala;
    }
}
