package cr.ac.una.mahn.modelo;

import cr.ac.una.mahn.util.NumerosRomanos;

/** Colección que se exhibe en una sala. */
public class Coleccion {

    private int id;
    private int idSala;
    private String sala;
    private String museo;
    private String nombre;
    /** Siglo de la colección: positivo d. C., negativo a. C. */
    private int siglo;
    private String descripcion;

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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getSiglo() {
        return siglo;
    }

    public void setSiglo(int siglo) {
        this.siglo = siglo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /** Siglo en números romanos, por ejemplo «XIX d. C.». */
    public String getSigloTexto() {
        return NumerosRomanos.siglo(siglo);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
