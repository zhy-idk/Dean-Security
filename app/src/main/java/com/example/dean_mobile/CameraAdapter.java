package com.example.dean_mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.HashMap;
import java.util.Map;

public class CameraAdapter extends FirebaseRecyclerAdapter<Camera, CameraAdapter.ViewHolder> {

    private OnCameraClickListener clickListener;

    public interface OnCameraClickListener {
        void onCameraClick(int position);
    }

    public CameraAdapter(@NonNull FirebaseRecyclerOptions<Camera> options) {
        super(options);
    }

    public void setOnCameraClickListener(OnCameraClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Camera model) {
        Map<String, String> headers = new HashMap<>();
        headers.put("skip_zrok_interstitial", "1");
        holder.wvLive.loadUrl(model.getLink(), headers);

        // Set click listener for selecting this camera by position
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onCameraClick(holder.getBindingAdapterPosition());
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.camera_item, parent, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        WebView wvLive;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            wvLive = itemView.findViewById(R.id.wvLive);
            wvLive.getSettings().setLoadWithOverviewMode(true);
            wvLive.getSettings().setUseWideViewPort(true);
            wvLive.getSettings().setBuiltInZoomControls(false);
            wvLive.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            wvLive.setScrollbarFadingEnabled(true);
            wvLive.setWebViewClient(new WebViewClient());
        }
    }
}