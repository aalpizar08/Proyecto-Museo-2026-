package cr.ac.una.mahn.servicio;

import cr.ac.una.mahn.util.Formatos;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import javafx.scene.image.Image;

/**
 * Carga y guarda las imágenes de especies y temáticas. La base de datos guarda una
 * ruta relativa (por ejemplo {@code especies/tyrannosaurus_rex.jpg}) que se busca
 * primero en la carpeta {@code imagenes/} y luego en los recursos de la aplicación.
 */
public final class ImagenServicio {

    public static final Path CARPETA = Path.of("imagenes");

    private ImagenServicio() {
    }

    /** Devuelve la imagen o {@code null} si la ruta está vacía o el archivo no existe. */
    public static Image cargar(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            return null;
        }
        String normalizada = ruta.trim().replace('\\', '/');
        try {
            Path archivo = Path.of(normalizada);
            if (archivo.isAbsolute() && Files.isRegularFile(archivo)) {
                return new Image(archivo.toUri().toString());
            }
            Path local = CARPETA.resolve(normalizada);
            if (Files.isRegularFile(local)) {
                return new Image(local.toUri().toString());
            }
        } catch (InvalidPathException ex) {
            return null;
        }
        URL recurso = ImagenServicio.class.getResource("/imagenes/" + normalizada);
        return recurso == null ? null : new Image(recurso.toExternalForm());
    }

    /**
     * Copia la imagen elegida por el usuario a {@code imagenes/<subcarpeta>} con un nombre
     * único y devuelve la ruta relativa que se guarda en la base de datos.
     */
    public static String copiar(File origen, String subcarpeta) throws IOException {
        String nombre = origen.getName();
        int punto = nombre.lastIndexOf('.');
        String extension = punto < 0 ? "jpg" : nombre.substring(punto + 1).toLowerCase(Locale.ROOT);
        String base = Formatos.slug(punto < 0 ? nombre : nombre.substring(0, punto));
        String nuevoNombre = base + "_" + System.currentTimeMillis() + "." + extension;
        Path carpeta = CARPETA.resolve(subcarpeta);
        Files.createDirectories(carpeta);
        Files.copy(origen.toPath(), carpeta.resolve(nuevoNombre), StandardCopyOption.REPLACE_EXISTING);
        return subcarpeta + "/" + nuevoNombre;
    }
}
