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

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FaceActivity extends AppCompatActivity {
    ImageButton btnImage;
    EditText etName;
    Button btnUpload;
    RecyclerView rvUsers;
    FacesAdapter adapter;

    private Uri imageUri;
    private FirebaseStorage storage;
    private FirebaseFirestore db;

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

        // Initialize Firebase
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        btnImage = findViewById(R.id.btnImage);
        etName = findViewById(R.id.etName);
        btnUpload = findViewById(R.id.btnUpload);
        rvUsers = findViewById(R.id.rvUsers);

        btnImage.setOnClickListener(v -> showImagePickerDialog());

        // Add upload button functionality
        btnUpload.setOnClickListener(v -> uploadImage());

        rvUsers.setLayoutManager(new GridLayoutManager(this, 2));

        Query query = db.collection("faces");

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

    private void uploadImage() {
        String name = etName.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading message
        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();
        btnUpload.setEnabled(false); // Disable button during upload

        // Create storage reference with custom name
        StorageReference imageRef = storage.getReference()
                .child("faces/" + name + ".jpg");

        // Upload image to Storage first
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        // Save to Firestore with auto-generated document ID
                        saveFaceToFirestore(name, downloadUri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnUpload.setEnabled(true); // Re-enable button
                });
    }

    private void saveFaceToFirestore(String name, String imageUrl) {
        Face face = new Face(name, imageUrl);

        // Use add() instead of document().set() to auto-generate document ID
        db.collection("faces")
                .add(face)  // This creates a random document ID
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Face registered successfully!", Toast.LENGTH_SHORT).show();
                    // Optional: Log the auto-generated document ID
                    // Log.d("FaceActivity", "Document added with ID: " + documentReference.getId());

                    // Clear form
                    etName.setText("");
                    btnImage.setImageResource(R.drawable.placeholder); // Set default image
                    imageUri = null;
                    btnUpload.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnUpload.setEnabled(true);
                });
    }
}