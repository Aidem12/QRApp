package com.example.qrasist;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrasist.adapters.AlumnoAdapter;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.dialogs.AlumnoDialogFragment;
import com.example.qrasist.dialogs.GrupoDialogFragment;
import com.example.qrasist.models.Alumno;
import com.example.qrasist.models.Grupo;
import com.example.qrasist.models.Maestro;
import com.example.qrasist.sync.SyncManager;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class GestionAlumnosFragment extends Fragment implements AlumnoAdapter.AlumnoListener, 
        AlumnoDialogFragment.OnAlumnoGuardadoListener, GrupoDialogFragment.OnGrupoGuardadoListener {

    private DBHelper db;
    private SharedPreferences prefs;
    private List<Alumno> listaAlumnos;
    private AlumnoAdapter adapter;
    private List<Grupo> listaGrupos;
    private int grupoIdSeleccionado;

    private SearchView searchView;
    private TextView tvTotalAlumnos;
    private RecyclerView rvAlumnos;
    private LinearLayout layoutEmpty;
    private Spinner spinnerFiltroGrupo;
    private ExtendedFloatingActionButton fabAgregarAlumno, fabAgregarGrupo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alumnos, container, false);

        db = new DBHelper(getContext());
        prefs = requireActivity().getSharedPreferences("QRAsistPrefs", Context.MODE_PRIVATE);

        // Inicializar con el grupo del maestro por defecto
        int maestroId = prefs.getInt("user_id", -1);
        Maestro maestro = db.obtenerMaestroPorId(maestroId);
        if (maestro != null) {
            grupoIdSeleccionado = maestro.getGrupoId();
        }

        searchView = view.findViewById(R.id.search_view_alumnos);
        tvTotalAlumnos = view.findViewById(R.id.tv_total_alumnos);
        rvAlumnos = view.findViewById(R.id.rv_alumnos);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        spinnerFiltroGrupo = view.findViewById(R.id.spinner_filtro_grupo_alumnos);
        fabAgregarAlumno = view.findViewById(R.id.fab_agregar_alumno);
        fabAgregarGrupo = view.findViewById(R.id.fab_agregar_grupo);

        rvAlumnos.setLayoutManager(new LinearLayoutManager(getContext()));

        configurarSpinnerGrupos();
        configurarBusqueda();
        
        fabAgregarAlumno.setOnClickListener(v -> abrirDialogAlumno(-1));
        fabAgregarGrupo.setOnClickListener(v -> abrirDialogGrupo());

        return view;
    }

    private void configurarSpinnerGrupos() {
        listaGrupos = db.obtenerGrupos();
        List<String> nombres = new ArrayList<>();
        int indexInicial = 0;
        
        for (int i = 0; i < listaGrupos.size(); i++) {
            nombres.add(listaGrupos.get(i).getNombre());
            if (listaGrupos.get(i).getId() == grupoIdSeleccionado) {
                indexInicial = i;
            }
        }

        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, nombres);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltroGrupo.setAdapter(adapterSpinner);
        spinnerFiltroGrupo.setSelection(indexInicial);

        spinnerFiltroGrupo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                grupoIdSeleccionado = listaGrupos.get(position).getId();
                cargarAlumnos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void cargarAlumnos() {
        listaAlumnos = db.obtenerAlumnosPorGrupo(grupoIdSeleccionado);
        tvTotalAlumnos.setText("Total: " + listaAlumnos.size() + " alumno(s)");

        if (listaAlumnos.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvAlumnos.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvAlumnos.setVisibility(View.VISIBLE);
        }

        adapter = new AlumnoAdapter(getContext(), listaAlumnos, db, this);
        rvAlumnos.setAdapter(adapter);
    }

    private void configurarBusqueda() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (adapter != null) adapter.filtrar(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) adapter.filtrar(newText);
                return true;
            }
        });
    }

    private void abrirDialogAlumno(int alumnoId) {
        AlumnoDialogFragment dialog = AlumnoDialogFragment.newInstance(alumnoId);
        dialog.setOnAlumnoGuardadoListener(this);
        dialog.show(getChildFragmentManager(), "dialog_alumno");
    }

    private void abrirDialogGrupo() {
        GrupoDialogFragment dialog = new GrupoDialogFragment();
        dialog.setOnGrupoGuardadoListener(this);
        dialog.show(getChildFragmentManager(), "dialog_grupo");
    }

    private void mostrarConfirmacionEliminar(Alumno alumno) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_eliminar_titulo)
                .setMessage("¿Eliminar a " + alumno.getNombre() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    if (db.eliminarAlumno(alumno.getId())) {
                        // GATILLO DE BORRADO FIRESTORE
                        new SyncManager(getContext()).eliminarAlumnoFirestore(alumno.getId());
                        
                        Snackbar.make(rvAlumnos, R.string.exito_alumno_eliminado, Snackbar.LENGTH_SHORT).show();
                        cargarAlumnos();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onEditar(Alumno alumno) {
        abrirDialogAlumno(alumno.getId());
    }

    @Override
    public void onEliminar(Alumno alumno) {
        mostrarConfirmacionEliminar(alumno);
    }

    @Override
    public void onGuardado() {
        cargarAlumnos();
        Snackbar.make(rvAlumnos, R.string.exito_alumno_guardado, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onGrupoGuardado() {
        configurarSpinnerGrupos(); // Recargar el spinner para incluir el nuevo grupo
        Snackbar.make(rvAlumnos, R.string.exito_grupo_guardado, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarAlumnos();
    }
}