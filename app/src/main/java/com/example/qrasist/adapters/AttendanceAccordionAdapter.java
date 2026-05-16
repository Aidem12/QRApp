package com.example.qrasist.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrasist.R;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.Alumno;
import com.example.qrasist.models.Asistencia;

import java.util.ArrayList;
import java.util.List;

public class AttendanceAccordionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CHILD = 1;

    private List<Object> items = new ArrayList<>();
    private final DBHelper db;

    public AttendanceAccordionAdapter(DBHelper db) {
        this.db = db;
    }

    public void setData(List<Object> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof String) ? TYPE_HEADER : TYPE_CHILD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_header, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_child, parent, false);
            return new ItemViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvTitle.setText((String) items.get(position));
        } else {
            Asistencia a = (Asistencia) items.get(position);
            ItemViewHolder vh = (ItemViewHolder) holder;
            
            Alumno al = db.obtenerAlumnoPorId(a.getAlumnoId());
            vh.tvNombre.setText(al != null ? al.getNombre() : "Desconocido");

            switch (a.getEstado()) {
                case "Presente":
                    vh.tvIcono.setText("[✓] Asistió");
                    vh.tvIcono.setTextColor(Color.parseColor("#388E3C"));
                    break;
                case "Ausente":
                    vh.tvIcono.setText("[✗] Falta");
                    vh.tvIcono.setTextColor(Color.RED);
                    break;
                case "Retardo":
                    vh.tvIcono.setText("[⚠] Retardo");
                    vh.tvIcono.setTextColor(Color.parseColor("#F57C00"));
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        HeaderViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_header_title);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvIcono;
        ItemViewHolder(View v) {
            super(v);
            tvNombre = v.findViewById(R.id.tv_alumno_nombre);
            tvIcono = v.findViewById(R.id.tv_estado_icono);
        }
    }
}
