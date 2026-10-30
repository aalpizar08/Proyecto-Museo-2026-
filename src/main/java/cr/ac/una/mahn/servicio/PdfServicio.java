package cr.ac.una.mahn.servicio;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import cr.ac.una.mahn.modelo.DetalleEntrada;
import cr.ac.una.mahn.modelo.Entrada;
import cr.ac.una.mahn.modelo.FilaReporteComision;
import cr.ac.una.mahn.modelo.FilaReporteValoracion;
import cr.ac.una.mahn.util.CodigoQR;
import cr.ac.una.mahn.util.DibujoEstrellas;
import cr.ac.una.mahn.util.Formatos;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.imageio.ImageIO;

/** Genera los PDF de la aplicación: el comprobante de la entrada y los dos reportes. */
public class PdfServicio {

    private static final Color AZUL = new DeviceRgb(0x14, 0x21, 0x3d);
    private static final Color DORADO = new DeviceRgb(0xe0, 0xa5, 0x26);
    private static final Color CREMA = new DeviceRgb(0xf6, 0xf2, 0xe9);
    private static final Color GRIS = new DeviceRgb(0x66, 0x66, 0x66);
    private static final Color BORDE = new DeviceRgb(0xdd, 0xd5, 0xc4);

    /** Fuentes estándar del PDF (soportan tildes y eñes). */
    private record Fuentes(PdfFont normal, PdfFont negrita, PdfFont cursiva) {
        static Fuentes crear() throws IOException {
            return new Fuentes(PdfFontFactory.createFont(StandardFonts.HELVETICA),
                    PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD),
                    PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE));
        }
    }

    // ------------------------------------------------------------------ comprobante de entrada

    public Path generarComprobante(Entrada entrada, BufferedImage qr, Path destino) throws IOException {
        Fuentes f = Fuentes.crear();
        try (Document doc = crearDocumento(destino, f)) {
            encabezado(doc, f, "Comprobante de entrada",
                    "Presente este código QR al ingresar a cada sala en la fecha indicada");

            Table superior = new Table(UnitValue.createPercentArray(new float[]{60, 40})).useAllAvailableWidth()
                    .setMarginTop(16);
            Cell datos = new Cell().setBorder(Border.NO_BORDER).setPaddingRight(16);
            datos.add(dato(f, "Código de la entrada", entrada.getCodigo()));
            datos.add(dato(f, "Visitante", entrada.getNombreVisitante()));
            datos.add(dato(f, "Fecha de compra", Formatos.fechaHora(entrada.getFechaCompra())));
            datos.add(dato(f, "Forma de pago", "Tarjeta " + entrada.getTarjeta()));
            datos.add(dato(f, "Salas incluidas", String.valueOf(entrada.getDetalles().size())));
            Cell celdaQr = new Cell().setBorder(new SolidBorder(BORDE, 1)).setPadding(10)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE);
            celdaQr.add(new Image(ImageDataFactory.create(aPng(qr))).setWidth(185)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER));
            celdaQr.add(new Paragraph(CodigoQR.contenidoEntrada(entrada.getCodigo())).setFont(f.negrita())
                    .setFontSize(8).setTextAlignment(TextAlignment.CENTER).setMargin(0));
            superior.addCell(datos);
            superior.addCell(celdaQr);
            doc.add(superior);

            doc.add(subtitulo(f, "Salas y días de visita"));
            Table tabla = new Table(UnitValue.createPercentArray(new float[]{28, 38, 18, 16})).useAllAvailableWidth();
            for (String titulo : new String[]{"Museo", "Sala", "Fecha de visita", "Precio"}) {
                tabla.addHeaderCell(celdaTitulo(f, titulo));
            }
            int fila = 0;
            for (DetalleEntrada d : entrada.getDetalles()) {
                boolean alterna = fila++ % 2 == 1;
                tabla.addCell(celda(d.getMuseo(), alterna, TextAlignment.LEFT));
                tabla.addCell(celda(d.getSala(), alterna, TextAlignment.LEFT));
                tabla.addCell(celda(Formatos.fecha(d.getFechaVisita()) + "\n" + Formatos.dia(d.getFechaVisita()),
                        alterna, TextAlignment.LEFT));
                tabla.addCell(celda(Formatos.moneda(d.getPrecio()), alterna, TextAlignment.RIGHT));
            }
            doc.add(tabla);

            Table totales = new Table(UnitValue.createPercentArray(new float[]{75, 25})).useAllAvailableWidth()
                    .setMarginTop(6);
            filaTotal(totales, f, "Subtotal", entrada.getSubtotal(), false);
            filaTotal(totales, f, "IVA (13 %)", entrada.getIva(), false);
            filaTotal(totales, f, "TOTAL PAGADO", entrada.getTotal(), true);
            doc.add(totales);

            doc.add(new Paragraph("La entrada solo es válida en las salas y fechas indicadas. En cada ingreso el código "
                    + "QR se valida contra la fecha del sistema. Conserve este comprobante.")
                    .setFont(f.cursiva()).setFontSize(8.5f).setFontColor(GRIS).setMarginTop(18));
            doc.add(pie(f));
        }
        return destino;
    }

    // ------------------------------------------------------------------ reporte de comisiones

    public Path generarReporteComisiones(LocalDate desde, LocalDate hasta, List<FilaReporteComision> filas,
                                         BufferedImage grafico, Path destino) throws IOException {
        Fuentes f = Fuentes.crear();
        try (Document doc = crearDocumento(destino, f)) {
            encabezado(doc, f, "Comisiones por tarjeta",
                    "Periodo del " + Formatos.fecha(desde) + " al " + Formatos.fecha(hasta));
            doc.add(new Paragraph("Total de comisiones que se le deben pagar a cada marca autorizada de tarjeta por "
                    + "las entradas vendidas en el periodo. La comisión se registra automáticamente en cada venta "
                    + "con el porcentaje vigente en ese momento.").setFontSize(9.5f).setMarginTop(14));

            Table tabla = new Table(UnitValue.createPercentArray(new float[]{26, 15, 17, 21, 21})).useAllAvailableWidth()
                    .setMarginTop(8);
            for (String titulo : new String[]{"Tarjeta", "% vigente", "Transacciones", "Monto procesado", "Comisión a pagar"}) {
                tabla.addHeaderCell(celdaTitulo(f, titulo));
            }
            int transacciones = 0;
            BigDecimal procesado = BigDecimal.ZERO;
            BigDecimal comision = BigDecimal.ZERO;
            int fila = 0;
            for (FilaReporteComision r : filas) {
                boolean alterna = fila++ % 2 == 1;
                tabla.addCell(celda(r.tarjeta(), alterna, TextAlignment.LEFT));
                tabla.addCell(celda(Formatos.porcentaje(r.porcentajeVigente()), alterna, TextAlignment.RIGHT));
                tabla.addCell(celda(String.valueOf(r.transacciones()), alterna, TextAlignment.RIGHT));
                tabla.addCell(celda(Formatos.moneda(r.montoProcesado()), alterna, TextAlignment.RIGHT));
                tabla.addCell(celda(Formatos.moneda(r.comision()), alterna, TextAlignment.RIGHT));
                transacciones += r.transacciones();
                procesado = procesado.add(r.montoProcesado());
                comision = comision.add(r.comision());
            }
            tabla.addCell(celdaTotal(f, "TOTAL", TextAlignment.LEFT));
            tabla.addCell(celdaTotal(f, "", TextAlignment.RIGHT));
            tabla.addCell(celdaTotal(f, String.valueOf(transacciones), TextAlignment.RIGHT));
            tabla.addCell(celdaTotal(f, Formatos.moneda(procesado), TextAlignment.RIGHT));
            tabla.addCell(celdaTotal(f, Formatos.moneda(comision), TextAlignment.RIGHT));
            doc.add(tabla);

            if (grafico != null) {
                doc.add(subtitulo(f, "Comisión a pagar por marca"));
                doc.add(new Image(ImageDataFactory.create(aPng(grafico))).setWidth(UnitValue.createPercentValue(92))
                        .setHorizontalAlignment(HorizontalAlignment.CENTER));
            }
            doc.add(pie(f));
        }
        return destino;
    }

    // ------------------------------------------------------------------ reporte de valoración

    public Path generarReporteValoraciones(String museo, boolean mejores, List<FilaReporteValoracion> filas,
                                           Path destino) throws IOException {
        Fuentes f = Fuentes.crear();
        try (Document doc = crearDocumento(destino, f)) {
            encabezado(doc, f, mejores ? "Salas mejor valoradas" : "Salas peor valoradas",
                    museo + " · " + filas.size() + (filas.size() == 1 ? " sala" : " salas"));
            doc.add(new Paragraph((mejores
                    ? "Listado de las salas con el promedio de valoración más alto. "
                    : "Listado invertido: salas con el promedio de valoración más bajo. ")
                    + "Las valoraciones son anónimas, de 1 a 5 estrellas, y se registran al leer el código QR de cada sala.")
                    .setFontSize(9.5f).setMarginTop(14));

            Table tabla = new Table(UnitValue.createPercentArray(new float[]{7, 37, 25, 10, 13, 8})).useAllAvailableWidth()
                    .setMarginTop(8);
            for (String titulo : new String[]{"#", "Sala", "Museo", "Promedio", "Estrellas", "Votos"}) {
                tabla.addHeaderCell(celdaTitulo(f, titulo));
            }
            int fila = 0;
            for (FilaReporteValoracion r : filas) {
                boolean alterna = fila++ % 2 == 1;
                tabla.addCell(celda(String.valueOf(r.posicion()), alterna, TextAlignment.CENTER));
                tabla.addCell(celda(r.sala(), alterna, TextAlignment.LEFT));
                tabla.addCell(celda(r.museo(), alterna, TextAlignment.LEFT));
                tabla.addCell(celda(Formatos.promedio(r.promedio()), alterna, TextAlignment.CENTER));
                Cell estrellas = celda("", alterna, TextAlignment.CENTER);
                estrellas.add(new Image(ImageDataFactory.create(aPng(DibujoEstrellas.imagen(r.promedio(), 40))))
                        .setWidth(62).setHorizontalAlignment(HorizontalAlignment.CENTER));
                tabla.addCell(estrellas);
                tabla.addCell(celda(String.valueOf(r.cantidad()), alterna, TextAlignment.CENTER));
            }
            doc.add(tabla);
            doc.add(pie(f));
        }
        return destino;
    }

    // ------------------------------------------------------------------ utilidades de formato

    private Document crearDocumento(Path destino, Fuentes f) throws IOException {
        if (destino.getParent() != null) {
            Files.createDirectories(destino.getParent());
        }
        Document doc = new Document(new PdfDocument(new PdfWriter(destino.toString())), PageSize.LETTER);
        doc.setMargins(36, 40, 36, 40);
        doc.setFont(f.normal()).setFontSize(10);
        return doc;
    }

    private void encabezado(Document doc, Fuentes f, String titulo, String descripcion) {
        Table banda = new Table(UnitValue.createPercentArray(new float[]{1})).useAllAvailableWidth();
        Cell celda = new Cell().setBackgroundColor(AZUL).setBorder(Border.NO_BORDER).setPadding(16);
        celda.add(new Paragraph("MUSEO AMERICANO DE HISTORIA NATURAL · MAHN 3.0").setFont(f.negrita()).setFontSize(8.5f)
                .setFontColor(DORADO).setMargin(0));
        celda.add(new Paragraph(titulo).setFont(f.negrita()).setFontSize(20).setFontColor(ColorConstants.WHITE)
                .setMarginTop(4).setMarginBottom(2));
        celda.add(new Paragraph(descripcion).setFontSize(10).setFontColor(new DeviceRgb(0xd8, 0xdc, 0xe4)).setMargin(0));
        banda.addCell(celda);
        doc.add(banda);
    }

    private Paragraph dato(Fuentes f, String etiqueta, String valor) {
        Paragraph p = new Paragraph().setMarginBottom(6);
        p.add(new com.itextpdf.layout.element.Text(etiqueta.toUpperCase() + "\n").setFont(f.negrita()).setFontSize(7.5f)
                .setFontColor(GRIS));
        p.add(new com.itextpdf.layout.element.Text(valor).setFontSize(12));
        return p;
    }

    private Paragraph subtitulo(Fuentes f, String texto) {
        return new Paragraph(texto).setFont(f.negrita()).setFontSize(12).setFontColor(AZUL).setMarginTop(18)
                .setMarginBottom(6);
    }

    private Cell celdaTitulo(Fuentes f, String texto) {
        return new Cell().add(new Paragraph(texto).setFont(f.negrita()).setFontSize(9).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(AZUL).setBorder(Border.NO_BORDER).setPadding(6);
    }

    private Cell celda(String texto, boolean alterna, TextAlignment alineacion) {
        return new Cell().add(new Paragraph(texto).setFontSize(9.5f))
                .setBackgroundColor(alterna ? CREMA : ColorConstants.WHITE)
                .setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(BORDE, 0.5f))
                .setPadding(5).setTextAlignment(alineacion).setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    private Cell celdaTotal(Fuentes f, String texto, TextAlignment alineacion) {
        return new Cell().add(new Paragraph(texto).setFont(f.negrita()).setFontSize(10))
                .setBackgroundColor(new DeviceRgb(0xfc, 0xef, 0xcf)).setBorder(Border.NO_BORDER)
                .setBorderTop(new SolidBorder(AZUL, 1)).setPadding(6).setTextAlignment(alineacion);
    }

    private void filaTotal(Table tabla, Fuentes f, String etiqueta, BigDecimal monto, boolean destacado) {
        Paragraph textoEtiqueta = new Paragraph(etiqueta).setFontSize(destacado ? 12 : 10);
        Paragraph textoMonto = new Paragraph(Formatos.moneda(monto)).setFontSize(destacado ? 14 : 10);
        if (destacado) {
            textoEtiqueta.setFont(f.negrita()).setFontColor(AZUL);
            textoMonto.setFont(f.negrita()).setFontColor(AZUL);
        }
        tabla.addCell(new Cell().add(textoEtiqueta).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT)
                .setPadding(3));
        tabla.addCell(new Cell().add(textoMonto).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT)
                .setPadding(3));
    }

    private Paragraph pie(Fuentes f) {
        return new Paragraph("Generado por MAHN 3.0 el " + Formatos.fechaHora(LocalDateTime.now()))
                .setFont(f.cursiva()).setFontSize(8).setFontColor(GRIS).setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(20);
    }

    private static byte[] aPng(BufferedImage imagen) throws IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(imagen, "png", salida);
        return salida.toByteArray();
    }
}
