package com.example.dean_mobile;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class UserFragment extends Fragment {
    Button btnFace, btnCam, btnCloud, btnTheme, btnAbout, btnLogout;

    public UserFragment() {
        // Required empty public constructor
    }

    private boolean isDarkTheme() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialization
        btnFace = view.findViewById(R.id.btnFace);
        btnCam = view.findViewById(R.id.btnCamera);
        btnCloud = view.findViewById(R.id.btnContent);
        btnTheme = view.findViewById(R.id.btnTheme);
        btnAbout = view.findViewById(R.id.btnAbout);
        btnLogout = view.findViewById(R.id.btnLogout);

        boolean isDarkTheme = isDarkTheme();
        btnTheme.setText(isDarkTheme ? "Change to Light Theme" : "Change to Dark Theme");

        btnFace.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FaceActivity.class);
            startActivity(intent);
        });

        btnCam.setOnClickListener(v -> {
            // Handle button click

        });

        btnCloud.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CloudContentActivity.class);
            startActivity(intent);
        });

        btnTheme.setOnClickListener(v -> {
            boolean currentIsDark = isDarkTheme();
            if (currentIsDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });

        btnAbout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AboutActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setMessage("Are you sure you want to continue?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Handle No
                    })
                    .show();
        });
    }
}