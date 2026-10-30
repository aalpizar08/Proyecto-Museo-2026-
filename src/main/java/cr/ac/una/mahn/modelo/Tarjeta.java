package cr.ac.una.mahn.modelo;

import java.math.BigDecimal;

/** Tipo de tarjeta aceptado y la comisión que cobra la marca por transacción. */
public class Tarjeta {

    private int id;
    private String nombre;
    private BigDecimal porcentajeComision;
    private boolean activa = true;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPorcentajeComision() {
        return porcentajeComision;
    }

    public void setPorcentajeComision(BigDecimal porcentajeComision) {
        this.porcentajeComision = porcentajeComision;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
