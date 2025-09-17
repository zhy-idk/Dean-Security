package com.example.dean_mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;


public class NotifAdapter extends FirebaseRecyclerAdapter<Notif, NotifAdapter.ViewHolder> {

    public NotifAdapter(@NonNull FirebaseRecyclerOptions<Notif> options) {
        super(options);
    }

    @NonNull
    @Override
    public NotifAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notif_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Notif model) {
        holder.textNotif.setText(model.getTitle());
        holder.textNotifTime.setText(model.getTime());
        holder.textNotifDate.setText(model.getDate());
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNotif, textNotifTime, textNotifDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNotif = itemView.findViewById(R.id.textNotif);
            textNotifTime = itemView.findViewById(R.id.textNotifTime);
            textNotifDate = itemView.findViewById(R.id.textNotifDate);
        }
    }
}
