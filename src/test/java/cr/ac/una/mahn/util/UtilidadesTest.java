package cr.ac.una.mahn.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cr.ac.una.mahn.excepcion.ValidacionException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class UtilidadesTest {

    @Test
    void convierteSiglosARomanos() {
        assertEquals("XIX d. C.", NumerosRomanos.siglo(19));
        assertEquals("I a. C.", NumerosRomanos.siglo(-1));
        assertEquals("XXI d. C.", NumerosRomanos.siglo(21));
    }

    @Test
    void interpretaCodigosQr() throws ValidacionException {
        assertEquals("TNHAYDRWQPB4", CodigoQR.extraerCodigoEntrada("MAHN-ENTRADA:TNHAYDRWQPB4"));
        assertEquals("TNHAYDRWQPB4", CodigoQR.extraerCodigoEntrada("tnhaydrwqpb4"));
        // Lector USB con teclado en inglés sobre distribución latinoamericana
        assertEquals("TNHAYDRWQPB4", CodigoQR.extraerCodigoEntrada("MAHN'ENTRADAÑTNHAYDRWQPB4"));
        assertEquals(39, CodigoQR.extraerIdSala("MAHN-SALA:39"));
        assertEquals(39, CodigoQR.extraerIdSala("39"));
        assertThrows(ValidacionException.class, () -> CodigoQR.extraerIdSala("MAHN-ENTRADA:TNHAYDRWQPB4"));
        assertThrows(ValidacionException.class, () -> CodigoQR.extraerCodigoEntrada("MAHN-SALA:39"));
        assertThrows(ValidacionException.class, () -> CodigoQR.extraerCodigoEntrada(""));
    }

    @Test
    void validaDatosDeFormularios() throws ValidacionException {
        assertEquals(new BigDecimal("12.5"), Validador.decimal("12,5", "Precio", BigDecimal.ZERO, false, new BigDecimal("100"), 2));
        assertThrows(ValidacionException.class,
                () -> Validador.decimal("-1", "Precio", BigDecimal.ZERO, false, new BigDecimal("100"), 2));
        assertThrows(ValidacionException.class,
                () -> Validador.decimal("1.234", "Precio", BigDecimal.ZERO, false, new BigDecimal("100"), 2));
        assertThrows(ValidacionException.class, () -> Validador.nombrePersona("Ana123", "Nombre"));
        assertThrows(ValidacionException.class, () -> Validador.url("www.amnh.org", "Sitio web"));
        assertEquals("Ana Mora", Validador.nombrePersona("  Ana   Mora ", "Nombre"));
    }

    @Test
    void formateaMedidasYMontos() {
        assertEquals("$1,234.50", Formatos.moneda(new BigDecimal("1234.5")));
        assertEquals("45 g", Formatos.peso(new BigDecimal("0.045")));
        assertEquals("1.2 cm", Formatos.tamano(new BigDecimal("0.012")));
        assertEquals("salon_nandu", Formatos.slug("Salón Ñandú"));
    }
}
