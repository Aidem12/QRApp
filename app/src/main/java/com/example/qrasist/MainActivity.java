package com.example.qrasist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.qrasist.database.DBHelper;
import com.example.qrasist.models.Alumno;
import com.example.qrasist.models.Maestro;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private DBHelper db;
    private RadioGroup rgRol;
    private TextInputEditText etUsuario, etPassword;
    private TextInputLayout tilUsuario, tilPassword;
    private Button btnLogin;
    private TextView tvForgot;
    private String rolSeleccionado = "MAESTRO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DBHelper(this);

        // Referenciar vistas
        rgRol = findViewById(R.id.rg_rol);
        etUsuario = findViewById(R.id.et_usuario);
        etPassword = findViewById(R.id.et_password);
        tilUsuario = findViewById(R.id.til_usuario);
        tilPassword = findViewById(R.id.til_password);
        btnLogin = findViewById(R.id.btn_login);
        tvForgot = findViewById(R.id.tv_forgot);

        setupRolSelector();
        setupLoginButton();
    }

    private void setupRolSelector() {
        rgRol.setOnCheckedChangeListener((group, checkedId) -> {
            int color;
            if (checkedId == R.id.rb_maestro) {
                rolSeleccionado = "MAESTRO";
                color = ContextCompat.getColor(this, R.color.maestro_primary);
            } else {
                rolSeleccionado = "ALUMNO";
                color = ContextCompat.getColor(this, R.color.alumno_primary);
            }
            
            // Cambiar colores de los inputs y botón
            ColorStateList colorStateList = ColorStateList.valueOf(color);
            tilUsuario.setBoxStrokeColor(color);
            tilUsuario.setHintTextColor(colorStateList);
            tilPassword.setBoxStrokeColor(color);
            tilPassword.setHintTextColor(colorStateList);
            btnLogin.setBackgroundColor(color);
            tvForgot.setTextColor(color);
        });
    }

    private void setupLoginButton() {
        btnLogin.setOnClickListener(v -> {
            String usuario = etUsuario.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (usuario.isEmpty() || password.isEmpty()) {
                Snackbar.make(v, R.string.error_campos_vacios, Snackbar.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("QRAsistPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            if (rolSeleccionado.equals("MAESTRO")) {
                Maestro maestro = db.loginMaestro(usuario, password);
                if (maestro != null) {
                    editor.putInt("user_id", maestro.getId());
                    editor.putString("user_nombre", maestro.getNombre());
                    editor.putString("user_rol", "MAESTRO");
                    editor.apply();

                    startActivity(new Intent(this, DashboardMaestroActivity.class));
                    finish();
                } else {
                    Snackbar.make(v, R.string.error_credenciales, Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Alumno alumno = db.obtenerAlumnoPorMatricula(usuario);
                if (alumno != null && alumno.getMatricula().equals(password)) {
                    editor.putInt("user_id", alumno.getId());
                    editor.putString("user_nombre", alumno.getNombre());
                    editor.putString("user_rol", "ALUMNO");
                    editor.apply();

                    startActivity(new Intent(this, DashboardAlumnoActivity.class));
                    finish();
                } else {
                    Snackbar.make(v, R.string.error_credenciales, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }
}