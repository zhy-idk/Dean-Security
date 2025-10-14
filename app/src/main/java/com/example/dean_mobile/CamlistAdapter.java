package com.example.dean_mobile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
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
                        // Edit option
                        showEditDialog(holder, itemRef, model);
                    } else {
                        // Delete option
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
        Context context = holder.itemView.getContext();

        // Create layout
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * context.getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        // Camera Link
        TextView linkLabel = new TextView(context);
        linkLabel.setText("Camera Link:");
        EditText inputLink = new EditText(context);
        inputLink.setInputType(InputType.TYPE_CLASS_TEXT);
        inputLink.setText(model.getLink());

        // Bounding 1
        TextView bound1Label = new TextView(context);
        bound1Label.setText("Bounding 1 (0–100):");
        EditText inputBound1 = new EditText(context);
        inputBound1.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputBound1.setText(String.valueOf(model.getBounding1()));

        // Bounding 2
        TextView bound2Label = new TextView(context);
        bound2Label.setText("Bounding 2 (0–100):");
        EditText inputBound2 = new EditText(context);
        inputBound2.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputBound2.setText(String.valueOf(model.getBounding2()));

        // Optional: also limit input length to 3 digits max
        inputBound1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        inputBound2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        // Add views to layout
        layout.addView(linkLabel);
        layout.addView(inputLink);
        layout.addView(bound1Label);
        layout.addView(inputBound1);
        layout.addView(bound2Label);
        layout.addView(inputBound2);

        new AlertDialog.Builder(context)
                .setTitle("Edit Camera Info")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newLink = inputLink.getText().toString().trim();
                    String bound1Text = inputBound1.getText().toString().trim();
                    String bound2Text = inputBound2.getText().toString().trim();

                    int newBound1 = 0;
                    int newBound2 = 0;

                    try {
                        if (!bound1Text.isEmpty()) newBound1 = Integer.parseInt(bound1Text);
                        if (!bound2Text.isEmpty()) newBound2 = Integer.parseInt(bound2Text);
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Invalid number entered", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Enforce limits (0–100)
                    if (newBound1 < 0) newBound1 = 0;
                    if (newBound1 > 100) newBound1 = 100;
                    if (newBound2 < 0) newBound2 = 0;
                    if (newBound2 > 100) newBound2 = 100;

                    if (!newLink.isEmpty()) itemRef.child("link").setValue(newLink);
                    itemRef.child("bounding1").setValue(newBound1);
                    itemRef.child("bounding2").setValue(newBound2);

                    Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show();
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
