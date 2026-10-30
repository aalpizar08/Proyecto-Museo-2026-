package cr.ac.una.mahn.modelo;

import java.time.LocalDateTime;

/** Valoración anónima de una sala (1 a 5 estrellas y una observación opcional). */
public class Valoracion {

    private int id;
    private int idSala;
    private int estrellas;
    private String observacion;
    private LocalDateTime fecha;

    public Valoracion() {
    }

    public Valoracion(int idSala, int estrellas, String observacion, LocalDateTime fecha) {
        this.idSala = idSala;
        this.estrellas = estrellas;
        this.observacion = observacion;
        this.fecha = fecha;
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

    public int getEstrellas() {
        return estrellas;
    }

    public void setEstrellas(int estrellas) {
        this.estrellas = estrellas;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
