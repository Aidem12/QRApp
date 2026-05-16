package com.example.qrasist;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrasist.adapters.TareaAdapter;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.dialogs.TareaDialogFragment;
import com.example.qrasist.models.Maestro;
import com.example.qrasist.models.Tarea;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GestionTareasFragment extends Fragment implements TareaAdapter.TareaListener, TareaDialogFragment.OnTareaGuardadaListener {

    private DBHelper db;
    private SharedPreferences prefs;
    private List<Tarea> listaTareas;
    private TareaAdapter adapter;
    private int maestroId, grupoIdMaestro;

    private TextView tvResumenTareas;
    private RecyclerView rvTareas;
    private LinearLayout layoutEmptyTareas;
    private ExtendedFloatingActionButton fabNuevaTarea; // TIPO CORREGIDO

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tareas, container, false);

        db = new DBHelper(getContext());
        prefs = requireActivity().getSharedPreferences("QRAsistPrefs", Context.MODE_PRIVATE);
        maestroId = prefs.getInt("user_id", -1);

        Maestro maestro = db.obtenerMaestroPorId(maestroId);
        if (maestro != null) {
            grupoIdMaestro = maestro.getGrupoId();
        }

        tvResumenTareas = view.findViewById(R.id.tv_resumen_tareas);
        rvTareas = view.findViewById(R.id.rv_tareas);
        layoutEmptyTareas = view.findViewById(R.id.layout_empty_tareas);
        fabNuevaTarea = view.findViewById(R.id.fab_nueva_tarea_fragment);

        rvTareas.setLayoutManager(new LinearLayoutManager(getContext()));

        fabNuevaTarea.setOnClickListener(v -> abrirDialogTarea(-1));

        cargarTareas();

        return view;
    }

    private void cargarTareas() {
        listaTareas = db.obtenerTareasPorMaestro(maestroId);
        
        String fechaHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int activas = 0, vencidas = 0;
        
        for (Tarea t : listaTareas) {
            if (t.getFechaLimite().compareTo(fechaHoy) >= 0) activas++;
            else vencidas++;
        }

        tvResumenTareas.setText(listaTareas.size() + " tarea(s) · " + activas + " activa(s) · " + vencidas + " vencida(s)");

        if (listaTareas.isEmpty()) {
            layoutEmptyTareas.setVisibility(View.VISIBLE);
            rvTareas.setVisibility(View.GONE);
        } else {
            layoutEmptyTareas.setVisibility(View.GONE);
            rvTareas.setVisibility(View.VISIBLE);
        }

        adapter = new TareaAdapter(getContext(), listaTareas, db, this);
        rvTareas.setAdapter(adapter);
    }

    private void abrirDialogTarea(int tareaId) {
        TareaDialogFragment dialog = TareaDialogFragment.newInstance(tareaId, maestroId);
        dialog.setOnTareaGuardadaListener(this);
        dialog.show(getChildFragmentManager(), "dialog_tarea");
    }

    private void mostrarConfirmacionEliminar(Tarea tarea) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar tarea")
                .setMessage("¿Eliminar '" + tarea.getTitulo() + "'?\nSe eliminará el registro de todos los alumnos.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    if (db.eliminarTarea(tarea.getId())) {
                        Snackbar.make(rvTareas, "Tarea eliminada", Snackbar.LENGTH_SHORT).show();
                        cargarTareas();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onEditar(Tarea tarea) {
        abrirDialogTarea(tarea.getId());
    }

    @Override
    public void onEliminar(Tarea tarea) {
        mostrarConfirmacionEliminar(tarea);
    }

    @Override
    public void onGuardada() {
        cargarTareas();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarTareas();
    }
}