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
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tablas
        db.execSQL("CREATE TABLE GRUPOS (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "descripcion TEXT)");

        db.execSQL("CREATE TABLE MAESTROS (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "usuario TEXT NOT NULL UNIQUE, " +
                "password TEXT NOT NULL, " +
                "grupo_id INTEGER, " +
                "FOREIGN KEY(grupo_id) REFERENCES GRUPOS(id))");

        db.execSQL("CREATE TABLE ALUMNOS (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "matricula TEXT NOT NULL UNIQUE, " +
                "qr_code TEXT NOT NULL, " +
                "grupo_id INTEGER, " +
                "email TEXT, " +
                "FOREIGN KEY(grupo_id) REFERENCES GRUPOS(id))");

        db.execSQL("CREATE TABLE ASISTENCIAS (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "alumno_id INTEGER NOT NULL, " +
                "maestro_id INTEGER NOT NULL, " +
                "fecha TEXT NOT NULL, " +
                "estado TEXT NOT NULL, " +
                "observacion TEXT, " +
                "FOREIGN KEY(alumno_id) REFERENCES ALUMNOS(id), " +
                "FOREIGN KEY(maestro_id) REFERENCES MAESTROS(id))");

        db.execSQL("CREATE TABLE TAREAS (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "titulo TEXT NOT NULL, " +
                "descripcion TEXT, " +
                "fecha_asignacion TEXT NOT NULL, " +
                "fecha_limite TEXT NOT NULL, " +
                "grupo_id INTEGER, " +
                "maestro_id INTEGER, " +
                "FOREIGN KEY(grupo_id) REFERENCES GRUPOS(id), " +
                "FOREIGN KEY(maestro_id) REFERENCES MAESTROS(id))");

        db.execSQL("CREATE TABLE TAREA_ALUMNO (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tarea_id INTEGER NOT NULL, " +
                "alumno_id INTEGER NOT NULL, " +
                "estado TEXT NOT NULL DEFAULT 'Pendiente', " +
                "fecha_entrega TEXT, " +
                "comentario TEXT, " +
                "FOREIGN KEY(tarea_id) REFERENCES TAREAS(id), " +
                "FOREIGN KEY(alumno_id) REFERENCES ALUMNOS(id))");

        // Datos de prueba
        insertarDatosPrueba(db);
    }

    private void insertarDatosPrueba(SQLiteDatabase db) {
        String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 7);
        String fechaLimite = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

        // Grupos
        db.execSQL("INSERT INTO GRUPOS (id, nombre, descripcion) VALUES (1, 'Ingeniería en Sistemas 5A', 'Turno matutino')");

        // Maestros
        db.execSQL("INSERT INTO MAESTROS (nombre, usuario, password, grupo_id) VALUES ('Prof. García', 'garcia', '1234', 1)");

        // Alumnos
        db.execSQL("INSERT INTO ALUMNOS (nombre, matricula, qr_code, grupo_id, email) VALUES ('Ana Torres', 'ISC001', 'ISC001', 1, 'ana@mail.com')");
        db.execSQL("INSERT INTO ALUMNOS (nombre, matricula, qr_code, grupo_id, email) VALUES ('Luis Ramírez', 'ISC002', 'ISC002', 1, 'luis@mail.com')");
        db.execSQL("INSERT INTO ALUMNOS (nombre, matricula, qr_code, grupo_id, email) VALUES ('María González', 'ISC003', 'ISC003', 1, 'maria@mail.com')");

        // Tareas
        db.execSQL("INSERT INTO TAREAS (id, titulo, descripcion, fecha_asignacion, fecha_limite, grupo_id, maestro_id) VALUES (1, 'Tarea 1 - Introducción a SQLite', '', '" + hoy + "', '" + fechaLimite + "', 1, 1)");
        db.execSQL("INSERT INTO TAREAS (id, titulo, descripcion, fecha_asignacion, fecha_limite, grupo_id, maestro_id) VALUES (2, 'Tarea 2 - Diseño de interfaces', '', '" + hoy + "', '" + fechaLimite + "', 1, 1)");

        // Tarea_Alumno (3 alumnos * 2 tareas = 6 registros)
        for (int t = 1; t <= 2; t++) {
            for (int a = 1; a <= 3; a++) {
                db.execSQL("INSERT INTO TAREA_ALUMNO (tarea_id, alumno_id, estado) VALUES (" + t + ", " + a + ", 'Pendiente')");
            }
        }

        // Asistencias
        db.execSQL("INSERT INTO ASISTENCIAS (alumno_id, maestro_id, fecha, estado) VALUES (1, 1, '" + hoy + "', 'Presente')");
        db.execSQL("INSERT INTO ASISTENCIAS (alumno_id, maestro_id, fecha, estado) VALUES (2, 1, '" + hoy + "', 'Retardo')");
        db.execSQL("INSERT INTO ASISTENCIAS (alumno_id, maestro_id, fecha, estado) VALUES (3, 1, '" + hoy + "', 'Ausente')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS TAREA_ALUMNO");
        db.execSQL("DROP TABLE IF EXISTS TAREAS");
        db.execSQL("DROP TABLE IF EXISTS ASISTENCIAS");
        db.execSQL("DROP TABLE IF EXISTS ALUMNOS");
        db.execSQL("DROP TABLE IF EXISTS MAESTROS");
        db.execSQL("DROP TABLE IF EXISTS GRUPOS");
        onCreate(db);
    }

    // ---- MÉTODOS CRUD ----

    // ---- GRUPOS ----
    public long insertarGrupo(String nombre, String descripcion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("descripcion", descripcion);
        return db.insert("GRUPOS", null, values);
    }

    public List<Grupo> obtenerGrupos() {
        List<Grupo> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM GRUPOS", null);
        if (cursor.moveToFirst()) {
            do {
                lista.add(new Grupo(cursor.getInt(0), cursor.getString(1), cursor.getString(2)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    // ---- MAESTROS ----
    public long insertarMaestro(String nombre, String usuario, String password, int grupoId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("usuario", usuario);
        values.put("password", password);
        values.put("grupo_id", grupoId);
        return db.insert("MAESTROS", null, values);
    }

    public Maestro loginMaestro(String usuario, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM MAESTROS WHERE usuario = ? AND password = ?", new String[]{usuario, password});
        Maestro maestro = null;
        if (cursor.moveToFirst()) {
            maestro = new Maestro(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4));
        }
        cursor.close();
        return maestro;
    }

    public Maestro obtenerMaestroPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM MAESTROS WHERE id = ?", new String[]{String.valueOf(id)});
        Maestro maestro = null;
        if (cursor.moveToFirst()) {
            maestro = new Maestro(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4));
        }
        cursor.close();
        return maestro;
    }

    // ---- ALUMNOS ----
    public long insertarAlumno(String nombre, String matricula, String qrCode, int grupoId, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("matricula", matricula);
        values.put("qr_code", qrCode);
        values.put("grupo_id", grupoId);
        values.put("email", email);
        return db.insert("ALUMNOS", null, values);
    }

    public List<Alumno> obtenerAlumnosPorGrupo(int grupoId) {
        List<Alumno> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM ALUMNOS WHERE grupo_id = ?", new String[]{String.valueOf(grupoId)});
        if (cursor.moveToFirst()) {
            do {
                lista.add(new Alumno(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4), cursor.getString(5)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public Alumno obtenerAlumnoPorMatricula(String matricula) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM ALUMNOS WHERE matricula = ?", new String[]{matricula});
        Alumno alumno = null;
        if (cursor.moveToFirst()) {
            alumno = new Alumno(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4), cursor.getString(5));
        }
        cursor.close();
        return alumno;
    }

    public Alumno obtenerAlumnoPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM ALUMNOS WHERE id = ?", new String[]{String.valueOf(id)});
        Alumno alumno = null;
        if (cursor.moveToFirst()) {
            alumno = new Alumno(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4), cursor.getString(5));
        }
        cursor.close();
        return alumno;
    }

    public boolean actualizarAlumno(int id, String nombre, String matricula, String email, int grupoId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("matricula", matricula);
        values.put("email", email);
        values.put("grupo_id", grupoId);
        return db.update("ALUMNOS", values, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean eliminarAlumno(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("ALUMNOS", "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // ---- ASISTENCIAS ----
    public long insertarAsistencia(int alumnoId, int maestroId, String fecha, String estado, String observacion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("alumno_id", alumnoId);
        values.put("maestro_id", maestroId);
        values.put("fecha", fecha);
        values.put("estado", estado);
        values.put("observacion", observacion);
        return db.insert("ASISTENCIAS", null, values);
    }

    public List<Asistencia> obtenerAsistenciasPorGrupo(int grupoId) {
        List<Asistencia> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT ASISTENCIAS.* FROM ASISTENCIAS INNER JOIN ALUMNOS ON ASISTENCIAS.alumno_id = ALUMNOS.id WHERE ALUMNOS.grupo_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(grupoId)});
        if (cursor.moveToFirst()) {
            do {
                lista.add(new Asistencia(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public List<Asistencia> obtenerAsistenciasPorAlumno(int alumnoId) {
        List<Asistencia> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM ASISTENCIAS WHERE alumno_id = ?", new String[]{String.valueOf(alumnoId)});
        if (cursor.moveToFirst()) {
            do {
                lista.add(new Asistencia(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public List<Asistencia> obtenerAsistenciasHoy(int grupoId, String fecha) {
        List<Asistencia> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT ASISTENCIAS.* FROM ASISTENCIAS INNER JOIN ALUMNOS ON ASISTENCIAS.alumno_id = ALUMNOS.id WHERE ALUMNOS.grupo_id = ? AND ASISTENCIAS.fecha = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(grupoId), fecha});
        if (cursor.moveToFirst()) {
            do {
                lista.add(new Asistencia(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public boolean yaRegistradoHoy(int alumnoId, String fecha) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM ASISTENCIAS WHERE alumno_id = ? AND fecha = ?", new String[]{String.valueOf(alumnoId), fecha});
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        return existe;
    }

    public boolean actualizarAsistencia(int id, String estado, String observacion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("estado", estado);
        values.put("observacion", observacion);
        return db.update("ASISTENCIAS", values, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean eliminarAsistencia(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("ASISTENCIAS", "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // ---- TAREAS ----
    public long insertarTarea(String titulo, String descripcion, String fechaAsignacion, String fechaLimite, int grupoId, int maestroId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("titulo", titulo);
        values.put("descripcion", descripcion);
        values.put("fecha_asignacion", fechaAsignacion);
        values.put("fecha_limite", fechaLimite);
        values.put("grupo_id", grupoId);
        values.put("maestro_id", maestroId);
        return db.insert("TAREAS", null, values);
    }

    public List<Tarea> obtenerTareasPorMaestro(int maestroId) {
        List<Tarea> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TAREAS WHERE maestro_id = ?", new String[]{String.valueOf(maestroId)});
        if (cursor.moveToFirst()) {
            do {
                lista.add(new Tarea(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(5), cursor.getInt(6)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public boolean actualizarTarea(int id, String titulo, String descripcion, String fechaLimite) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("titulo", titulo);
        values.put("descripcion", descripcion);
        values.put("fecha_limite", fechaLimite);
        return db.update("TAREAS", values, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean eliminarTarea(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("TAREAS", "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // ---- TAREA_ALUMNO ----
    public void asignarTareaAGrupo(int tareaId, int grupoId) {
        SQLiteDatabase db = this.getWritableDatabase();
        List<Alumno> alumnos = obtenerAlumnosPorGrupo(grupoId);
        for (Alumno a : alumnos) {
            ContentValues values = new ContentValues();
            values.put("tarea_id", tareaId);
            values.put("alumno_id", a.getId());
            values.put("estado", "Pendiente");
            db.insert("TAREA_ALUMNO", null, values);
        }
    }

    public List<TareaAlumno> obtenerTareasPorAlumno(int alumnoId) {
        List<TareaAlumno> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TAREA_ALUMNO WHERE alumno_id = ?", new String[]{String.valueOf(alumnoId)});
        if (cursor.moveToFirst()) {
            do {
                lista.add(new TareaAlumno(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public List<TareaAlumno> obtenerTareasPorAlumnoYEstado(int alumnoId, String estado) {
        List<TareaAlumno> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TAREA_ALUMNO WHERE alumno_id = ? AND estado = ?", new String[]{String.valueOf(alumnoId), estado});
        if (cursor.moveToFirst()) {
            do {
                lista.add(new TareaAlumno(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public boolean actualizarEstadoTarea(int tareaAlumnoId, String estado, String fechaEntrega, String comentario) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("estado", estado);
        values.put("fecha_entrega", fechaEntrega);
        values.put("comentario", comentario);
        return db.update("TAREA_ALUMNO", values, "id = ?", new String[]{String.valueOf(tareaAlumnoId)}) > 0;
    }

    public int contarTareasPendientes(int alumnoId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM TAREA_ALUMNO WHERE alumno_id = ? AND estado = 'Pendiente'", new String[]{String.valueOf(alumnoId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
}