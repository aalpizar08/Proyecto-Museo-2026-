package cr.ac.una.mahn.modelo;

import cr.ac.una.mahn.util.Formatos;
import cr.ac.una.mahn.util.NumerosRomanos;

/**
 * Elemento que se muestra en el carrusel de la ficha de una sala: una especie
 * o una temática, con los datos ya preparados para la pantalla.
 */
public record ElementoExhibicion(String tipo, String nombre, String subtitulo, String coleccion,
                                 String epoca, String detalle, String datos, String rutaImagen) {

    public static final String ESPECIE = "Especie";
    public static final String TEMATICA = "Temática";

    public static ElementoExhibicion de(Especie e) {
        String coleccion = e.getColeccion() + " · siglo " + NumerosRomanos.siglo(e.getSigloColeccion());
        String datos = "Extinción: " + (e.isExtinta() ? e.getFechaExtincion() : "no está extinta")
                + "   ·   Peso: " + Formatos.peso(e.getPesoKg())
                + "   ·   Tamaño: " + Formatos.tamano(e.getTamanoM());
        return new ElementoExhibicion(ESPECIE, e.getNombreComun(), e.getNombreCientifico(), coleccion,
                e.getEpoca(), e.getCaracteristicas(), datos, e.getRutaImagen());
    }

    public static ElementoExhibicion de(Tematica t) {
        return new ElementoExhibicion(TEMATICA, t.getNombre(), t.getSala(), "No aplica (sala temática)",
                t.getEpoca(), t.getCaracteristicas(), "", t.getRutaImagen());
    }

    public boolean esEspecie() {
        return ESPECIE.equals(tipo);
    }
}
