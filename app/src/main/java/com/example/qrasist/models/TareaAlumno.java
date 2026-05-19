package com.example.qrasist.models;

public class TareaAlumno {
    private int id;
    private int tareaId;
    private int alumnoId;
    private String estado;
    private String fechaEntrega;
    private String comentario; // Ruta local de la imagen
    private String imageUrl;   // URL de Firebase Storage

    public TareaAlumno() {}

    public TareaAlumno(int id, int tareaId, int alumnoId, String estado, String fechaEntrega, String comentario) {
        this.id = id;
        this.tareaId = tareaId;
        this.alumnoId = alumnoId;
        this.estado = estado;
        this.fechaEntrega = fechaEntrega;
        this.comentario = comentario;
    }

    // Constructor completo para sincronización
    public TareaAlumno(int id, int tareaId, int alumnoId, String estado, String fechaEntrega, String comentario, String imageUrl) {
        this.id = id;
        this.tareaId = tareaId;
        this.alumnoId = alumnoId;
        this.estado = estado;
        this.fechaEntrega = fechaEntrega;
        this.comentario = comentario;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTareaId() { return tareaId; }
    public void setTareaId(int tareaId) { this.tareaId = tareaId; }
    public int getAlumnoId() { return alumnoId; }
    public void setAlumnoId(int alumnoId) { this.alumnoId = alumnoId; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(String fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}