package com.model;

import org.apache.kafka.common.protocol.types.Field;

import java.util.ArrayList;

public class ClientPlu {

    private String idTrasaccion;
    private String cliente;
    private String plu;

    public ClientPlu(String idTrasaccion, String cliente, String plu) {
        this.idTrasaccion= idTrasaccion;
        this.cliente=cliente;
        this.plu=plu;
    }

    public ClientPlu() {

    }

    public String getIdTrasaccion() {
        return idTrasaccion;
    }

    public void setIdTrasaccion(String idTrasaccion) {
        this.idTrasaccion = idTrasaccion;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getPlu() {
        return plu;
    }

    public void setPlu(String plu) {
        this.plu = plu;
    }

    @Override
    public String toString() {
        return "{" +
                "cliente='" + cliente + '\'' +
                ", plu='" + plu + '\'' +
                '}';
    }
}
