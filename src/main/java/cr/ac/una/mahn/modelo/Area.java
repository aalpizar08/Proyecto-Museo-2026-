package cr.ac.una.mahn.modelo;

/** Área de investigación o temática a la que pertenece una sala. */
public class Area {

    private final int id;
    private final String nombre;
    private final String descripcion;

    public Area(int id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
