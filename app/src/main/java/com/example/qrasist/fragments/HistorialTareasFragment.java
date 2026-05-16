package com.example.qrasist.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrasist.R;
import com.example.qrasist.adapters.TaskAccordionAdapter;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.Alumno;
import com.example.qrasist.models.Grupo;
import com.example.qrasist.models.Tarea;
import com.example.qrasist.models.TareaAlumno;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HistorialTareasFragment extends Fragment {

    private DBHelper db;
    private Spinner spinnerFiltroGrupo;
    private RecyclerView rvHistorial;
    private int maestroId;
    private List<Grupo> listaGrupos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historial_tareas, container, false);

        db = new DBHelper(getContext());
        SharedPreferences prefs = requireActivity().getSharedPreferences("QRAsistPrefs", Context.MODE_PRIVATE);
        maestroId = prefs.getInt("user_id", -1);

        // ID corregido para evitar el error de compilación
        spinnerFiltroGrupo = view.findViewById(R.id.spinner_filtro_grupo_tareas);
        rvHistorial = view.findViewById(R.id.rv_historial_tareas);
        rvHistorial.setLayoutManager(new LinearLayoutManager(getContext()));

        configurarSpinnerGrupos();
        return view;
    }

    private void configurarSpinnerGrupos() {
        listaGrupos = db.obtenerGrupos();
        List<String> nombres = new ArrayList<>();
        nombres.add("Selecciona un grupo...");
        for (Grupo g : listaGrupos) {
            nombres.add(g.getNombre());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nombres);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltroGrupo.setAdapter(adapter);

        spinnerFiltroGrupo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    cargarDatosAcordeon(listaGrupos.get(position - 1).getId());
                } else {
                    rvHistorial.setAdapter(null);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void cargarDatosAcordeon(int grupoId) {
        List<Tarea> tareasMaestro = db.obtenerTareasPorMaestro(maestroId);
        List<Alumno> alumnosGrupo = db.obtenerAlumnosPorGrupo(grupoId);
        Map<Tarea, List<TareaAlumno>> data = new LinkedHashMap<>();

        for (Tarea t : tareasMaestro) {
            if (t.getGrupoId() == grupoId) {
                List<TareaAlumno> entregas = new ArrayList<>();
                for (Alumno a : alumnosGrupo) {
                    List<TareaAlumno> tareasDelAlumno = db.obtenerTareasPorAlumno(a.getId());
                    for (TareaAlumno ta : tareasDelAlumno) {
                        if (ta.getTareaId() == t.getId()) {
                            entregas.add(ta);
                            break;
                        }
                    }
                }
                data.put(t, entregas);
            }
        }
        rvHistorial.setAdapter(new TaskAccordionAdapter(requireContext(), data, db));
    }
}
