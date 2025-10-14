package com.example.dean_mobile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.util.List;

public class LocalVideoAdapter extends RecyclerView.Adapter<LocalVideoAdapter.ViewHolder> {

    private final Context context;
    private final List<File> videoFiles;

    public LocalVideoAdapter(Context context, List<File> videoFiles) {
        this.context = context;
        this.videoFiles = videoFiles;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.local_media_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = videoFiles.get(position);

        Glide.with(context)
                .load(file)
                .centerCrop()
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            Uri uri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider", // must match manifest
                    file
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "video/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return videoFiles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.ivMedia);
        }
    }
}
