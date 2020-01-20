package com.services.models;

public class UbicacionModel {

    private String nombre;
    private String ip;
    private String puerto;

    public final static String NOMBRE_XD = "Por defecto";
    public final static String IP_XD = "192.168.173.1";
    public final static String PUERTO_XD = "8080";

    public UbicacionModel(String nombre, String ip, String puerto) {
        this.nombre = nombre;
        this.ip = ip;
        this.puerto = puerto;
    }

    public static UbicacionModel getDefaultUbicacion() {
        return new UbicacionModel(NOMBRE_XD, IP_XD, PUERTO_XD);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPuerto() {
        return puerto;
    }

    public void setPuerto(String puerto) {
        this.puerto = puerto;
    }
}