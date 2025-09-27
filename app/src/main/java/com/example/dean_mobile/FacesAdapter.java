package com.example.dean_mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class FacesAdapter extends FirestoreRecyclerAdapter<Face, FacesAdapter.ViewHolder> {

    // Interface for handling item clicks
    public interface OnItemClickListener {
        void onItemClick(Face face, int position, String documentId);
    }

    private OnItemClickListener listener;

    public FacesAdapter(@NonNull FirestoreRecyclerOptions<Face> options) {
        super(options);
    }

    // Method to set the click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Face model) {
        holder.txtName.setText(model.getName());
        Glide.with(holder.imgFace.getContext()).load(model.getImage()).into(holder.imgFace);

        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                String documentId = getSnapshots().getSnapshot(position).getId();
                listener.onItemClick(model, position, documentId);
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.face_item, parent, false);
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