package cr.ac.una.mahn.util;

import cr.ac.una.mahn.vista.ControlEstrellas;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;

/** Fábrica de columnas para los TableView de la aplicación. */
public final class Tablas {

    private Tablas() {
    }

    /** Columna de texto; si el texto es largo se muestra completo en un tooltip. */
    public static <S> TableColumn<S, String> texto(String titulo, Function<S, String> valor, double ancho) {
        TableColumn<S, String> columna = new TableColumn<>(titulo);
        columna.setPrefWidth(ancho);
        columna.setCellValueFactory(d -> new ReadOnlyStringWrapper(valor.apply(d.getValue())));
        columna.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean vacio) {
                super.updateItem(item, vacio);
                setText(vacio || item == null ? null : item);
                setTooltip(vacio || item == null || item.length() < 35 ? null : new Tooltip(item));
            }
        });
        return columna;
    }

    /** Columna de montos en dólares alineada a la derecha. */
    public static <S> TableColumn<S, BigDecimal> moneda(String titulo, Function<S, BigDecimal> valor, double ancho) {
        return formateada(titulo, valor, ancho, Formatos::moneda, "celda-numero");
    }

    public static <S> TableColumn<S, BigDecimal> porcentaje(String titulo, Function<S, BigDecimal> valor, double ancho) {
        return formateada(titulo, valor, ancho, Formatos::porcentaje, "celda-numero");
    }

    public static <S> TableColumn<S, Integer> entero(String titulo, Function<S, Integer> valor, double ancho) {
        return formateada(titulo, valor, ancho, String::valueOf, "celda-numero");
    }

    /** Columna de fecha que se ordena como fecha y se muestra como dd/MM/yyyy. */
    public static <S> TableColumn<S, LocalDate> fecha(String titulo, Function<S, LocalDate> valor, double ancho) {
        return formateada(titulo, valor, ancho, Formatos::fecha, "celda-centro");
    }

    /** Columna que muestra el promedio con estrellas. */
    public static <S> TableColumn<S, Double> estrellas(String titulo, Function<S, Double> valor, double ancho) {
        TableColumn<S, Double> columna = new TableColumn<>(titulo);
        columna.setPrefWidth(ancho);
        columna.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(valor.apply(d.getValue())));
        columna.setCellFactory(c -> new TableCell<>() {
            private final ControlEstrellas estrellas = new ControlEstrellas(16, false);

            @Override
            protected void updateItem(Double item, boolean vacio) {
                super.updateItem(item, vacio);
                if (vacio || item == null) {
                    setGraphic(null);
                } else {
                    estrellas.setValor(item);
                    setGraphic(estrellas);
                }
            }
        });
        return columna;
    }

    private static <S, T> TableColumn<S, T> formateada(String titulo, Function<S, T> valor, double ancho,
                                                      Function<T, String> formato, String estilo) {
        TableColumn<S, T> columna = new TableColumn<>(titulo);
        columna.setPrefWidth(ancho);
        columna.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(valor.apply(d.getValue())));
        columna.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean vacio) {
                super.updateItem(item, vacio);
                setText(vacio || item == null ? null : formato.apply(item));
            }
        });
        columna.getStyleClass().add(estilo);
        return columna;
    }
}
