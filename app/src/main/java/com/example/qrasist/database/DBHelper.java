package com.example.qrasist.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.qrasist.models.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "qrasist.db";
    private static final int DATABASE_VERSION = 2;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE GRUPOS (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, descripcion TEXT)");
        db.execSQL("CREATE TABLE MAESTROS (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, usuario TEXT NOT NULL UNIQUE, password TEXT NOT NULL, grupo_id INTEGER, FOREIGN KEY(grupo_id) REFERENCES GRUPOS(id))");
        db.execSQL("CREATE TABLE ALUMNOS (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, matricula TEXT NOT NULL UNIQUE, qr_code TEXT NOT NULL, grupo_id INTEGER, email TEXT, FOREIGN KEY(grupo_id) REFERENCES GRUPOS(id))");
        db.execSQL("CREATE TABLE ASISTENCIAS (id INTEGER PRIMARY KEY AUTOINCREMENT, alumno_id INTEGER NOT NULL, maestro_id INTEGER NOT NULL, fecha TEXT NOT NULL, estado TEXT NOT NULL, observacion TEXT, FOREIGN KEY(alumno_id) REFERENCES ALUMNOS(id), FOREIGN KEY(maestro_id) REFERENCES MAESTROS(id))");
        db.execSQL("CREATE TABLE TAREAS (id INTEGER PRIMARY KEY AUTOINCREMENT, titulo TEXT NOT NULL, descripcion TEXT, fecha_asignacion TEXT NOT NULL, fecha_limite TEXT NOT NULL, grupo_id INTEGER, maestro_id INTEGER, FOREIGN KEY(grupo_id) REFERENCES GRUPOS(id), FOREIGN KEY(maestro_id) REFERENCES MAESTROS(id))");
        db.execSQL("CREATE TABLE TAREA_ALUMNO (id INTEGER PRIMARY KEY AUTOINCREMENT, tarea_id INTEGER NOT NULL, alumno_id INTEGER NOT NULL, estado TEXT NOT NULL DEFAULT 'Pendiente', fecha_entrega TEXT, comentario TEXT, image_url TEXT, FOREIGN KEY(tarea_id) REFERENCES TAREAS(id), FOREIGN KEY(alumno_id) REFERENCES ALUMNOS(id))");
        insertarDatosPrueba(db);
    }

    private void insertarDatosPrueba(SQLiteDatabase db) {
        String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        db.execSQL("INSERT INTO GRUPOS (id, nombre, descripcion) VALUES (1, 'Ingeniería en Sistemas 5A', 'Turno matutino')");
        db.execSQL("INSERT INTO MAESTROS (nombre, usuario, password, grupo_id) VALUES ('Prof. García', 'garcia', '1234', 1)");
        db.execSQL("INSERT INTO ALUMNOS (nombre, matricula, qr_code, grupo_id, email) VALUES ('Ana Torres', 'ISC001', 'ISC001', 1, 'ana@mail.com'), ('Luis Ramírez', 'ISC002', 'ISC002', 1, 'luis@mail.com'), ('María González', 'ISC003', 'ISC003', 1, 'maria@mail.com')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE TAREA_ALUMNO ADD COLUMN image_url TEXT");
        }
    }

    // --- GRUPOS ---
    public List<Grupo> obtenerGrupos() {
        List<Grupo> lista = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM GRUPOS", null);
        if (c.moveToFirst()) do { lista.add(new Grupo(c.getInt(0), c.getString(1), c.getString(2))); } while (c.moveToNext());
        c.close(); return lista;
    }
    public Grupo obtenerGrupoPorId(int id) {
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM GRUPOS WHERE id = ?", new String[]{String.valueOf(id)});
        if (c.moveToFirst()) { Grupo g = new Grupo(c.getInt(0), c.getString(1), c.getString(2)); c.close(); return g; }
        c.close(); return null;
    }
    public long insertarGrupo(String n, String d) {
        ContentValues v = new ContentValues(); v.put("nombre", n); v.put("descripcion", d);
        return getWritableDatabase().insert("GRUPOS", null, v);
    }

    // --- MAESTROS ---
    public Maestro loginMaestro(String u, String p) {
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM MAESTROS WHERE usuario=? AND password=?", new String[]{u, p});
        if (c.moveToFirst()) return new Maestro(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getInt(4));
        return null;
    }
    public Maestro obtenerMaestroPorId(int id) {
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM MAESTROS WHERE id=?", new String[]{String.valueOf(id)});
        if (c.moveToFirst()) return new Maestro(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getInt(4));
        return null;
    }

    // --- ALUMNOS ---
    public List<Alumno> obtenerAlumnosPorGrupo(int gId) {
        List<Alumno> lista = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM ALUMNOS WHERE grupo_id=?", new String[]{String.valueOf(gId)});
        if (c.moveToFirst()) do { lista.add(new Alumno(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getInt(4), c.getString(5))); } while (c.moveToNext());
        c.close(); return lista;
    }
    public Alumno obtenerAlumnoPorId(int id) {
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM ALUMNOS WHERE id=?", new String[]{String.valueOf(id)});
        if (c.moveToFirst()) return new Alumno(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getInt(4), c.getString(5));
        c.close(); return null;
    }
    public Alumno obtenerAlumnoPorMatricula(String m) {
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM ALUMNOS WHERE matricula=?", new String[]{m});
        if (c.moveToFirst()) return new Alumno(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getInt(4), c.getString(5));
        return null;
    }
    public long insertarAlumno(String n, String m, String qr, int g, String e) {
        ContentValues v = new ContentValues(); v.put("nombre", n); v.put("matricula", m); v.put("qr_code", qr); v.put("grupo_id", g); v.put("email", e);
        return getWritableDatabase().insert("ALUMNOS", null, v);
    }
    public boolean actualizarAlumno(int id, String n, String m, String e, int g) {
        ContentValues v = new ContentValues(); v.put("nombre", n); v.put("matricula", m); v.put("email", e); v.put("grupo_id", g);
        return getWritableDatabase().update("ALUMNOS", v, "id=?", new String[]{String.valueOf(id)}) > 0;
    }
    public boolean eliminarAlumno(int id) { return getWritableDatabase().delete("ALUMNOS", "id=?", new String[]{String.valueOf(id)}) > 0; }

    // --- ASISTENCIAS ---
    public List<Asistencia> obtenerTodasLasAsistencias() {
        List<Asistencia> l = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM ASISTENCIAS ORDER BY fecha DESC", null);
        if (c.moveToFirst()) do { l.add(new Asistencia(c.getInt(0), c.getInt(1), c.getInt(2), c.getString(3), c.getString(4), c.getString(5))); } while (c.moveToNext());
        c.close(); return l;
    }
    public List<Asistencia> obtenerAsistenciasHoy(int g, String f) {
        List<Asistencia> l = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT ASISTENCIAS.* FROM ASISTENCIAS JOIN ALUMNOS ON ASISTENCIAS.alumno_id=ALUMNOS.id WHERE ALUMNOS.grupo_id=? AND ASISTENCIAS.fecha=?", new String[]{String.valueOf(g), f});
        if (c.moveToFirst()) do { l.add(new Asistencia(c.getInt(0), c.getInt(1), c.getInt(2), c.getString(3), c.getString(4), c.getString(5))); } while (c.moveToNext());
        c.close(); return l;
    }
    public List<Asistencia> obtenerAsistenciasPorGrupo(int g) {
        List<Asistencia> l = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT ASISTENCIAS.* FROM ASISTENCIAS JOIN ALUMNOS ON ASISTENCIAS.alumno_id=ALUMNOS.id WHERE ALUMNOS.grupo_id=?", new String[]{String.valueOf(g)});
        if (c.moveToFirst()) do { l.add(new Asistencia(c.getInt(0), c.getInt(1), c.getInt(2), c.getString(3), c.getString(4), c.getString(5))); } while (c.moveToNext());
        c.close(); return l;
    }
    public List<Asistencia> obtenerAsistenciasPorFecha(String f) {
        List<Asistencia> l = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM ASISTENCIAS WHERE fecha=?", new String[]{f});
        if (c.moveToFirst()) do { l.add(new Asistencia(c.getInt(0), c.getInt(1), c.getInt(2), c.getString(3), c.getString(4), c.getString(5))); } while (c.moveToNext());
        c.close(); return l;
    }
    public List<Asistencia> obtenerAsistenciasPorAlumno(int aId) {
        List<Asistencia> l = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM ASISTENCIAS WHERE alumno_id=?", new String[]{String.valueOf(aId)});
        if (c.moveToFirst()) do { l.add(new Asistencia(c.getInt(0), c.getInt(1), c.getInt(2), c.getString(3), c.getString(4), c.getString(5))); } while (c.moveToNext());
        c.close(); return l;
    }
    public boolean actualizarAsistencia(int id, String e, String o) {
        ContentValues v = new ContentValues(); v.put("estado", e); v.put("observacion", o);
        return getWritableDatabase().update("ASISTENCIAS", v, "id=?", new String[]{String.valueOf(id)}) > 0;
    }
    public boolean eliminarAsistencia(int id) { return getWritableDatabase().delete("ASISTENCIAS", "id=?", new String[]{String.valueOf(id)}) > 0; }
    public boolean yaRegistradoHoy(int a, String f) {
        Cursor c = getReadableDatabase().rawQuery("SELECT id FROM ASISTENCIAS WHERE alumno_id=? AND fecha=?", new String[]{String.valueOf(a), f});
        boolean r = c.getCount() > 0; c.close(); return r;
    }
    public long insertarAsistencia(int a, int m, String f, String e, String o) {
        ContentValues v = new ContentValues(); v.put("alumno_id", a); v.put("maestro_id", m); v.put("fecha", f); v.put("estado", e); v.put("observacion", o);
        return getWritableDatabase().insert("ASISTENCIAS", null, v);
    }

    // --- TAREAS ---
    public Tarea obtenerTareaPorId(int id) {
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM TAREAS WHERE id=?", new String[]{String.valueOf(id)});
        if (c.moveToFirst()) return new Tarea(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getInt(5), c.getInt(6));
        return null;
    }
    public List<Tarea> obtenerTareasPorMaestro(int m) {
        List<Tarea> l = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM TAREAS WHERE maestro_id=?", new String[]{String.valueOf(m)});
        if (c.moveToFirst()) do { l.add(new Tarea(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getInt(5), c.getInt(6))); } while (c.moveToNext());
        c.close(); return l;
    }
    public long insertarTarea(String t, String d, String fa, String fl, int g, int m) {
        ContentValues v = new ContentValues(); v.put("titulo", t); v.put("descripcion", d); v.put("fecha_asignacion", fa); v.put("fecha_limite", fl); v.put("grupo_id", g); v.put("maestro_id", m);
        return getWritableDatabase().insert("TAREAS", null, v);
    }
    public boolean actualizarTarea(int id, String t, String d, String fl) {
        ContentValues v = new ContentValues(); v.put("titulo", t); v.put("descripcion", d); v.put("fecha_limite", fl);
        return getWritableDatabase().update("TAREAS", v, "id=?", new String[]{String.valueOf(id)}) > 0;
    }
    public boolean eliminarTarea(int id) {
        getWritableDatabase().delete("TAREA_ALUMNO", "tarea_id=?", new String[]{String.valueOf(id)});
        return getWritableDatabase().delete("TAREAS", "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    // --- TAREA ALUMNO ---
    public List<TareaAlumno> obtenerTareasPorAlumno(int a) {
        List<TareaAlumno> l = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM TAREA_ALUMNO WHERE alumno_id=?", new String[]{String.valueOf(a)});
        if (c.moveToFirst()) do { l.add(new TareaAlumno(c.getInt(0), c.getInt(1), c.getInt(2), c.getString(3), c.getString(4), c.getString(5))); } while (c.moveToNext());
        c.close(); return l;
    }
    public List<TareaAlumno> obtenerTareasPorAlumnoYEstado(int a, String s) {
        List<TareaAlumno> l = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM TAREA_ALUMNO WHERE alumno_id=? AND estado=?", new String[]{String.valueOf(a), s});
        if (c.moveToFirst()) do { l.add(new TareaAlumno(c.getInt(0), c.getInt(1), c.getInt(2), c.getString(3), c.getString(4), c.getString(5))); } while (c.moveToNext());
        c.close(); return l;
    }
    public boolean actualizarEstadoTarea(int id, String s, String f, String o) {
        ContentValues v = new ContentValues(); v.put("estado", s); v.put("fecha_entrega", f); v.put("comentario", o);
        return getWritableDatabase().update("TAREA_ALUMNO", v, "id=?", new String[]{String.valueOf(id)}) > 0;
    }
    public int contarTareasPendientes(int a) {
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM TAREA_ALUMNO WHERE alumno_id=? AND estado='Pendiente'", new String[]{String.valueOf(a)});
        int count = 0; if (c.moveToFirst()) count = c.getInt(0); c.close(); return count;
    }
    public void asignarTareaAGrupo(int t, int g) {
        for (Alumno a : obtenerAlumnosPorGrupo(g)) {
            ContentValues v = new ContentValues(); v.put("tarea_id", t); v.put("alumno_id", a.getId()); v.put("estado", "Pendiente");
            getWritableDatabase().insert("TAREA_ALUMNO", null, v);
        }
    }

    // --- MÉTODOS SYNC (FIREBASE) ---
    public long insertarAlumnoSync(Alumno a) {
        ContentValues v = new ContentValues(); v.put("id", a.getId()); v.put("nombre", a.getNombre()); v.put("matricula", a.getMatricula()); v.put("qr_code", a.getQrCode()); v.put("grupo_id", a.getGrupoId()); v.put("email", a.getEmail());
        return getWritableDatabase().insertWithOnConflict("ALUMNOS", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public long insertarAsistenciaSync(Asistencia a) {
        ContentValues v = new ContentValues(); v.put("id", a.getId()); v.put("alumno_id", a.getAlumnoId()); v.put("maestro_id", a.getMaestroId()); v.put("fecha", a.getFecha()); v.put("estado", a.getEstado()); v.put("observacion", a.getObservacion());
        return getWritableDatabase().insertWithOnConflict("ASISTENCIAS", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public long insertarTareaSync(Tarea t) {
        ContentValues v = new ContentValues(); v.put("id", t.getId()); v.put("titulo", t.getTitulo()); v.put("descripcion", t.getDescripcion()); v.put("fecha_asignacion", t.getFechaAsignacion()); v.put("fecha_limite", t.getFechaLimite()); v.put("grupo_id", t.getGrupoId()); v.put("maestro_id", t.getMaestroId());
        return getWritableDatabase().insertWithOnConflict("TAREAS", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public long insertarTareaAlumnoSync(TareaAlumno ta) {
        ContentValues v = new ContentValues(); v.put("id", ta.getId()); v.put("tarea_id", ta.getTareaId()); v.put("alumno_id", ta.getAlumnoId()); v.put("estado", ta.getEstado()); v.put("fecha_entrega", ta.getFechaEntrega()); v.put("comentario", ta.getComentario()); v.put("image_url", ta.getImageUrl());
        return getWritableDatabase().insertWithOnConflict("TAREA_ALUMNO", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<Alumno> obtenerTodosAlumnos() {
        List<Alumno> lista = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM ALUMNOS", null);
        if (c.moveToFirst()) do { lista.add(new Alumno(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getInt(4), c.getString(5))); } while (c.moveToNext());
        c.close(); return lista;
    }

    public List<Tarea> obtenerTodasTareas() {
        List<Tarea> l = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM TAREAS", null);
        if (c.moveToFirst()) do { l.add(new Tarea(c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getInt(5), c.getInt(6))); } while (c.moveToNext());
        c.close(); return l;
    }

    public List<TareaAlumno> obtenerTodasTareasAlumno() {
        List<TareaAlumno> l = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM TAREA_ALUMNO", null);
        if (c.moveToFirst()) do { l.add(new TareaAlumno(c.getInt(0), c.getInt(1), c.getInt(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6))); } while (c.moveToNext());
        c.close(); return l;
    }

    public long insertarGrupo(Grupo g) {
        ContentValues v = new ContentValues(); 
        v.put("id", g.getId()); 
        v.put("nombre", g.getNombre()); 
        v.put("descripcion", g.getDescripcion());
        return getWritableDatabase().insertWithOnConflict("GRUPOS", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }
}