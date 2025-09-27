package com.example.dean_mobile;

import static java.security.AccessController.getContext;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
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
import com.google.firebase.auth.FirebaseAuth;
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

        Query query = db.collection("faces").orderBy("name", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Face> options =
                new FirestoreRecyclerOptions.Builder<Face>()
                        .setQuery(query, Face.class)
                        .build();

        adapter = new FacesAdapter(options);
        adapter.setOnItemClickListener(new FacesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Face face, int position, String documentId) {
                // Create EditText for name input
                EditText editText = new EditText(FaceActivity.this);
                editText.setText(face.getName()); // Set current name
                editText.setHint("Enter name");
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                editText.setSingleLine(true);

                // Add some padding to the EditText
                int padding = (int) (16 * getResources().getDisplayMetrics().density); // 16dp in pixels
                editText.setPadding(padding, padding, padding, padding);

                new AlertDialog.Builder(FaceActivity.this)
                        .setTitle("Edit Face")
                        .setMessage("Enter new name or delete this face:")
                        .setView(editText)
                        .setPositiveButton("Save", (dialog, which) -> {
                            String newName = editText.getText().toString().trim();
                            if (!newName.isEmpty()) {
                                // Update the name in Firestore using documentId
                                updateFaceName(documentId, newName);
                            } else {
                                Toast.makeText(FaceActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Delete", (dialog, which) -> {
                            // Show delete confirmation using documentId
                            showDeleteConfirmation(face, documentId);
                        })
                        .setNeutralButton("Cancel", (dialog, which) -> {
                            // Dialog will close automatically
                        })
                        .show();
            }
        });
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

    // Method to update face name in Firestore
    private void updateFaceName(String documentId, String newName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // First, check if the new name already exists
        db.collection("faces")
                .whereEqualTo("name", newName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Name already exists
                        Toast.makeText(this, "Name '" + newName + "' already exists. Please choose a different name.", Toast.LENGTH_LONG).show();
                    } else {
                        // Name doesn't exist, proceed with update
                        proceedWithNameUpdate(documentId, newName);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking name availability: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method to proceed with name update after validation
    private void proceedWithNameUpdate(String documentId, String newName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // First get the current face data
        db.collection("faces").document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Face currentFace = documentSnapshot.toObject(Face.class);
                        String oldName = currentFace.getName();
                        String currentImageUrl = currentFace.getImage();

                        // Show loading
                        Toast.makeText(this, "Updating name and file...", Toast.LENGTH_SHORT).show();

                        // Create reference to old file
                        StorageReference oldFileRef = storage.getReferenceFromUrl(currentImageUrl);

                        // Download the image data first
                        oldFileRef.getBytes(Long.MAX_VALUE)
                                .addOnSuccessListener(imageData -> {
                                    // Create reference for new file with new name
                                    StorageReference newFileRef = storage.getReference()
                                            .child("faces/" + newName + ".jpg");

                                    // Upload with new name
                                    newFileRef.putBytes(imageData)
                                            .addOnSuccessListener(uploadTask -> {
                                                // Get new download URL
                                                newFileRef.getDownloadUrl()
                                                        .addOnSuccessListener(newDownloadUri -> {
                                                            // Update Firestore with new name and image URL
                                                            db.collection("faces").document(documentId)
                                                                    .update(
                                                                            "name", newName,
                                                                            "image", newDownloadUri.toString()
                                                                    )
                                                                    .addOnSuccessListener(aVoid -> {
                                                                        // Delete old file after successful update
                                                                        deleteOldFile(oldFileRef, oldName);
                                                                        Toast.makeText(this, "Name and file updated successfully", Toast.LENGTH_SHORT).show();
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Toast.makeText(this, "Error updating document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                        // Clean up new file if document update failed
                                                                        newFileRef.delete();
                                                                    });
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(this, "Error getting new download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            newFileRef.delete(); // Clean up
                                                        });
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Error uploading new file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error downloading current image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Document not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error retrieving current data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Helper method to delete old file
    private void deleteOldFile(StorageReference oldFileRef, String oldName) {
        oldFileRef.delete()
                .addOnSuccessListener(aVoid -> {
                    // Old file deleted successfully (silent success)
                })
                .addOnFailureListener(e -> {
                    // Log error but don't show to user since main operation succeeded
                    // You could use Log.w("FaceActivity", "Failed to delete old file: " + e.getMessage());
                });
    }

    // Method to show delete confirmation
    private void showDeleteConfirmation(Face face, String documentId) {
        new AlertDialog.Builder(FaceActivity.this)
                .setTitle("Delete Face")
                .setMessage("Are you sure you want to delete \"" + face.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteFace(documentId);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Method to delete face from Firestore
    private void deleteFace(String documentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("faces").document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Face deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting face: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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