package com.example.qrasist.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.qrasist.R;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.Alumno;
import com.example.qrasist.models.Grupo;
import com.example.qrasist.sync.SyncManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AlumnoDialogFragment extends DialogFragment {

    public interface OnAlumnoGuardadoListener {
        void onGuardado();
    }

    private int alumnoId = -1;
    private OnAlumnoGuardadoListener listener;
    private DBHelper db;
    private List<Grupo> listaGrupos;

    private TextView tvTitulo;
    private TextInputEditText etNombre, etMatricula, etEmail;
    private Spinner spinnerGrupo;
    private Button btnCancelar, btnGuardar;

    public static AlumnoDialogFragment newInstance(int alumnoId) {
        AlumnoDialogFragment fragment = new AlumnoDialogFragment();
        Bundle args = new Bundle();
        args.putInt("alumno_id", alumnoId);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnAlumnoGuardadoListener(OnAlumnoGuardadoListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            alumnoId = getArguments().getInt("alumno_id");
        }
        db = new DBHelper(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_alumno, container, false);

        tvTitulo = view.findViewById(R.id.tv_dialog_titulo);
        etNombre = view.findViewById(R.id.et_nombre_dialog);
        etMatricula = view.findViewById(R.id.et_matricula_dialog);
        etEmail = view.findViewById(R.id.et_email_dialog);
        spinnerGrupo = view.findViewById(R.id.spinner_grupo_dialog);
        btnCancelar = view.findViewById(R.id.btn_cancelar_dialog);
        btnGuardar = view.findViewById(R.id.btn_guardar_dialog);

        configurarSpinner();

        if (alumnoId != -1) {
            tvTitulo.setText("Editar Alumno");
            cargarDatosAlumno();
        } else {
            tvTitulo.setText("Nuevo Alumno");
        }

        btnCancelar.setOnClickListener(v -> dismiss());
        btnGuardar.setOnClickListener(v -> guardarAlumno());

        return view;
    }

    private void configurarSpinner() {
        listaGrupos = db.obtenerGrupos();
        List<String> nombresGrupos = new ArrayList<>();
        for (Grupo g : listaGrupos) {
            nombresGrupos.add(g.getNombre());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, nombresGrupos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGrupo.setAdapter(adapter);
    }

    private void cargarDatosAlumno() {
        Alumno alumno = db.obtenerAlumnoPorId(alumnoId);
        if (alumno != null) {
            etNombre.setText(alumno.getNombre());
            etMatricula.setText(alumno.getMatricula());
            etEmail.setText(alumno.getEmail());

            for (int i = 0; i < listaGrupos.size(); i++) {
                if (listaGrupos.get(i).getId() == alumno.getGrupoId()) {
                    spinnerGrupo.setSelection(i);
                    break;
                }
            }
        }
    }

    private void guardarAlumno() {
        String nombre = etNombre.getText().toString().trim();
        String matricula = etMatricula.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (nombre.isEmpty() || matricula.isEmpty()) {
            Toast.makeText(getContext(), "Nombre y Matrícula son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Email inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerGrupo.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Selecciona un grupo", Toast.LENGTH_SHORT).show();
            return;
        }

        int grupoId = listaGrupos.get(spinnerGrupo.getSelectedItemPosition()).getId();

        boolean exito;
        SyncManager syncManager = new SyncManager(getContext());
        if (alumnoId == -1) {
            long id = db.insertarAlumno(nombre, matricula, matricula, grupoId, email);
            exito = id != -1;
            if (exito) {
                Alumno a = db.obtenerAlumnoPorId((int) id);
                if (a != null) syncManager.syncAlumno(a);
            }
        } else {
            exito = db.actualizarAlumno(alumnoId, nombre, matricula, email, grupoId);
            if (exito) {
                Alumno a = db.obtenerAlumnoPorId(alumnoId);
                if (a != null) syncManager.syncAlumno(a);
            }
        }

        if (exito) {
            if (listener != null) listener.onGuardado();
            dismiss();
        } else {
            Toast.makeText(getContext(), "Error al guardar alumno", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}