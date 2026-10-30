package cr.ac.una.mahn.modelo;

/** Tipo de museo del catálogo: Arte, Historia, Musical o Militar. */
public class TipoMuseo {

    private final int id;
    private final String nombre;

    public TipoMuseo(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
