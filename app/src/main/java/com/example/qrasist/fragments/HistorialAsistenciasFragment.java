package com.example.qrasist.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrasist.R;
import com.example.qrasist.adapters.AttendanceAccordionAdapter;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.*;

import java.util.*;

public class HistorialAsistenciasFragment extends Fragment {

    private DBHelper db;
    private Spinner spinnerGrupo;
    private Button btnFecha;
    private TextView tvInfo;
    private RecyclerView rv;
    private AttendanceAccordionAdapter adapter;

    private int grupoIdSeleccionado = -1;
    private String fechaSeleccionada = null;
    private List<Grupo> listaGrupos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historial_asistencias, container, false);

        db = new DBHelper(getContext());
        spinnerGrupo = view.findViewById(R.id.spinner_filtro_grupo);
        btnFecha = view.findViewById(R.id.btn_filtro_fecha);
        tvInfo = view.findViewById(R.id.tv_info_filtro);
        rv = view.findViewById(R.id.rv_historial_asistencias);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AttendanceAccordionAdapter(db);
        rv.setAdapter(adapter);

        configurarFiltros();
        actualizarLista();

        return view;
    }

    private void configurarFiltros() {
        // Spinner Grupos
        listaGrupos = db.obtenerGrupos();
        List<String> nombres = new ArrayList<>();
        nombres.add("Todos los grupos");
        for (Grupo g : listaGrupos) nombres.add(g.getNombre());

        ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, nombres);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGrupo.setAdapter(groupAdapter);

        spinnerGrupo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                grupoIdSeleccionado = (position == 0) ? -1 : listaGrupos.get(position - 1).getId();
                actualizarLista();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Botón Fecha
        btnFecha.setOnClickListener(v -> {
            if (fechaSeleccionada != null) {
                fechaSeleccionada = null;
                btnFecha.setText("Por Fecha");
                actualizarLista();
            } else {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(getContext(), (dp, year, month, day) -> {
                    fechaSeleccionada = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day);
                    btnFecha.setText(fechaSeleccionada);
                    actualizarLista();
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void actualizarLista() {
        List<Asistencia> asistencias;
        if (grupoIdSeleccionado != -1 && fechaSeleccionada != null) {
            asistencias = db.obtenerAsistenciasHoy(grupoIdSeleccionado, fechaSeleccionada);
        } else if (grupoIdSeleccionado != -1) {
            asistencias = db.obtenerAsistenciasPorGrupo(grupoIdSeleccionado);
        } else if (fechaSeleccionada != null) {
            asistencias = db.obtenerAsistenciasPorFecha(fechaSeleccionada);
        } else {
            asistencias = db.obtenerTodasLasAsistencias();
        }

        Map<String, List<Asistencia>> agrupadas = new LinkedHashMap<>();
        for (Asistencia a : asistencias) {
            String key;
            if (fechaSeleccionada != null) {
                Alumno al = db.obtenerAlumnoPorId(a.getAlumnoId());
                Grupo g = (al != null) ? db.obtenerGrupoPorId(al.getGrupoId()) : null;
                key = (g != null) ? g.getNombre() : "Sin Grupo";
            } else {
                key = a.getFecha();
            }

            if (!agrupadas.containsKey(key)) agrupadas.put(key, new ArrayList<>());
            agrupadas.get(key).add(a);
        }

        List<Object> items = new ArrayList<>();
        for (String header : agrupadas.keySet()) {
            items.add(header);
            items.addAll(agrupadas.get(header));
        }

        adapter.setData(items);
        tvInfo.setText(asistencias.size() + " registros encontrados");
    }
}
