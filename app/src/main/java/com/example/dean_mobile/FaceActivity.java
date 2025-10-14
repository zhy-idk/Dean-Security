package com.example.dean_mobile;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FaceActivity extends AppCompatActivity {
    ImageButton btnImage;
    EditText etName;
    Button btnUpload;
    RecyclerView rvUsers;
    FacesAdapter adapter;

    ImageView ivPerson;
    TextView tvName, tvStatus;

    private Uri imageUri;
    private DatabaseReference dbRef;

    // Gallery
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    btnImage.setImageURI(uri);
                }
            });

    // Camera
    private final ActivityResultLauncher<Uri> takePhoto =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && imageUri != null) {
                    btnImage.setImageURI(imageUri);
                }
            });

    // Request permission
    private final ActivityResultLauncher<String> requestPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) openCamera();
                else Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_face);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        dbRef = FirebaseDatabase.getInstance().getReference("personBucket");

        btnImage = findViewById(R.id.btnImage);
        etName = findViewById(R.id.etName);
        btnUpload = findViewById(R.id.btnUpload);
        rvUsers = findViewById(R.id.rvUsers);
        ivPerson = findViewById(R.id.ivPerson);
        tvName = findViewById(R.id.tvName);
        tvStatus = findViewById(R.id.tvStatus);

        loadFace();

        btnImage.setOnClickListener(v -> showImagePickerDialog());
        btnUpload.setOnClickListener(v -> uploadImage());

        Query query = FirebaseDatabase.getInstance().getReference().child("notif");
        query.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                RegAlert alert = snapshot.getValue(RegAlert.class);
                if (alert != null) {
                    String message = alert.getMessage();
                    if (!message.equals("")){
                        AlertDialog.Builder builder = new AlertDialog.Builder(FaceActivity.this);
                        builder.setTitle("Alert")
                                .setMessage(message)  // Set the message from RegAlert
                                .setPositiveButton("OK", (dialog, which) -> {
                                    // Update the Firebase database to set the message to ""
                                    FirebaseDatabase.getInstance().getReference().child("notif").setValue(new RegAlert(""));
                                    FirebaseDatabase.getInstance().getReference().child("personBucket").setValue(new Face("", "", 0));
                                    dialog.dismiss();
                                })
                                .show();
                    }
                }
            }});

    }

    @Override
    protected void onStart() {
        super.onStart();
        //adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //adapter.stopListening();
    }

    private void showEditDialog(Face face, String key) {
        EditText editText = new EditText(FaceActivity.this);
        editText.setText(face.getName());
        editText.setHint("Enter name");
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        editText.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(FaceActivity.this)
                .setTitle("Edit Face")
                .setMessage("Enter new name or delete this face:")
                .setView(editText)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = editText.getText().toString().trim();
                    if (!newName.isEmpty()) updateFaceName(key, newName);
                    else Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Delete", (dialog, which) -> deleteFace(key))
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void updateFaceName(String key, String newName) {
        dbRef.orderByChild("name").equalTo(newName)
                .get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Toast.makeText(this, "Name already exists.", Toast.LENGTH_SHORT).show();
                    } else {
                        dbRef.child(key).child("name").setValue(newName)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void deleteFace(String key) {
        dbRef.child(key).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showImagePickerDialog() {
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Option")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED) openCamera();
                        else requestPermission.launch(Manifest.permission.CAMERA);
                    } else pickImage.launch("image/*");
                }).show();
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

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        btnUpload.setEnabled(false);
        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            String base64String = encodeImageToBase64(bitmap);
            saveFaceToDatabase(name, base64String);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnUpload.setEnabled(true);
        }
    }

    private void saveFaceToDatabase(String name, String base64Image) {
        // Create a new Face object
        Face face = new Face(name, base64Image, 1);

        dbRef.setValue(face).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(FaceActivity.this, "Data saved successfully", Toast.LENGTH_SHORT).show();
                etName.setText("");
                btnImage.setImageResource(R.drawable.placeholder);
                imageUri = null;
                btnUpload.setEnabled(true);
            } else {
                Toast.makeText(FaceActivity.this, "Failed to save data", Toast.LENGTH_SHORT).show();
                btnUpload.setEnabled(true);
            }
        });
    }

    private void loadFace() {
        dbRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Face face = snapshot.getValue(Face.class);
                if (face != null) {
                    tvName.setText(face.getName());
                    ivPerson.setImageBitmap(decodeBase64ToBitmap(face.getImage()));
                    tvStatus.setText(!face.getName().isEmpty() ? "Waiting cam approval..." : "" );
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private Bitmap decodeBase64ToBitmap(String base64Image) {
        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }



}