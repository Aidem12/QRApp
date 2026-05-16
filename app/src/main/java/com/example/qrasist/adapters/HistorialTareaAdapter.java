package com.example.qrasist.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrasist.R;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.Alumno;
import com.example.qrasist.models.Tarea;
import com.example.qrasist.models.TareaAlumno;
import com.google.android.material.chip.Chip;

import java.util.List;

public class HistorialTareaAdapter extends RecyclerView.Adapter<HistorialTareaAdapter.ViewHolder> {

    public interface HistorialTareaListener {
        void onMarcarEntregada(TareaAlumno tareaAlumno);
    }

    private Context context;
    private List<TareaAlumno> lista;
    private DBHelper db;
    private HistorialTareaListener listener;

    public HistorialTareaAdapter(Context context, List<TareaAlumno> lista, DBHelper db, HistorialTareaListener listener) {
        this.context = context;
        this.lista = lista;
        this.db = db;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_historial_tarea, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TareaAlumno ta = lista.get(position);
        Tarea tarea = db.obtenerTareaPorId(ta.getTareaId());
        Alumno alumno = db.obtenerAlumnoPorId(ta.getAlumnoId());

        if (tarea != null) {
            holder.tvTitulo.setText(tarea.getTitulo());
            holder.tvFechaLimite.setText("Límite: " + tarea.getFechaLimite());
        }

        if (alumno != null) {
            holder.tvNombreAlumno.setText(alumno.getNombre());
        }

        String estado = ta.getEstado();
        holder.chipEstado.setText(estado);

        int color;
        switch (estado) {
            case "Entregada":
                color = ContextCompat.getColor(context, R.color.tarea_entregada);
                holder.btnMarcar.setVisibility(View.GONE);
                break;
            case "No entregada":
                color = ContextCompat.getColor(context, R.color.tarea_no_entregada);
                holder.btnMarcar.setVisibility(View.GONE);
                break;
            default: // Pendiente
                color = ContextCompat.getColor(context, R.color.tarea_pendiente);
                holder.btnMarcar.setVisibility(View.VISIBLE);
                break;
        }

        holder.chipEstado.setChipBackgroundColor(ColorStateList.valueOf(color));
        holder.chipEstado.setTextColor(ContextCompat.getColor(context, android.R.color.white));

        holder.btnMarcar.setOnClickListener(v -> listener.onMarcarEntregada(ta));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvNombreAlumno, tvFechaLimite;
        Chip chipEstado;
        Button btnMarcar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tv_titulo_tarea_hist);
            tvNombreAlumno = itemView.findViewById(R.id.tv_nombre_alumno_tarea_hist);
            tvFechaLimite = itemView.findViewById(R.id.tv_fecha_limite_hist);
            chipEstado = itemView.findViewById(R.id.chip_estado_tarea_hist);
            btnMarcar = itemView.findViewById(R.id.btn_marcar_entregada);
        }
    }
}