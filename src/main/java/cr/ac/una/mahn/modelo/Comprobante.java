package cr.ac.una.mahn.modelo;

import java.nio.file.Path;

/** Archivos generados al vender una entrada: el PDF del comprobante y la imagen JPG del QR. */
public record Comprobante(Path pdf, Path imagenQr) {
}
