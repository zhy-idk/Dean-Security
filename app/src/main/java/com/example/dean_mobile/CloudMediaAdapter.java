package com.example.dean_mobile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class CloudMediaAdapter extends RecyclerView.Adapter<CloudMediaAdapter.ViewHolder> {

    private final Context context;
    private final List<StorageReference> mediaFiles;

    public CloudMediaAdapter(Context context, List<StorageReference> mediaFiles) {
        this.context = context;
        this.mediaFiles = mediaFiles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cloud_media_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StorageReference ref = mediaFiles.get(position);
        String name = ref.getName();

        holder.tvFileName.setText(name);

        ref.getDownloadUrl().addOnSuccessListener(uri -> {
            // Load thumbnail preview using Glide
            Glide.with(context)
                    .load(uri)
                    .centerCrop()
                    .into(holder.ivThumbnail);

            // Show dialog on click
            holder.itemView.setOnClickListener(v -> {
                showOptionsDialog(ref, uri, name, position);
            });
        });
    }

    private void showOptionsDialog(StorageReference ref, Uri uri, String fileName, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(fileName);
        builder.setItems(new String[]{"View", "Delete"}, (dialog, which) -> {
            if (which == 0) {
                // View option
                viewMedia(uri, fileName);
            } else if (which == 1) {
                // Delete option
                confirmDelete(ref, fileName, position);
            }
        });
        builder.show();
    }

    private void viewMedia(Uri uri, String fileName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri,
                (fileName.toLowerCase().endsWith(".mp4") ||
                        fileName.toLowerCase().endsWith(".mov") ||
                        fileName.toLowerCase().endsWith(".mkv"))
                        ? "video/*" : "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent chooser = Intent.createChooser(intent, "Open with");
        context.startActivity(chooser);
    }

    private void confirmDelete(StorageReference ref, String fileName, int position) {
        AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(context);
        confirmBuilder.setTitle("Delete File");
        confirmBuilder.setMessage("Are you sure you want to delete " + fileName + "?");
        confirmBuilder.setPositiveButton("Delete", (dialog, which) -> {
            deleteFile(ref, position);
        });
        confirmBuilder.setNegativeButton("Cancel", null);
        confirmBuilder.show();
    }

    private void deleteFile(StorageReference ref, int position) {
        ref.delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                    mediaFiles.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, mediaFiles.size());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return mediaFiles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvFileName;
        ViewHolder(View view) {
            super(view);
            ivThumbnail = view.findViewById(R.id.ivThumbnail);
            tvFileName = view.findViewById(R.id.tvFileName);
        }
    }
}