package com.example.qrasist.sync;

import android.content.Context;
import android.util.Log;

import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.*;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SyncManager {
    private static final String TAG = "SyncManager";
    private final DBHelper db;
    private final FirebaseFirestore firestore;

    public SyncManager(Context context) {
        this.db = new DBHelper(context);
        this.firestore = FirebaseFirestore.getInstance();
    }

    // --- SUBIDA (Dispositivo A) ---
    public void subirTodoAFirebase() {
        subirGrupos();
        subirAlumnos();
        subirAsistencias();
        subirTareas();
        subirTareasAlumnos();
    }

    private void subirGrupos() {
        for (Grupo g : db.obtenerGrupos()) {
            firestore.collection("grupos").document(String.valueOf(g.getId())).set(g);
        }
    }

    private void subirAlumnos() {
        for (Alumno a : db.obtenerTodosAlumnos()) {
            firestore.collection("alumnos").document(String.valueOf(a.getId())).set(a);
        }
    }

    private void subirAsistencias() {
        for (Asistencia a : db.obtenerTodasLasAsistencias()) {
            firestore.collection("asistencias").document(String.valueOf(a.getId())).set(a);
        }
    }

    private void subirTareas() {
        for (Tarea t : db.obtenerTodasTareas()) {
            firestore.collection("tareas").document(String.valueOf(t.getId())).set(t);
        }
    }

    private void subirTareasAlumnos() {
        for (TareaAlumno ta : db.obtenerTodasTareasAlumno()) {
            // Se suben los datos de texto directamente a Firestore.
            // La lógica de Firebase Storage ha sido desactivada.
            firestore.collection("tarea_alumno").document(String.valueOf(ta.getId())).set(ta);
        }
    }

    // --- ESCUCHA (Dispositivo B) ---
    public void iniciarEscuchaRealTime() {
        escucharColeccion("grupos", Grupo.class);
        escucharColeccion("alumnos", Alumno.class);
        escucharColeccion("asistencias", Asistencia.class);
        escucharColeccion("tareas", Tarea.class);
        escucharColeccion("tarea_alumno", TareaAlumno.class);
    }

    private <T> void escucharColeccion(String collectionPath, Class<T> modelClass) {
        firestore.collection(collectionPath).addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error en escucha: " + collectionPath, error);
                return;
            }
            if (value != null) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    T item = doc.toObject(modelClass);
                    actualizarLocal(item);
                }
            }
        });
    }

    private void actualizarLocal(Object item) {
        if (item instanceof Grupo) db.insertarGrupo((Grupo) item);
        else if (item instanceof Alumno) db.insertarAlumnoSync((Alumno) item);
        else if (item instanceof Asistencia) db.insertarAsistenciaSync((Asistencia) item);
        else if (item instanceof Tarea) db.insertarTareaSync((Tarea) item);
        else if (item instanceof TareaAlumno) db.insertarTareaAlumnoSync((TareaAlumno) item);
    }
}