package com.example.dean_mobile;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class UserFragment extends Fragment {
    Button btnAuthorize, btnFace, btnCam, btnTheme, btnAbout;

    public UserFragment() {
        // Required empty public constructor
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
        btnAuthorize = view.findViewById(R.id.btnAuthorize);
        btnFace = view.findViewById(R.id.btnFace);
        btnCam = view.findViewById(R.id.btnCam);
        btnTheme = view.findViewById(R.id.btnTheme);
        btnAbout = view.findViewById(R.id.btnAbout);

        // Set click listeners
        btnAuthorize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AuthorizeActivity.class);
                startActivity(intent);
            }});

        btnFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FaceActivity.class);
                startActivity(intent);
            }});

        btnCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle button click
            }});

        btnTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }});

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle button click
            }});
    }
}