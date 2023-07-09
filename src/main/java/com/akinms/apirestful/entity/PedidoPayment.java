package com.akinms.apirestful.entity;

import java.util.Date;
import java.util.Set;

public class PedidoPayment {
    private Date fecha;
    private double monto_total;
    private String estado;
    private String tipo_entrega;
    private String metodo_pago;
    private Cliente cliente;
    public Bodega bodega;
    private Set<DetallePedido> detallesPedido;
    private Payment payment;

    public PedidoPayment() {
    }

    public PedidoPayment(Date fecha, double monto_total, String estado, String tipo_entrega, String metodo_pago, Cliente cliente, Bodega bodega, Set<DetallePedido> detallesPedido, Payment payment) {
        this.fecha = fecha;
        this.monto_total = monto_total;
        this.estado = estado;
        this.tipo_entrega = tipo_entrega;
        this.metodo_pago = metodo_pago;
        this.cliente = cliente;
        this.bodega = bodega;
        this.detallesPedido = detallesPedido;
        this.payment = payment;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public double getMonto_total() {
        return monto_total;
    }

    public void setMonto_total(double monto_total) {
        this.monto_total = monto_total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getTipo_entrega() {
        return tipo_entrega;
    }

    public void setTipo_entrega(String tipo_entrega) {
        this.tipo_entrega = tipo_entrega;
    }

    public String getMetodo_pago() {
        return metodo_pago;
    }

    public void setMetodo_pago(String metodo_pago) {
        this.metodo_pago = metodo_pago;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Bodega getBodega() {
        return bodega;
    }

    public void setBodega(Bodega bodega) {
        this.bodega = bodega;
    }

    public Set<DetallePedido> getDetallesPedido() {
        return detallesPedido;
    }

    public void setDetallesPedido(Set<DetallePedido> detallesPedido) {
        this.detallesPedido = detallesPedido;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }
}
