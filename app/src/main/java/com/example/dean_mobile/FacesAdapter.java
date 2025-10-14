package com.example.dean_mobile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class FacesAdapter extends FirebaseRecyclerAdapter<Face, FacesAdapter.ViewHolder> {

    // Interface for handling item clicks
    public interface OnItemClickListener {
        void onItemClick(Face face, int position);  // No need for key now
    }

    private OnItemClickListener listener;

    public FacesAdapter(@NonNull FirebaseRecyclerOptions<Face> options) {
        super(options);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Face model) {
        holder.txtName.setText(model.getName());
        Log.d("FacesAdapter", "Image: " + model.getImage()); // Log the image)

        // Decode Base64 image string
        if (model.getImage() != null && !model.getImage().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(model.getImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.imgFace.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.imgFace.setImageResource(R.drawable.placeholder); // fallback image
            }
        } else {
            holder.imgFace.setImageResource(R.drawable.placeholder);
        }

        // Handle item click (no key needed anymore)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(model, position);  // Only pass the model and position
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.face_item, parent, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        ImageView imgFace;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            imgFace = itemView.findViewById(R.id.imgFace);
        }
    }
}
