package com.example.dean_mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class UserFragment extends Fragment {
    Button btnFace, btnCam, btnCloud, btnTheme, btnAutoUpload, btnAbout, btnLogout;
    SharedPreferences sharedPreferences;

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
        sharedPreferences = requireActivity().getSharedPreferences("AutoUpload", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    public void checkAutoUpload() {
        boolean autoUpload = sharedPreferences.getBoolean("autoUpload", false);
        btnAutoUpload.setText(autoUpload ? "Auto Upload: Yes" : "Auto Upload: No");
    }

    public void checkTheme() {
        boolean isDarkTheme = isDarkTheme();
        btnTheme.setText(isDarkTheme ? "Change to Light Theme" : "Change to Dark Theme");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialization
        btnFace = view.findViewById(R.id.btnFace);
        btnCam = view.findViewById(R.id.btnCamera);
        btnCloud = view.findViewById(R.id.btnContent);
        btnTheme = view.findViewById(R.id.btnTheme);
        btnAutoUpload = view.findViewById(R.id.btnAutoUpload);
        btnAbout = view.findViewById(R.id.btnAbout);
        btnLogout = view.findViewById(R.id.btnLogout);

        checkTheme();
        checkAutoUpload();

        btnFace.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FaceActivity.class);
            startActivity(intent);
        });

        btnCam.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RegCamActivity.class);
            startActivity(intent);

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

        btnAutoUpload.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("autoUpload", !sharedPreferences.getBoolean("autoUpload", false));
            editor.apply();

            checkAutoUpload();
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