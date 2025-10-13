package com.example.dean_mobile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;

public class CamlistAdapter extends FirebaseRecyclerAdapter<Camera, CamlistAdapter.ViewHolder> {

    public CamlistAdapter(@NonNull FirebaseRecyclerOptions<Camera> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Camera model) {
        holder.tvLink.setText(model.getLink());
        Log.d("Testing", "onBindViewHolder: " + model.getLink());

        holder.itemView.setOnClickListener(v -> {
            showOptionsDialog(holder, getRef(position), model);
        });
    }

    private void showOptionsDialog(ViewHolder holder, DatabaseReference itemRef, Camera model) {
        String[] options = {"Edit", "Delete"};
        new AlertDialog.Builder(holder.itemView.getContext())
                .setTitle("Choose Action")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Edit
                        showEditDialog(holder, itemRef, model);
                    } else {
                        // Delete
                        itemRef.removeValue()
                                .addOnSuccessListener(unused ->
                                        Toast.makeText(holder.itemView.getContext(), "Camera deleted", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(holder.itemView.getContext(), "Failed to delete", Toast.LENGTH_SHORT).show());
                    }
                })
                .show();
    }

    private void showEditDialog(ViewHolder holder, DatabaseReference itemRef, Camera model) {
        EditText input = new EditText(holder.itemView.getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(model.getLink());

        new AlertDialog.Builder(holder.itemView.getContext())
                .setTitle("Edit Camera Link")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newLink = input.getText().toString().trim();
                    if (!newLink.isEmpty()) {
                        itemRef.child("link").setValue(newLink)
                                .addOnSuccessListener(unused ->
                                        Toast.makeText(holder.itemView.getContext(), "Updated successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(holder.itemView.getContext(), "Failed to update", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.camlist_item, parent, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLink;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLink = itemView.findViewById(R.id.tvLink);
        }
    }
}
