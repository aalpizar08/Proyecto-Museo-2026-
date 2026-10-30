package cr.ac.una.mahn.servicio;

import cr.ac.una.mahn.modelo.Comprobante;
import cr.ac.una.mahn.modelo.Entrada;
import cr.ac.una.mahn.modelo.Sala;
import cr.ac.una.mahn.util.CodigoQR;
import cr.ac.una.mahn.util.Formatos;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Genera los archivos de una entrada vendida (imagen JPG del QR y PDF del comprobante)
 * y las imágenes QR que se colocan en la entrada de cada sala.
 */
public class ComprobanteServicio {

    public static final Path CARPETA_ENTRADAS = Path.of("generados", "entradas");
    public static final Path CARPETA_SALAS = Path.of("generados", "qr_salas");

    private final QRServicio qr = new QRServicio();
    private final PdfServicio pdf = new PdfServicio();

    public Comprobante generar(Entrada entrada) throws IOException {
        String contenido = CodigoQR.contenidoEntrada(entrada.getCodigo());
        Path rutaQr = CARPETA_ENTRADAS.resolve("entrada_" + entrada.getCodigo() + "_QR.jpg");
        qr.guardarJPG(qr.generarConLeyenda(contenido, "Entrada " + entrada.getCodigo(), entrada.getNombreVisitante()),
                rutaQr);
        Path rutaPdf = pdf.generarComprobante(entrada, qr.generar(contenido, 400),
                CARPETA_ENTRADAS.resolve("entrada_" + entrada.getCodigo() + ".pdf"));
        return new Comprobante(rutaPdf.toAbsolutePath(), rutaQr.toAbsolutePath());
    }

    /** Imagen JPG con el QR que identifica a la sala en el módulo de valoración. */
    public Path generarQRSala(Sala sala) throws IOException {
        Path ruta = CARPETA_SALAS.resolve("sala_" + sala.getId() + "_" + Formatos.slug(sala.getNombre()) + ".jpg");
        qr.guardarJPG(qr.generarConLeyenda(CodigoQR.contenidoSala(sala.getId()), sala.getNombre(), sala.getMuseo()), ruta);
        return ruta.toAbsolutePath();
    }
}
