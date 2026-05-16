package com.example.qrasist.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrasist.R;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.Alumno;
import com.example.qrasist.models.Grupo;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class AlumnoAdapter extends RecyclerView.Adapter<AlumnoAdapter.ViewHolder> {

    public interface AlumnoListener {
        void onEditar(Alumno alumno);
        void onEliminar(Alumno alumno);
    }

    private Context context;
    private List<Alumno> listaOriginal;
    private List<Alumno> listaFiltrada;
    private DBHelper db;
    private AlumnoListener listener;

    public AlumnoAdapter(Context context, List<Alumno> lista, DBHelper db, AlumnoListener listener) {
        this.context = context;
        this.listaOriginal = lista;
        this.listaFiltrada = new ArrayList<>(lista);
        this.db = db;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_alumno, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Alumno alumno = listaFiltrada.get(position);
        holder.tvNombre.setText(alumno.getNombre());
        holder.tvMatricula.setText("Matrícula: " + alumno.getMatricula());
        holder.tvEmail.setText(alumno.getEmail());

        // Obtener nombre del grupo (Simplificación: asumiendo que los grupos ya están cargados o se consultan)
        // Para mayor eficiencia, se podrían pasar los grupos en un Map
        holder.chipGrupo.setText("Grupo ID: " + alumno.getGrupoId());

        holder.btnEditar.setOnClickListener(v -> listener.onEditar(alumno));
        holder.btnEliminar.setOnClickListener(v -> listener.onEliminar(alumno));
    }

    @Override
    public int getItemCount() {
        return listaFiltrada.size();
    }

    public void filtrar(String query) {
        listaFiltrada.clear();
        if (query.isEmpty()) {
            listaFiltrada.addAll(listaOriginal);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Alumno alumno : listaOriginal) {
                if (alumno.getNombre().toLowerCase().contains(lowerCaseQuery) ||
                        alumno.getMatricula().toLowerCase().contains(lowerCaseQuery)) {
                    listaFiltrada.add(alumno);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvMatricula, tvEmail;
        Chip chipGrupo;
        ImageButton btnEditar, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_nombre_alumno_item);
            tvMatricula = itemView.findViewById(R.id.tv_matricula_item);
            tvEmail = itemView.findViewById(R.id.tv_email_item);
            chipGrupo = itemView.findViewById(R.id.chip_grupo_item);
            btnEditar = itemView.findViewById(R.id.btn_editar_alumno);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar_alumno);
        }
    }
}