package com.example.qrasist.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrasist.R;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.Grupo;
import com.example.qrasist.models.Tarea;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.ViewHolder> {

    public interface TareaListener {
        void onEditar(Tarea tarea);
        void onEliminar(Tarea tarea);
    }

    private Context context;
    private List<Tarea> lista;
    private DBHelper db;
    private TareaListener listener;

    public TareaAdapter(Context context, List<Tarea> lista, DBHelper db, TareaListener listener) {
        this.context = context;
        this.lista = lista;
        this.db = db;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tarea, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tarea tarea = lista.get(position);
        holder.tvTitulo.setText(tarea.getTitulo());
        holder.tvDescripcion.setText(tarea.getDescripcion());
        holder.chipFecha.setText("Límite: " + tarea.getFechaLimite());

        // Obtener nombre del grupo
        List<Grupo> grupos = db.obtenerGrupos();
        for (Grupo g : grupos) {
            if (g.getId() == tarea.getGrupoId()) {
                holder.chipGrupo.setText(g.getNombre());
                break;
            }
        }

        // Calcular estado
        String fechaHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        if (tarea.getFechaLimite().compareTo(fechaHoy) >= 0) {
            holder.chipEstado.setText("Activa");
            holder.chipEstado.setChipBackgroundColorResource(R.color.tarea_entregada);
        } else {
            holder.chipEstado.setText("Vencida");
            holder.chipEstado.setChipBackgroundColorResource(R.color.tarea_no_entregada);
        }
        holder.chipEstado.setTextColor(ContextCompat.getColor(context, android.R.color.white));

        holder.btnEditar.setOnClickListener(v -> listener.onEditar(tarea));
        holder.btnEliminar.setOnClickListener(v -> listener.onEliminar(tarea));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescripcion;
        Chip chipEstado, chipGrupo, chipFecha;
        ImageButton btnEditar, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tv_titulo_tarea);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion_tarea);
            chipEstado = itemView.findViewById(R.id.chip_estado_tarea);
            chipGrupo = itemView.findViewById(R.id.chip_grupo_tarea);
            chipFecha = itemView.findViewById(R.id.chip_fecha_limite_tarea);
            btnEditar = itemView.findViewById(R.id.btn_editar_tarea);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar_tarea);
        }
    }
}