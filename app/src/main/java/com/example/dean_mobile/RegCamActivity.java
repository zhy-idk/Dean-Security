package com.example.dean_mobile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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
    Button btnUpload, btnGet;
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
        btnGet = findViewById(R.id.btnGet);
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

        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Query querycam = FirebaseDatabase.getInstance().getReference().child("ip");
                querycam.get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        GetCam alert = snapshot.getValue(GetCam.class);
                        if (alert != null) {
                            String message = alert.getPublic_();
                            Log.d("Firebase Data", "Public message: " + message);
                            if (!message.isEmpty()){
                                AlertDialog.Builder builder = new AlertDialog.Builder(RegCamActivity.this);
                                builder.setTitle("Detected Cam")
                                        .setMessage(message)  // Set the message from RegAlert
                                        .setPositiveButton("Copy", (dialog, which) -> {
                                            // Update the Firebase database to set the message to ""
                                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                            ClipData clip = ClipData.newPlainText("Copied Message", message);
                                            clipboard.setPrimaryClip(clip);
                                            Toast.makeText(RegCamActivity.this, "Message copied to clipboard!", Toast.LENGTH_SHORT).show();
                                            FirebaseDatabase.getInstance().getReference().child("ip").setValue(new GetCam("", ""));
                                            dialog.dismiss();
                                        })
                                        .show();
                            } else {
                                Toast.makeText(RegCamActivity.this, "No cam detected", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }});
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
