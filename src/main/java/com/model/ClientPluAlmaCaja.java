package com.model;

import org.apache.kafka.common.protocol.types.Field;

public class ClientPluAlmaCaja {

    private String idTrasaccion;
    private String cliente;
    private String plu;
    private String almacen;
    private String caja;

    public ClientPluAlmaCaja(String idTrasaccion, String cliente, String plu, String almacen, String caja) {
        this.idTrasaccion = idTrasaccion;
        this.cliente = cliente;
        this.plu = plu;
        this.almacen = almacen;
        this.caja= caja;
    }

    public ClientPluAlmaCaja() {
    }

    public String getPlu() {
        return plu;
    }

    public void setPlu(String plu) {
        this.plu = plu;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getIdTrasaccion() {
        return idTrasaccion;
    }

    public void setIdTrasaccion(String idTrasaccion) {
        this.idTrasaccion = idTrasaccion;
    }

    public String getAlmacen() {
        return almacen;
    }

    public void setAlmacen(String almacen) {
        this.almacen = almacen;
    }

    public String getCaja() {
        return caja;
    }

    public void setCaja(String caja) {
        this.caja = caja;
    }
}
