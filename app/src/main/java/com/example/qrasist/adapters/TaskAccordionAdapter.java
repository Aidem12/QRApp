package com.example.qrasist.adapters;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrasist.R;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.Alumno;
import com.example.qrasist.models.Tarea;
import com.example.qrasist.models.TareaAlumno;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TaskAccordionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CHILD = 1;

    private List<Object> allItems = new ArrayList<>();
    private List<Object> visibleItems = new ArrayList<>();
    private Set<Integer> expandedTaskIds = new HashSet<>();
    
    private final Map<Tarea, List<TareaAlumno>> dataMap;
    private final DBHelper db;
    private final Context context;

    public TaskAccordionAdapter(Context context, Map<Tarea, List<TareaAlumno>> data, DBHelper db) {
        this.context = context;
        this.db = db;
        this.dataMap = data;
        actualizarListaVisible();
    }

    private void actualizarListaVisible() {
        visibleItems.clear();
        for (Tarea task : dataMap.keySet()) {
            visibleItems.add(task);
            if (expandedTaskIds.contains(task.getId())) {
                List<TareaAlumno> children = dataMap.get(task);
                if (children != null) visibleItems.addAll(children);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return (visibleItems.get(position) instanceof Tarea) ? TYPE_HEADER : TYPE_CHILD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_HEADER) {
            return new HeaderVH(inflater.inflate(R.layout.item_task_header, parent, false));
        }
        return new ChildVH(inflater.inflate(R.layout.item_task_student_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = visibleItems.get(position);
        if (holder instanceof HeaderVH) {
            Tarea t = (Tarea) item;
            HeaderVH h = (HeaderVH) holder;
            h.tv.setText(t.getTitulo());
            h.itemView.setOnClickListener(v -> {
                if (expandedTaskIds.contains(t.getId())) expandedTaskIds.remove(t.getId());
                else expandedTaskIds.add(t.getId());
                actualizarListaVisible();
            });
        } else {
            TareaAlumno ta = (TareaAlumno) item;
            ChildVH vh = (ChildVH) holder;
            Alumno al = db.obtenerAlumnoPorId(ta.getAlumnoId());
            vh.tvNombre.setText(al != null ? al.getNombre() : "Alumno Desconocido");

            String status = ta.getEstado();
            String label = status;
            int color = R.color.tarea_pendiente;

            // Configurar Panel de Revisión
            boolean entregada = status.equals("Entregada") || status.equals("Revisada") || status.equals("Rechazada");
            vh.layoutReview.setVisibility(entregada ? View.VISIBLE : View.GONE);

            if (status.equals("Pendiente")) {
                label = "Pendiente";
                color = R.color.tarea_pendiente;
            } else if (status.equals("Entregada")) {
                label = "Por Revisar";
                color = R.color.tarea_pendiente;
            } else if (status.equals("Revisada")) {
                label = "Aceptada";
                color = R.color.tarea_entregada;
            } else if (status.equals("Rechazada")) {
                label = "Incorrecta";
                color = R.color.tarea_no_entregada;
            }

            vh.chip.setText(label);
            vh.chip.setChipBackgroundColorResource(color);
            vh.chip.setTextColor(Color.WHITE);

            // Cargar Imagen
            if (entregada && ta.getComentario() != null && !ta.getComentario().isEmpty()) {
                try {
                    vh.ivPreview.setImageURI(Uri.parse(ta.getComentario()));
                } catch (Exception e) {
                    vh.ivPreview.setImageResource(android.R.drawable.ic_menu_report_image);
                }
            }

            // Botones de Acción del Maestro
            vh.btnAccept.setOnClickListener(v -> {
                db.actualizarEstadoTarea(ta.getId(), "Revisada", ta.getFechaEntrega(), ta.getComentario());
                ta.setEstado("Revisada");
                notifyItemChanged(position);
                Toast.makeText(context, "Tarea Aceptada", Toast.LENGTH_SHORT).show();
            });

            vh.btnReject.setOnClickListener(v -> {
                db.actualizarEstadoTarea(ta.getId(), "Rechazada", ta.getFechaEntrega(), ta.getComentario());
                ta.setEstado("Rechazada");
                notifyItemChanged(position);
                Toast.makeText(context, "Marcada como Incorrecta", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public int getItemCount() { return visibleItems.size(); }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tv;
        HeaderVH(View v) { super(v); tv = v.findViewById(R.id.tv_task_header_title); }
    }

    static class ChildVH extends RecyclerView.ViewHolder {
        TextView tvNombre;
        Chip chip;
        LinearLayout layoutReview;
        ImageView ivPreview;
        Button btnAccept, btnReject;
        ChildVH(View v) {
            super(v);
            tvNombre = v.findViewById(R.id.tv_student_name);
            chip = v.findViewById(R.id.chip_delivery_status);
            layoutReview = v.findViewById(R.id.layout_teacher_review);
            ivPreview = v.findViewById(R.id.iv_task_evidence_preview);
            btnAccept = v.findViewById(R.id.btn_accept_task);
            btnReject = v.findViewById(R.id.btn_reject_task);
        }
    }
}