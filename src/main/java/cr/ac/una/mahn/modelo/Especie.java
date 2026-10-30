package cr.ac.una.mahn.modelo;

import java.math.BigDecimal;

/** Especie en exhibición dentro de una colección. */
public class Especie {

    private int id;
    private int idColeccion;
    private String coleccion;
    private int sigloColeccion;
    private int idSala;
    private String sala;
    private String nombreCientifico;
    private String nombreComun;
    private String fechaExtincion;
    private String epoca;
    private BigDecimal pesoKg;
    private BigDecimal tamanoM;
    private String caracteristicas;
    private String rutaImagen;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdColeccion() {
        return idColeccion;
    }

    public void setIdColeccion(int idColeccion) {
        this.idColeccion = idColeccion;
    }

    public String getColeccion() {
        return coleccion;
    }

    public void setColeccion(String coleccion) {
        this.coleccion = coleccion;
    }

    public int getSigloColeccion() {
        return sigloColeccion;
    }

    public void setSigloColeccion(int sigloColeccion) {
        this.sigloColeccion = sigloColeccion;
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

    public String getNombreCientifico() {
        return nombreCientifico;
    }

    public void setNombreCientifico(String nombreCientifico) {
        this.nombreCientifico = nombreCientifico;
    }

    public String getNombreComun() {
        return nombreComun;
    }

    public void setNombreComun(String nombreComun) {
        this.nombreComun = nombreComun;
    }

    public String getFechaExtincion() {
        return fechaExtincion;
    }

    public void setFechaExtincion(String fechaExtincion) {
        this.fechaExtincion = fechaExtincion;
    }

    public String getEpoca() {
        return epoca;
    }

    public void setEpoca(String epoca) {
        this.epoca = epoca;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(BigDecimal pesoKg) {
        this.pesoKg = pesoKg;
    }

    public BigDecimal getTamanoM() {
        return tamanoM;
    }

    public void setTamanoM(BigDecimal tamanoM) {
        this.tamanoM = tamanoM;
    }

    public String getCaracteristicas() {
        return caracteristicas;
    }

    public void setCaracteristicas(String caracteristicas) {
        this.caracteristicas = caracteristicas;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public void setRutaImagen(String rutaImagen) {
        this.rutaImagen = rutaImagen;
    }

    public boolean isExtinta() {
        return fechaExtincion != null && !fechaExtincion.isBlank();
    }

    @Override
    public String toString() {
        return nombreComun + " (" + nombreCientifico + ")";
    }
}
