package com.example.qrasist.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrasist.R;
import com.example.qrasist.adapters.TareaAlumnoAdapter;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.TareaAlumno;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class TareasEstadoFragment extends Fragment implements TareaAlumnoAdapter.OnTareaActionListener {

    private String tabTipo;
    private int alumnoId;
    private DBHelper db;
    private RecyclerView rv;
    private LinearLayout layoutEmpty;
    private TextView tvMessage;
    private TareaAlumnoAdapter adapter;
    private List<TareaAlumno> listaFiltrada = new ArrayList<>();

    private TareaAlumno taskToUpdate;
    private int taskPosition = -1;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null && taskToUpdate != null) {
                    try {
                        // Crear un archivo local permanente en la carpeta de la app
                        String fileName = "evidencia_" + taskToUpdate.getId() + "_" + System.currentTimeMillis() + ".jpg";
                        File destFile = new File(requireContext().getFilesDir(), fileName);

                        // Copiar datos de la URI al archivo local
                        InputStream in = requireContext().getContentResolver().openInputStream(uri);
                        OutputStream out = new FileOutputStream(destFile);
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        out.close();
                        in.close();

                        // Guardamos la RUTA ABSOLUTA del archivo local
                        taskToUpdate.setComentario(destFile.getAbsolutePath());
                        
                        if (adapter != null) {
                            adapter.notifyItemChanged(taskPosition);
                        }
                        Toast.makeText(getContext(), "Imagen adjuntada correctamente", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    public static TareasEstadoFragment newInstance(String tipo, int aId) {
        TareasEstadoFragment fragment = new TareasEstadoFragment();
        Bundle args = new Bundle();
        args.putString("tipo", tipo);
        args.putInt("aId", aId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabTipo = getArguments().getString("tipo");
            alumnoId = getArguments().getInt("aId");
        }
        db = new DBHelper(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tareas_estado, container, false);
        rv = v.findViewById(R.id.rv_tareas_estado);
        layoutEmpty = v.findViewById(R.id.layout_empty_tareas_alumno);
        tvMessage = v.findViewById(R.id.tv_empty_message);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        cargarTareas();
        return v;
    }

    private void cargarTareas() {
        listaFiltrada.clear();
        List<TareaAlumno> todas = db.obtenerTareasPorAlumno(alumnoId);

        for (TareaAlumno ta : todas) {
            if (tabTipo.equals("Pendiente")) {
                if (ta.getEstado().equals("Pendiente")) listaFiltrada.add(ta);
            } else if (tabTipo.equals("Entregada")) {
                if (ta.getEstado().equals("Entregada") || ta.getEstado().equals("Rechazada") || ta.getEstado().equals("Revisada"))
                    listaFiltrada.add(ta);
            } else {
                if (ta.getEstado().equals("No entregada")) listaFiltrada.add(ta);
            }
        }

        if (listaFiltrada.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
            switch (tabTipo) {
                case "Pendiente": tvMessage.setText("No tienes tareas pendientes 🎉"); break;
                case "Entregada": tvMessage.setText("Aún no has entregado ninguna tarea"); break;
                default: tvMessage.setText("No tienes tareas sin entregar ✅"); break;
            }
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
            adapter = new TareaAlumnoAdapter(getContext(), listaFiltrada, db, this);
            rv.setAdapter(adapter);
        }
    }

    @Override
    public void onAdjuntarImagen(TareaAlumno ta, int position) {
        this.taskToUpdate = ta;
        this.taskPosition = position;
        pickImageLauncher.launch("image/*");
    }
}