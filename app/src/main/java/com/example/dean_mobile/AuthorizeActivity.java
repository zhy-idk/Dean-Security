package com.example.dean_mobile;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class AuthorizeActivity extends AppCompatActivity {
    EditText etEmail;
    Button btnSetAuthorize;
    ListView listUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_authorize);

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        btnSetAuthorize = findViewById(R.id.btnSetAuthorize);
        listUsers = findViewById(R.id.listUsers);

        // Create a sample list of users
        ArrayList<String> users = new ArrayList<>();
        users.add("user1@example.com");
        users.add("user2@example.com");
        users.add("user3@example.com");
        users.add("user4@example.com");
        users.add("user5@example.com");

        // Set the ListView adapter to show the users list
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, users);
        listUsers.setAdapter(adapter);
    }
}
