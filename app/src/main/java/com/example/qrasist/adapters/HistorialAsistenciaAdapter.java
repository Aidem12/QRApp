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
import com.example.qrasist.models.Alumno;
import com.example.qrasist.models.Asistencia;
import com.google.android.material.chip.Chip;

import java.util.List;

public class HistorialAsistenciaAdapter extends RecyclerView.Adapter<HistorialAsistenciaAdapter.ViewHolder> {

    public interface HistorialAsistenciaListener {
        void onEditar(Asistencia asistencia);
        void onEliminar(Asistencia asistencia);
    }

    private Context context;
    private List<Asistencia> lista;
    private DBHelper db;
    private HistorialAsistenciaListener listener;

    public HistorialAsistenciaAdapter(Context context, List<Asistencia> lista, DBHelper db, HistorialAsistenciaListener listener) {
        this.context = context;
        this.lista = lista;
        this.db = db;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_historial_asistencia, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Asistencia asistencia = lista.get(position);
        Alumno alumno = db.obtenerAlumnoPorId(asistencia.getAlumnoId());

        if (alumno != null) {
            holder.tvNombre.setText(alumno.getNombre());
        }
        holder.tvFecha.setText(asistencia.getFecha());

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
        holder.chipEstado.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(color));
        holder.chipEstado.setTextColor(ContextCompat.getColor(context, android.R.color.white));

        holder.btnEditar.setOnClickListener(v -> listener.onEditar(asistencia));
        holder.btnEliminar.setOnClickListener(v -> listener.onEliminar(asistencia));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View viewEstadoColor;
        TextView tvNombre, tvFecha, tvObservacion;
        Chip chipEstado;
        ImageButton btnEditar, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewEstadoColor = itemView.findViewById(R.id.view_color_estado);
            tvNombre = itemView.findViewById(R.id.tv_nombre_alumno_hist);
            tvFecha = itemView.findViewById(R.id.tv_fecha_hist);
            tvObservacion = itemView.findViewById(R.id.tv_observacion_hist);
            chipEstado = itemView.findViewById(R.id.chip_estado_hist);
            btnEditar = itemView.findViewById(R.id.btn_editar_asistencia);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar_asistencia);
        }
    }
}