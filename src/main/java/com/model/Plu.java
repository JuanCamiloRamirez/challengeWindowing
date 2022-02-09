package com.model;

public class Plu {

    private String idTrasaccion;
    private String plu;

    public String getIdTrasaccion() {
        return idTrasaccion;
    }

    public void setIdTrasaccion(String idTrasaccion) {
        this.idTrasaccion = idTrasaccion;
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
                "idTrasaccion='" + idTrasaccion + '\'' +
                ", plu='" + plu + '\'' +
                '}';
    }
}
