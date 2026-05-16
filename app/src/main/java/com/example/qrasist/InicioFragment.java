package com.example.qrasist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrasist.adapters.AsistenciaRecienteAdapter;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.Asistencia;
import com.example.qrasist.models.Maestro;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InicioFragment extends Fragment {

    private DBHelper db;
    private SharedPreferences prefs;
    private int maestroId, grupoId;
    private TextView tvSaludo, tvNombreMaestro;
    private TextView tvStatAsistencias, tvStatTareas, tvStatAlumnos;
    private RecyclerView rvUltimasAsistencias;
    private ExtendedFloatingActionButton fabNuevaTarea;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        db = new DBHelper(getContext());
        prefs = requireActivity().getSharedPreferences("QRAsistPrefs", Context.MODE_PRIVATE);
        maestroId = prefs.getInt("user_id", -1);

        tvSaludo = view.findViewById(R.id.tv_saludo);
        tvNombreMaestro = view.findViewById(R.id.tv_nombre_maestro);
        tvStatAsistencias = view.findViewById(R.id.tv_stat_asistencias);
        tvStatTareas = view.findViewById(R.id.tv_stat_tareas);
        tvStatAlumnos = view.findViewById(R.id.tv_stat_alumnos);
        rvUltimasAsistencias = view.findViewById(R.id.rv_ultimas_asistencias);
        fabNuevaTarea = view.findViewById(R.id.fab_nueva_tarea);

        rvUltimasAsistencias.setLayoutManager(new LinearLayoutManager(getContext()));

        cargarDatos();

        // CORRECCIÓN: Código activado para navegar
        fabNuevaTarea.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), GestionTareasActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void cargarDatos() {
        Maestro maestro = db.obtenerMaestroPorId(maestroId);
        if (maestro != null) {
            tvNombreMaestro.setText(maestro.getNombre());
            grupoId = maestro.getGrupoId();

            int hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (hora >= 6 && hora < 12) tvSaludo.setText("Buenos días,");
            else if (hora >= 12 && hora < 18) tvSaludo.setText("Buenas tardes,");
            else tvSaludo.setText("Buenas noches,");

            String fechaHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            List<Asistencia> asistenciasHoy = db.obtenerAsistenciasHoy(grupoId, fechaHoy);
            tvStatAsistencias.setText(String.valueOf(asistenciasHoy.size()));
            tvStatAlumnos.setText(String.valueOf(db.obtenerAlumnosPorGrupo(grupoId).size()));
            tvStatTareas.setText(String.valueOf(db.obtenerTareasPorMaestro(maestroId).size()));

            if (asistenciasHoy.size() > 5) {
                asistenciasHoy = asistenciasHoy.subList(0, 5);
            }
            AsistenciaRecienteAdapter adapter = new AsistenciaRecienteAdapter(getContext(), asistenciasHoy, db);
            rvUltimasAsistencias.setAdapter(adapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarDatos();
    }
}