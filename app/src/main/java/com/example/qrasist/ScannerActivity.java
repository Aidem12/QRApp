package com.example.qrasist;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.Alumno;
import com.example.qrasist.models.Asistencia;
import com.example.qrasist.sync.SyncManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScannerActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    
    private DecoratedBarcodeView barcodeScanner;
    private DBHelper db;
    private SharedPreferences prefs;
    private int maestroId;
    private Alumno alumnoEscaneado = null;
    private boolean escaneando = true;
    
    private View layoutCamara, layoutResultado;
    private TextView tvNombreAlumno, tvMatriculaAlumno, tvGrupoAlumno;
    private Spinner spinnerEstado;
    private TextInputEditText etObservacion;
    private Button btnCancelarScan, btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar_scanner);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        db = new DBHelper(this);
        prefs = getSharedPreferences("QRAsistPrefs", MODE_PRIVATE);
        maestroId = prefs.getInt("user_id", -1);

        // Referenciar views
        layoutCamara = findViewById(R.id.layout_camara);
        layoutResultado = findViewById(R.id.layout_resultado);
        barcodeScanner = findViewById(R.id.barcode_scanner);
        tvNombreAlumno = findViewById(R.id.tv_nombre_alumno);
        tvMatriculaAlumno = findViewById(R.id.tv_matricula_alumno);
        tvGrupoAlumno = findViewById(R.id.tv_grupo_alumno);
        spinnerEstado = findViewById(R.id.spinner_estado);
        etObservacion = findViewById(R.id.et_observacion);
        btnCancelarScan = findViewById(R.id.btn_cancelar_scan);
        btnRegistrar = findViewById(R.id.btn_registrar);

        // Configurar Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.estados_asistencia, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapter);

        btnCancelarScan.setOnClickListener(v -> reiniciarScanner());
        btnRegistrar.setOnClickListener(v -> registrarAsistencia());

        // Permisos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            configurarScanner();
        }
    }

    private void configurarScanner() {
        barcodeScanner.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (!escaneando || result.getText() == null) return;
                escaneando = false;
                procesarQR(result.getText());
            }
        });
    }

    private void procesarQR(String codigo) {
        Alumno alumno = db.obtenerAlumnoPorMatricula(codigo);
        if (alumno == null) {
            Snackbar.make(layoutCamara, R.string.error_qr_invalido, Snackbar.LENGTH_LONG).show();
            escaneando = true;
            return;
        }

        alumnoEscaneado = alumno;
        String fechaHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        if (db.yaRegistradoHoy(alumno.getId(), fechaHoy)) {
            Snackbar.make(layoutCamara, R.string.error_ya_registrado, Snackbar.LENGTH_LONG).show();
            escaneando = true;
            return;
        }

        mostrarResultado(alumno);
    }

    private void mostrarResultado(Alumno alumno) {
        layoutCamara.setVisibility(View.GONE);
        layoutResultado.setVisibility(View.VISIBLE);
        barcodeScanner.pause();

        tvNombreAlumno.setText(alumno.getNombre());
        tvMatriculaAlumno.setText("Matrícula: " + alumno.getMatricula());
        
        // Obtener nombre del grupo (simplificado para el ejemplo)
        tvGrupoAlumno.setText("Grupo ID: " + alumno.getGrupoId());
        
        spinnerEstado.setSelection(0); // Presente
    }

    private void registrarAsistencia() {
        if (alumnoEscaneado == null) return;

        String estado = spinnerEstado.getSelectedItem().toString();
        String observacion = etObservacion.getText().toString().trim();
        String fechaHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        long resultado = db.insertarAsistencia(alumnoEscaneado.getId(), maestroId, fechaHoy, estado, observacion);

        if (resultado > 0) {
            // INYECCIÓN FIRESTORE: Gatillo inmediato
            SyncManager syncManager = new SyncManager(this);
            Asistencia a = new Asistencia((int)resultado, alumnoEscaneado.getId(), maestroId, fechaHoy, estado, observacion);
            syncManager.syncAsistencia(a);

            Snackbar.make(layoutResultado, R.string.exito_asistencia, Snackbar.LENGTH_SHORT).show();
            new Handler().postDelayed(this::reiniciarScanner, 1500);
        } else {
            Snackbar.make(layoutResultado, "Error al registrar, intenta de nuevo", Snackbar.LENGTH_SHORT).show();
            escaneando = true;
        }
    }

    private void reiniciarScanner() {
        alumnoEscaneado = null;
        escaneando = true;
        etObservacion.setText("");
        layoutResultado.setVisibility(View.GONE);
        layoutCamara.setVisibility(View.VISIBLE);
        barcodeScanner.resume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                configurarScanner();
            } else {
                Snackbar.make(layoutCamara, "Se necesita permiso de cámara", Snackbar.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScanner.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScanner.pause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScanner.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}