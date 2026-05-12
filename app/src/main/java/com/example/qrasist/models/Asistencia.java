package com.example.qrasist.models;

public class Asistencia {
    private int id;
    private int alumnoId;
    private int maestroId;
    private String fecha;
    private String estado;
    private String observacion;

    public Asistencia() {}

    public Asistencia(int id, int alumnoId, int maestroId, String fecha, String estado, String observacion) {
        this.id = id;
        this.alumnoId = alumnoId;
        this.maestroId = maestroId;
        this.fecha = fecha;
        this.estado = estado;
        this.observacion = observacion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAlumnoId() { return alumnoId; }
    public void setAlumnoId(int alumnoId) { this.alumnoId = alumnoId; }
    public int getMaestroId() { return maestroId; }
    public void setMaestroId(int maestroId) { this.maestroId = maestroId; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}