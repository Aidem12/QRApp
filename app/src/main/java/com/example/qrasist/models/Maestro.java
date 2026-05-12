package com.example.qrasist.models;

public class Maestro {
    private int id;
    private String nombre;
    private String usuario;
    private String password;
    private int grupoId;

    public Maestro() {}

    public Maestro(int id, String nombre, String usuario, String password, int grupoId) {
        this.id = id;
        this.nombre = nombre;
        this.usuario = usuario;
        this.password = password;
        this.grupoId = grupoId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public int getGrupoId() { return grupoId; }
    public void setGrupoId(int grupoId) { this.grupoId = grupoId; }
}