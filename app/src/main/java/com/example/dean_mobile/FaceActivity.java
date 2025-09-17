package com.example.dean_mobile;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FaceActivity extends AppCompatActivity {
    ImageButton btnImage;
    EditText etName;
    Button btnUpload;
    RecyclerView rvUsers;
    FacesAdapter adapter;

    private Uri imageUri;

    // For gallery
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    btnImage.setImageURI(uri);
                }
            });

    // For camera
    private final ActivityResultLauncher<Uri> takePhoto =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && imageUri != null) {
                    btnImage.setImageURI(imageUri);
                }
            });

    // For requesting permissions
    private final ActivityResultLauncher<String> requestPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);

        btnImage = findViewById(R.id.btnImage);
        etName = findViewById(R.id.etName);
        btnUpload = findViewById(R.id.btnUpload);
        rvUsers = findViewById(R.id.rvUsers);

        btnImage.setOnClickListener(v -> showImagePickerDialog());


        rvUsers.setLayoutManager(new GridLayoutManager(this, 2));
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("db")
                .document("test")
                .collection("faces")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        String image = doc.getString("image");
                        System.out.println(name + " - " + image);
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error: " + e.getMessage());
                });

        Query query = db.collection("db")
                .document("test")
                .collection("faces");

        FirestoreRecyclerOptions<Face> options =
                new FirestoreRecyclerOptions.Builder<Face>()
                        .setQuery(query, Face.class)
                        .build();

        adapter = new FacesAdapter(options);
        rvUsers.setAdapter(adapter);

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

    private void showImagePickerDialog() {
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Option")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) { // Camera
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED) {
                            openCamera();
                        } else {
                            requestPermission.launch(Manifest.permission.CAMERA);
                        }
                    } else { // Gallery
                        pickImage.launch("image/*");
                    }
                })
                .show();
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        takePhoto.launch(imageUri);
    }
}
