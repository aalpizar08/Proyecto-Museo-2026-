package cr.ac.una.mahn.modelo;

/**
 * Opción de un ComboBox de filtro. El id {@code null} representa «todos».
 */
public record Opcion(Integer id, String texto) {

    public static final Opcion TODOS = new Opcion(null, "(Todos)");

    @Override
    public String toString() {
        return texto;
    }
}
