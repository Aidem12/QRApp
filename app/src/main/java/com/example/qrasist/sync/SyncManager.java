package com.example.qrasist.sync;

import android.content.Context;
import android.util.Log;

import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.*;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class SyncManager {
    private static final String TAG = "FIREBASE_SYNC";
    private final DBHelper db;
    private final FirebaseFirestore firestore;

    public SyncManager(Context context) {
        this.db = new DBHelper(context);
        this.firestore = FirebaseFirestore.getInstance();
        
        // Habilitar persistencia offline
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        this.firestore.setFirestoreSettings(settings);
    }

    // --- MÉTODOS DE SINCRONIZACIÓN INDIVIDUAL (INSERT/UPDATE) ---

    public void syncAlumno(Alumno a) {
        firestore.collection("alumnos").document(String.valueOf(a.getId())).set(a)
            .addOnSuccessListener(unused -> Log.d(TAG, "¡Éxito! Dato subido a la nube al instante."))
            .addOnFailureListener(e -> Log.e(TAG, "Falló la subida en tiempo real: ", e));
    }

    public void syncAsistencia(Asistencia a) {
        firestore.collection("asistencias").document(String.valueOf(a.getId())).set(a)
            .addOnSuccessListener(unused -> Log.d(TAG, "¡Éxito! Dato subido a la nube al instante."))
            .addOnFailureListener(e -> Log.e(TAG, "Falló la subida en tiempo real: ", e));
    }

    public void syncTarea(Tarea t) {
        firestore.collection("tareas").document(String.valueOf(t.getId())).set(t)
            .addOnSuccessListener(unused -> Log.d(TAG, "¡Éxito! Dato subido a la nube al instante."))
            .addOnFailureListener(e -> Log.e(TAG, "Falló la subida en tiempo real: ", e));
    }

    public void syncTareaAlumno(TareaAlumno ta) {
        firestore.collection("tarea_alumno").document(String.valueOf(ta.getId())).set(ta)
            .addOnSuccessListener(unused -> Log.d(TAG, "¡Éxito! Dato subido a la nube al instante."))
            .addOnFailureListener(e -> Log.e(TAG, "Falló la subida en tiempo real: ", e));
    }

    // --- MÉTODOS DE BORRADO (DELETE) ---

    public void eliminarAlumnoFirestore(int id) {
        firestore.collection("alumnos").document(String.valueOf(id)).delete()
            .addOnSuccessListener(unused -> Log.d(TAG, "¡Éxito! Documento eliminado de la nube."))
            .addOnFailureListener(e -> Log.e(TAG, "Error al intentar borrar en la nube: ", e));
    }

    public void eliminarAsistenciaFirestore(int id) {
        firestore.collection("asistencias").document(String.valueOf(id)).delete()
            .addOnSuccessListener(unused -> Log.d(TAG, "¡Éxito! Documento eliminado de la nube."))
            .addOnFailureListener(e -> Log.e(TAG, "Error al intentar borrar en la nube: ", e));
    }

    public void eliminarTareaFirestore(int id) {
        firestore.collection("tareas").document(String.valueOf(id)).delete()
            .addOnSuccessListener(unused -> Log.d(TAG, "¡Éxito! Documento eliminado de la nube."))
            .addOnFailureListener(e -> Log.e(TAG, "Error al intentar borrar en la nube: ", e));
        
        // También se deben borrar las asignaciones de esa tarea
        firestore.collection("tarea_alumno")
            .whereEqualTo("tareaId", id)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    doc.getReference().delete();
                }
            });
    }

    public void eliminarTareaAlumnoFirestore(int id) {
        firestore.collection("tarea_alumno").document(String.valueOf(id)).delete()
            .addOnSuccessListener(unused -> Log.d(TAG, "¡Éxito! Documento eliminado de la nube."))
            .addOnFailureListener(e -> Log.e(TAG, "Error al intentar borrar en la nube: ", e));
    }

    // --- SUBIDA MASIVA ---
    public void subirTodoAFirebase() {
        for (Grupo g : db.obtenerGrupos()) {
            firestore.collection("grupos").document(String.valueOf(g.getId())).set(g);
        }
        for (Alumno a : db.obtenerTodosAlumnos()) syncAlumno(a);
        for (Asistencia a : db.obtenerTodasLasAsistencias()) syncAsistencia(a);
        for (Tarea t : db.obtenerTodasTareas()) syncTarea(t);
        for (TareaAlumno ta : db.obtenerTodasTareasAlumno()) syncTareaAlumno(ta);
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