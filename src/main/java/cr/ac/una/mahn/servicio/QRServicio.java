package cr.ac.una.mahn.servicio;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Binarizer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.itextpdf.commons.exceptions.ITextException;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfResources;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import cr.ac.una.mahn.excepcion.ValidacionException;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/** Generación de códigos QR (imagen JPG) y lectura desde imágenes o archivos PDF. */
public class QRServicio {

    private static final int TAMANO_QR = 480;

    /** Genera la matriz del QR como imagen en blanco y negro del tamaño indicado. */
    public BufferedImage generar(String contenido, int tamano) {
        Map<EncodeHintType, Object> pistas = new EnumMap<>(EncodeHintType.class);
        pistas.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        pistas.put(EncodeHintType.MARGIN, 2);
        pistas.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        try {
            BitMatrix matriz = new QRCodeWriter().encode(contenido, BarcodeFormat.QR_CODE, tamano, tamano, pistas);
            BufferedImage imagen = new BufferedImage(tamano, tamano, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < tamano; x++) {
                for (int y = 0; y < tamano; y++) {
                    imagen.setRGB(x, y, matriz.get(x, y) ? 0x000000 : 0xFFFFFF);
                }
            }
            return imagen;
        } catch (WriterException ex) {
            throw new IllegalStateException("No se pudo generar el código QR de «" + contenido + "»", ex);
        }
    }

    /** QR con un título y un subtítulo debajo, listo para guardarse como JPG o imprimirse. */
    public BufferedImage generarConLeyenda(String contenido, String titulo, String subtitulo) {
        BufferedImage qr = generar(contenido, TAMANO_QR);
        int margen = 24;
        int ancho = TAMANO_QR + margen * 2;
        int alto = TAMANO_QR + margen + 120;
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imagen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, ancho, alto);
        g.drawImage(qr, margen, margen, null);
        g.setColor(new Color(0x14213d));
        g.fillRect(0, alto - 8, ancho, 8);
        escribirCentrado(g, titulo, new Font("SansSerif", Font.BOLD, 24), ancho, TAMANO_QR + margen + 40);
        g.setColor(new Color(0x555555));
        escribirCentrado(g, subtitulo, new Font("SansSerif", Font.PLAIN, 17), ancho, TAMANO_QR + margen + 72);
        g.setColor(new Color(0xa07414));
        escribirCentrado(g, "MAHN 3.0 · Museo Americano de Historia Natural",
                new Font("SansSerif", Font.PLAIN, 13), ancho, TAMANO_QR + margen + 100);
        g.dispose();
        return imagen;
    }

    /** Escribe el texto centrado, reduciendo la letra si no cabe en el ancho. */
    private static void escribirCentrado(Graphics2D g, String texto, Font fuente, int ancho, int y) {
        if (texto == null || texto.isBlank()) {
            return;
        }
        Font actual = fuente;
        FontMetrics medidas = g.getFontMetrics(actual);
        while (medidas.stringWidth(texto) > ancho - 30 && actual.getSize() > 9) {
            actual = actual.deriveFont(actual.getSize2D() - 1);
            medidas = g.getFontMetrics(actual);
        }
        g.setFont(actual);
        g.drawString(texto, (ancho - medidas.stringWidth(texto)) / 2, y);
    }

    /** Guarda la imagen como JPG de alta calidad para que el QR se lea sin problemas. */
    public void guardarJPG(BufferedImage imagen, Path destino) throws IOException {
        if (destino.getParent() != null) {
            Files.createDirectories(destino.getParent());
        }
        ImageWriter escritor = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam parametros = escritor.getDefaultWriteParam();
        parametros.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        parametros.setCompressionQuality(0.95f);
        Files.deleteIfExists(destino);
        try (ImageOutputStream salida = ImageIO.createImageOutputStream(destino.toFile())) {
            escritor.setOutput(salida);
            escritor.write(null, new IIOImage(imagen, null, null), parametros);
        } finally {
            escritor.dispose();
        }
    }

    /** Lee el contenido de un código QR desde una imagen (JPG, PNG, BMP, GIF) o un PDF. */
    public String leer(File archivo) throws ValidacionException, IOException {
        if (archivo.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            return leerDePdf(archivo);
        }
        BufferedImage imagen = ImageIO.read(archivo);
        if (imagen == null) {
            throw new ValidacionException("El archivo «" + archivo.getName()
                    + "» no es una imagen válida. Use un archivo JPG, PNG o PDF.");
        }
        return leer(imagen);
    }

    public String leer(BufferedImage imagen) throws ValidacionException {
        String texto = decodificar(imagen);
        if (texto == null) {
            throw new ValidacionException("No se encontró un código QR legible en la imagen.");
        }
        return texto;
    }

    private String decodificar(BufferedImage imagen) {
        Map<DecodeHintType, Object> pistas = new EnumMap<>(DecodeHintType.class);
        pistas.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        pistas.put(DecodeHintType.POSSIBLE_FORMATS, List.of(BarcodeFormat.QR_CODE));
        LuminanceSource fuente = new BufferedImageLuminanceSource(imagen);
        for (Binarizer binarizador : List.of(new HybridBinarizer(fuente), new GlobalHistogramBinarizer(fuente))) {
            try {
                return new QRCodeReader().decode(new BinaryBitmap(binarizador), pistas).getText();
            } catch (NotFoundException | ChecksumException | FormatException ex) {
                // Se intenta con el siguiente método de binarización
            }
        }
        return null;
    }

    /** Busca las imágenes incrustadas en el PDF (como el comprobante de la entrada) y lee el QR. */
    private String leerDePdf(File archivo) throws ValidacionException, IOException {
        try (PdfDocument pdf = new PdfDocument(new PdfReader(archivo))) {
            for (int pagina = 1; pagina <= pdf.getNumberOfPages(); pagina++) {
                PdfResources recursos = pdf.getPage(pagina).getResources();
                PdfDictionary objetos = recursos.getResource(PdfName.XObject);
                if (objetos == null) {
                    continue;
                }
                for (PdfName nombre : objetos.keySet()) {
                    PdfStream flujo = objetos.getAsStream(nombre);
                    if (flujo != null && PdfName.Image.equals(flujo.getAsName(PdfName.Subtype))) {
                        BufferedImage imagen = new PdfImageXObject(flujo).getBufferedImage();
                        String texto = imagen == null ? null : decodificar(imagen);
                        if (texto != null) {
                            return texto;
                        }
                    }
                }
            }
        } catch (ITextException ex) {
            throw new ValidacionException("No se pudo leer el PDF «" + archivo.getName() + "»: " + ex.getMessage());
        }
        throw new ValidacionException("El PDF «" + archivo.getName() + "» no contiene un código QR legible.");
    }
}
