package com.example.qrasist.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrasist.R;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.Alumno;
import com.example.qrasist.models.Asistencia;
import com.google.android.material.chip.Chip;

import java.util.List;

public class AsistenciaRecienteAdapter extends RecyclerView.Adapter<AsistenciaRecienteAdapter.ViewHolder> {

    private Context context;
    private List<Asistencia> lista;
    private DBHelper db;

    public AsistenciaRecienteAdapter(Context context, List<Asistencia> lista, DBHelper db) {
        this.context = context;
        this.lista = lista;
        this.db = db;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_asistencia_reciente, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Asistencia asistencia = lista.get(position);
        Alumno alumno = db.obtenerAlumnoPorId(asistencia.getAlumnoId());

        if (alumno != null) {
            holder.tvNombreAlumno.setText(alumno.getNombre());
        }
        holder.tvFechaAsistencia.setText(asistencia.getFecha());

        if (asistencia.getObservacion() != null && !asistencia.getObservacion().isEmpty()) {
            holder.tvObservacion.setVisibility(View.VISIBLE);
            holder.tvObservacion.setText(asistencia.getObservacion());
        } else {
            holder.tvObservacion.setVisibility(View.GONE);
        }

        int color;
        String estado = asistencia.getEstado();
        holder.chipEstado.setText(estado);

        switch (estado) {
            case "Presente":
                color = ContextCompat.getColor(context, R.color.estado_presente);
                break;
            case "Ausente":
                color = ContextCompat.getColor(context, R.color.estado_ausente);
                break;
            case "Retardo":
                color = ContextCompat.getColor(context, R.color.estado_retardo);
                break;
            default:
                color = ContextCompat.getColor(context, R.color.on_surface_variant);
                break;
        }

        holder.viewEstadoColor.setBackgroundColor(color);
        holder.chipEstado.setChipBackgroundColorResource(android.R.color.transparent);
        holder.chipEstado.setChipStrokeColorResource(android.R.color.transparent);
        holder.chipEstado.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        
        // Forma rápida de setear color al Chip de M3
        holder.chipEstado.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(color));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View viewEstadoColor;
        TextView tvNombreAlumno, tvFechaAsistencia, tvObservacion;
        Chip chipEstado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewEstadoColor = itemView.findViewById(R.id.view_estado_color);
            tvNombreAlumno = itemView.findViewById(R.id.tv_nombre_alumno);
            tvFechaAsistencia = itemView.findViewById(R.id.tv_fecha_asistencia);
            tvObservacion = itemView.findViewById(R.id.tv_observacion);
            chipEstado = itemView.findViewById(R.id.chip_estado);
        }
    }
}