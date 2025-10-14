package com.example.dean_mobile;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class CloudContentActivity extends AppCompatActivity {

    private RecyclerView rvCloudMedia;
    private ProgressBar progressBar;
    private CloudMediaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_media);

        rvCloudMedia = findViewById(R.id.rvCloudMedia);
        progressBar = findViewById(R.id.progressBar);

        rvCloudMedia.setLayoutManager(new GridLayoutManager(this, 3));

        List<StorageReference> mediaList = new ArrayList<>();
        adapter = new CloudMediaAdapter(this, mediaList);
        rvCloudMedia.setAdapter(adapter);

        progressBar.setVisibility(View.VISIBLE);

        FirebaseStorage.getInstance().getReference("media")
                .listAll()
                .addOnSuccessListener(listResult -> {
                    mediaList.addAll(listResult.getItems());
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}