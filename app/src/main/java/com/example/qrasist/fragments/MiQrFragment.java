package com.example.qrasist.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrasist.R;
import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.Alumno;
import com.example.qrasist.models.Asistencia;
import com.google.android.material.chip.Chip;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class MiQrFragment extends Fragment {

    private DBHelper db;
    private SharedPreferences prefs;
    private int alumnoId;
    private TextView tvNombreAlumno, tvMatriculaAlumno;
    private ImageView ivCodigoQr;
    private Button btnCompartirQr;
    private RecyclerView rvHistorialAlumno;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mi_qr, container, false);

        db = new DBHelper(getContext());
        prefs = requireActivity().getSharedPreferences("QRAsistPrefs", Context.MODE_PRIVATE);
        alumnoId = prefs.getInt("user_id", -1);

        tvNombreAlumno = view.findViewById(R.id.tv_nombre_alumno_qr);
        tvMatriculaAlumno = view.findViewById(R.id.tv_matricula_alumno_qr);
        ivCodigoQr = view.findViewById(R.id.iv_codigo_qr);
        btnCompartirQr = view.findViewById(R.id.btn_compartir_qr);
        rvHistorialAlumno = view.findViewById(R.id.rv_historial_alumno);

        rvHistorialAlumno.setLayoutManager(new LinearLayoutManager(getContext()));

        cargarDatosAlumno();

        btnCompartirQr.setOnClickListener(v -> compartirQr());

        return view;
    }

    private void cargarDatosAlumno() {
        Alumno alumno = db.obtenerAlumnoPorId(alumnoId);
        if (alumno != null) {
            tvNombreAlumno.setText(alumno.getNombre());
            tvMatriculaAlumno.setText("Matrícula: " + alumno.getMatricula());

            try {
                MultiFormatWriter writer = new MultiFormatWriter();
                BitMatrix bitMatrix = writer.encode(alumno.getMatricula(), BarcodeFormat.QR_CODE, 500, 500);
                BarcodeEncoder encoder = new BarcodeEncoder();
                Bitmap bitmap = encoder.createBitmap(bitMatrix);
                ivCodigoQr.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<Asistencia> historial = db.obtenerAsistenciasPorAlumno(alumnoId);
            rvHistorialAlumno.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                @NonNull
                @Override
                public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View v = LayoutInflater.from(getContext()).inflate(R.layout.item_asistencia_alumno, parent, false);
                    return new RecyclerView.ViewHolder(v) {};
                }

                @Override
                public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                    Asistencia a = historial.get(position);
                    TextView tvFecha = holder.itemView.findViewById(R.id.tv_fecha_asistencia_alumno);
                    TextView tvObs = holder.itemView.findViewById(R.id.tv_observacion_asistencia_alumno);
                    Chip chipEstado = holder.itemView.findViewById(R.id.chip_estado_asistencia_alumno);
                    View viewColor = holder.itemView.findViewById(R.id.view_color_asistencia);

                    tvFecha.setText(a.getFecha());
                    if (a.getObservacion() != null && !a.getObservacion().isEmpty()) {
                        tvObs.setVisibility(View.VISIBLE);
                        tvObs.setText(a.getObservacion());
                    } else {
                        tvObs.setVisibility(View.GONE);
                    }

                    int color;
                    switch (a.getEstado()) {
                        case "Presente": color = ContextCompat.getColor(getContext(), R.color.estado_presente); break;
                        case "Ausente": color = ContextCompat.getColor(getContext(), R.color.estado_ausente); break;
                        case "Retardo": color = ContextCompat.getColor(getContext(), R.color.estado_retardo); break;
                        default: color = ContextCompat.getColor(getContext(), R.color.on_surface_variant); break;
                    }
                    viewColor.setBackgroundColor(color);
                    chipEstado.setText(a.getEstado());
                    chipEstado.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(color));
                    chipEstado.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
                }

                @Override
                public int getItemCount() {
                    return historial.size();
                }
            });
        }
    }

    private void compartirQr() {
        if (ivCodigoQr.getDrawable() == null) return;
        Bitmap bitmap = ((BitmapDrawable) ivCodigoQr.getDrawable()).getBitmap();
        try {
            File cachePath = new File(requireContext().getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "alumno_qr.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            Uri contentUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", file);
            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(contentUri, requireContext().getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, "Compartir QR"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al compartir QR", Toast.LENGTH_SHORT).show();
        }
    }
}