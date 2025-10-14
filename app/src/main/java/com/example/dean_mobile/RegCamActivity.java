package com.example.dean_mobile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class RegCamActivity extends AppCompatActivity {

    EditText etCamLink;
    Button btnUpload;
    RecyclerView rvCamList;
    CamlistAdapter adapter;
    DatabaseReference cameraRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reg_cam);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        etCamLink = findViewById(R.id.etCamLink);
        btnUpload = findViewById(R.id.btnUpload);
        rvCamList = findViewById(R.id.rvCamList);

        rvCamList.setItemAnimator(null);
        rvCamList.setLayoutManager(new LinearLayoutManager(this));

        // Reference to "cameras" node
        cameraRef = FirebaseDatabase.getInstance().getReference().child("cameras");

        // Firebase query setup
        Query query = cameraRef;
        FirebaseRecyclerOptions<Camera> options =
                new FirebaseRecyclerOptions.Builder<Camera>()
                        .setQuery(query, Camera.class)
                        .build();

        adapter = new CamlistAdapter(options);
        rvCamList.setAdapter(adapter);

        // Upload button click listener
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadCameraLink();
            }
        });
    }

    private void uploadCameraLink() {
        String link = etCamLink.getText().toString().trim();
        if (link.isEmpty()) {
            Toast.makeText(this, "Please enter a link", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create new camera object
        Camera camera = new Camera(link, 0, 0);

        // Push to Firebase
        cameraRef.push().setValue(camera)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Camera uploaded successfully", Toast.LENGTH_SHORT).show();
                    etCamLink.setText(""); // clear input
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
