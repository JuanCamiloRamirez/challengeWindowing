package com.model;

public class ClientPluAlma {

    private String idTrasaccion;
    private String cliente;
    private String plu;
    private String almacen;

    public ClientPluAlma(String idTrasaccion, String cliente, String plu, String almacen) {
        this.idTrasaccion = idTrasaccion;
        this.cliente = cliente;
        this.plu = plu;
        this.almacen = almacen;
    }

    public ClientPluAlma() {
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
}
