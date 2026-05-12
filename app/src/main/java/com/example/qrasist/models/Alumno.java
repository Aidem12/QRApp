package com.example.qrasist.models;

public class Alumno {
    private int id;
    private String nombre;
    private String matricula;
    private String qrCode;
    private int grupoId;
    private String email;

    public Alumno() {}

    public Alumno(int id, String nombre, String matricula, String qrCode, int grupoId, String email) {
        this.id = id;
        this.nombre = nombre;
        this.matricula = matricula;
        this.qrCode = qrCode;
        this.grupoId = grupoId;
        this.email = email;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public int getGrupoId() { return grupoId; }
    public void setGrupoId(int grupoId) { this.grupoId = grupoId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}