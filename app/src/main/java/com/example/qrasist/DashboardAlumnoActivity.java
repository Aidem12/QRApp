package com.example.qrasist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.qrasist.fragments.MiQrFragment;
import com.example.qrasist.fragments.MisTareasFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardAlumnoActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_alumno);

        prefs = getSharedPreferences("QRAsistPrefs", MODE_PRIVATE);

        // 1. Verificar sesión
        if (!"ALUMNO".equals(prefs.getString("user_rol", ""))) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // 2. Configurar Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar_alumno);
        setSupportActionBar(toolbar);

        // 3. Configurar Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_alumno);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_mi_qr) {
                selectedFragment = new MiQrFragment();
            } else if (id == R.id.nav_mis_tareas) {
                selectedFragment = new MisTareasFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_alumno, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        // 4. Cargar fragment inicial
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_mi_qr);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard_maestro, menu); // Reutilizamos el menú de logout
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            cerrarSesion();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cerrarSesion() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}