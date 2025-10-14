package com.example.dean_mobile;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PushNotifActivity extends AppCompatActivity {
    EditText etEvent, etTime, etDate;
    Button btPush;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_push_notif);

        // Initialize Firebase Database reference
        dbRef = FirebaseDatabase.getInstance().getReference("notifs");

        etEvent = findViewById(R.id.etEvent);
        etTime = findViewById(R.id.etTime);
        etDate = findViewById(R.id.etDate);
        btPush = findViewById(R.id.btPush);

        btPush.setOnClickListener(v -> {
            String event = etEvent.getText().toString();
            String time = etTime.getText().toString();
            String date = etDate.getText().toString();

            String id = dbRef.push().getKey();

            // Create a notification object
            Notif notification = new Notif(event, time, date);

            // Store the notification in Firebase Realtime Database
            if (id != null) {
                dbRef.child(id).setValue(notification)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(PushNotifActivity.this, "Notification added", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(PushNotifActivity.this, "Failed to add notification", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
