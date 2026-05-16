package com.example.qrasist.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.qrasist.R;
import com.example.qrasist.database.DBHelper;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MisTareasFragment extends Fragment {

    private DBHelper db;
    private int alumnoId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mis_tareas, container, false);

        db = new DBHelper(getContext());
        SharedPreferences prefs = requireActivity().getSharedPreferences("QRAsistPrefs", Context.MODE_PRIVATE);
        alumnoId = prefs.getInt("user_id", -1);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout_tareas_alumno);
        ViewPager2 viewPager = view.findViewById(R.id.view_pager_tareas_alumno);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0: return TareasEstadoFragment.newInstance("Pendiente", alumnoId);
                    case 1: return TareasEstadoFragment.newInstance("Entregada", alumnoId);
                    default: return TareasEstadoFragment.newInstance("No entregada", alumnoId);
                }
            }

            @Override
            public int getItemCount() {
                return 3;
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Pendientes");
                    int pendientes = db.contarTareasPendientes(alumnoId);
                    if (pendientes > 0) {
                        tab.getOrCreateBadge().setNumber(pendientes);
                    } else {
                        tab.removeBadge();
                    }
                    break;
                case 1: tab.setText("Entregadas"); break;
                case 2: tab.setText("No entregadas"); break;
            }
        }).attach();

        return view;
    }
}