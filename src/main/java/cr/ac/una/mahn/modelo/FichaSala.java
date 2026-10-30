package cr.ac.una.mahn.modelo;

import java.util.List;

/** Información completa que se muestra al leer el código QR de una sala. */
public record FichaSala(Sala sala, List<ElementoExhibicion> elementos, ResumenValoracion resumen,
                        List<Valoracion> comentariosRecientes) {

    public boolean tieneEspecies() {
        return elementos.stream().anyMatch(ElementoExhibicion::esEspecie);
    }
}
