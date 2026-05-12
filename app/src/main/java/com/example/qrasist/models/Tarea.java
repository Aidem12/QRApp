package com.example.qrasist.models;

public class Tarea {
    private int id;
    private String titulo;
    private String descripcion;
    private String fechaAsignacion;
    private String fechaLimite;
    private int grupoId;
    private int maestroId;

    public Tarea() {}

    public Tarea(int id, String titulo, String descripcion, String fechaAsignacion, String fechaLimite, int grupoId, int maestroId) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaAsignacion = fechaAsignacion;
        this.fechaLimite = fechaLimite;
        this.grupoId = grupoId;
        this.maestroId = maestroId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(String fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }
    public String getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(String fechaLimite) { this.fechaLimite = fechaLimite; }
    public int getGrupoId() { return grupoId; }
    public void setGrupoId(int grupoId) { this.grupoId = grupoId; }
    public int getMaestroId() { return maestroId; }
    public void setMaestroId(int maestroId) { this.maestroId = maestroId; }
}