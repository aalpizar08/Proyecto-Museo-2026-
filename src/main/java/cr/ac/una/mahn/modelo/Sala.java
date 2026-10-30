package cr.ac.una.mahn.modelo;

/** Sala de exhibición de un museo. */
public class Sala {

    private int id;
    private int idMuseo;
    private String museo;
    private int idArea;
    private String area;
    private String nombre;
    private String descripcion;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getIdArea() {
        return idArea;
    }

    public void setIdArea(int idArea) {
        this.idArea = idArea;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /** Nombre de la sala acompañado de su museo, útil en listas con salas de varios museos. */
    public String getNombreConMuseo() {
        return nombre + " (" + museo + ")";
    }

    @Override
    public String toString() {
        return nombre;
    }
}
