package com.example.dean_mobile;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

public class HomeFragment extends Fragment {

    ImageView imageView;
    Button btnCapture, btnRecord;
    RecyclerView rvCameras, rvLocal;
    CameraAdapter adapter;
    Spinner spnView;
    LinearLayout loadingOverlay;

    LocalImageAdapter localImageAdapter;
    LocalVideoAdapter localVideoAdapter;

    private Surface inputSurface;
    private int selectedCameraPosition = 0; // auto-updated when centered

    // Recording variables
    private boolean isRecording = false;
    private MediaCodec mediaCodec;
    private MediaMuxer mediaMuxer;
    private int trackIndex = -1;
    private boolean muxerStarted = false;
    private long recordingStartTime = 0;
    private Handler recordingHandler;
    private Runnable recordingRunnable;
    private String currentVideoPath;
    private FileObserver fileObserver;

    LinearSnapHelper snapHelper;

    private static final int FRAME_RATE = 30;

    public HomeFragment() {}

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordingHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnCapture = view.findViewById(R.id.btnCapture);
        btnRecord = view.findViewById(R.id.btnRecord);
        rvCameras = view.findViewById(R.id.rvCameras);
        rvCameras.setItemAnimator(null);
        rvLocal = view.findViewById(R.id.rvLocal);
        rvLocal.setItemAnimator(null);
        spnView = view.findViewById(R.id.spnView);

        loadingOverlay = view.findViewById(R.id.loadingOverlay);


        setupRecyclerView();
        setupLocalMediaView();

        btnCapture.setOnClickListener(v -> captureSelectedCamera());
        btnRecord.setOnClickListener(v -> toggleRecording());
    }

    private void setupLocalMediaView() {
        rvLocal.setLayoutManager(new GridLayoutManager(getContext(), 3));

        spnView.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (selected.equals("Captured Images")) {
                    loadLocalImages();
                    observeFolder(Environment.DIRECTORY_PICTURES + "/IntruSight", true);
                } else if (selected.equals("Recorded Videos")) {
                    loadLocalVideos();
                    observeFolder(Environment.DIRECTORY_MOVIES + "/IntruSight", false);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Default to images
        loadLocalImages();
        observeFolder(Environment.DIRECTORY_PICTURES + "/IntruSight", true);

        // ✅ Add this observer block here:
        requireContext().getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                new android.database.ContentObserver(new Handler(Looper.getMainLooper())) {
                    @Override
                    public void onChange(boolean selfChange) {
                        super.onChange(selfChange);
                        loadLocalImages(); // or loadLocalVideos() depending on your current spinner selection
                    }
                }
        );
    }



    private void loadLocalImages() {
        File picturesDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "IntruSight");

        if (!picturesDir.exists())
            picturesDir.mkdirs();

        File[] imgFiles = picturesDir.listFiles((dir, name) ->
                (name.endsWith(".jpg") || name.endsWith(".jpeg")) && !name.contains(".trashed")
        );

        if (imgFiles == null)
            imgFiles = new File[0];

        Collections.reverse(Arrays.asList(imgFiles));

        if (localImageAdapter == null) {
            localImageAdapter = new LocalImageAdapter(getContext(), Arrays.asList(imgFiles));
            rvLocal.setAdapter(localImageAdapter);
        } else {
            localImageAdapter = new LocalImageAdapter(getContext(), Arrays.asList(imgFiles));
            rvLocal.setAdapter(localImageAdapter);
            localImageAdapter.notifyDataSetChanged();
        }
    }

    private void loadLocalVideos() {
        File videosDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "IntruSight");

        if (!videosDir.exists())
            videosDir.mkdirs();

        File[] files = videosDir.listFiles((dir, name) ->
                name.endsWith(".mp4") && !name.contains(".trashed")
        );

        if (files == null)
            files = new File[0];

        Collections.reverse(Arrays.asList(files));

        if (localVideoAdapter == null) {
            localVideoAdapter = new LocalVideoAdapter(getContext(), Arrays.asList(files));
            rvLocal.setAdapter(localVideoAdapter);
        } else {
            localVideoAdapter = new LocalVideoAdapter(getContext(), Arrays.asList(files));
            rvLocal.setAdapter(localVideoAdapter);
            localVideoAdapter.notifyDataSetChanged();
        }
    }




    private void setupRecyclerView() {
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvCameras.setLayoutManager(layoutManager);

        Query query = FirebaseDatabase.getInstance().getReference().child("cameras");

        FirebaseRecyclerOptions<Camera> options =
                new FirebaseRecyclerOptions.Builder<Camera>()
                        .setQuery(query, Camera.class)
                        .build();

        adapter = new CameraAdapter(options);
        rvCameras.setAdapter(adapter);
        adapter.startListening();

        // Snap to center when scrolling
        snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(rvCameras);

        rvCameras.postDelayed(() -> {
            View centerView = snapHelper.findSnapView(layoutManager);
            if (centerView != null) {
                int position = layoutManager.getPosition(centerView);
                Camera currentCamera = adapter.getItem(position);
                if (currentCamera != null) {
                    checkCameraReady(currentCamera.getLink());
                }
            }
        }, 500);

        // Detect centered camera when scrolling stops
        rvCameras.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View centerView = snapHelper.findSnapView(layoutManager);
                    if (centerView != null) {
                        int position = layoutManager.getPosition(centerView);
                        if (position != RecyclerView.NO_POSITION && position != selectedCameraPosition) {
                            selectedCameraPosition = position;
                            Toast.makeText(getContext(), "Camera " + (position + 1) + " selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

    }

    private void checkCameraReady(String link) {
        loadingOverlay.setVisibility(View.VISIBLE);

        new Thread(() -> {
            boolean isReachable = false;
            try {
                java.net.URL url = new java.net.URL(link);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();
                isReachable = (responseCode == 200);
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean finalIsReachable = isReachable;
            requireActivity().runOnUiThread(() -> {
                if (finalIsReachable) {
                    loadingOverlay.setVisibility(View.GONE);
                } else {
                    loadingOverlay.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }


    private WebView getWebViewAtPosition(int position) {
        RecyclerView.ViewHolder holder = rvCameras.findViewHolderForAdapterPosition(position);
        if (holder instanceof CameraAdapter.ViewHolder) {
            return ((CameraAdapter.ViewHolder) holder).wvLive;
        }
        return null;
    }

    // =============================================================================
    // CAPTURE IMAGE
    // =============================================================================

    private void captureSelectedCamera() {
        WebView webView = getWebViewAtPosition(selectedCameraPosition);

        try {
            Bitmap bitmap = Bitmap.createBitmap(webView.getWidth(), webView.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            webView.draw(canvas);
            saveBitmapToGallery(bitmap);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Capture failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveBitmapToGallery(Bitmap bitmap) {
        try {
            String fileName = "capture_" + System.currentTimeMillis() + ".jpg";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/IntruSight");
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);


            Uri uri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                OutputStream out = requireContext().getContentResolver().openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.close();

                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                requireContext().getContentResolver().update(uri, values, null, null);

                Toast.makeText(getContext(), "Image saved!", Toast.LENGTH_SHORT).show();

            }
            bitmap.recycle();
            localImageAdapter.notifyDataSetChanged();

            if (isAutoUploadEnabled(getContext())) {
                File picturesDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "IntruSight");
                File savedFile = new File(picturesDir, fileName);
                uploadToFirebase(savedFile);
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // =============================================================================
    // VIDEO RECORDING
    // =============================================================================

    private void toggleRecording() {
        if (isRecording) stopRecording();
        else startRecording();
    }

    private void startRecording() {
        WebView webView = getWebViewAtPosition(selectedCameraPosition);

        try {
            int width = webView.getWidth();
            int height = webView.getHeight();

            if (width == 0 || height == 0) {
                Toast.makeText(getContext(), "Camera view not ready yet", Toast.LENGTH_SHORT).show();
                return;
            }

            prepareVideoEncoder(width, height);
            isRecording = true;
            recordingStartTime = System.currentTimeMillis();

            btnRecord.setText("Stop");
            btnRecord.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), android.R.color.holo_red_dark));
            btnCapture.setEnabled(false);

            recordingRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isRecording) {
                        captureFrame();
                        recordingHandler.postDelayed(this, 1000 / FRAME_RATE);
                    }
                }
            };
            recordingHandler.post(recordingRunnable);

            Toast.makeText(getContext(), "Recording camera " + (selectedCameraPosition + 1), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to start recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            isRecording = false;
        }
    }

    private void stopRecording() {
        isRecording = false;
        if (recordingRunnable != null) recordingHandler.removeCallbacks(recordingRunnable);

        try {
            if (mediaCodec != null) {
                drainEncoder(true);
                mediaCodec.stop();
                mediaCodec.release();
                mediaCodec = null;
            }

            if (mediaMuxer != null) {
                if (muxerStarted) mediaMuxer.stop();
                mediaMuxer.release();
                mediaMuxer = null;
            }

            btnRecord.setText("Record");
            TypedValue typedValue = new TypedValue();
            requireContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
            btnRecord.setBackgroundTintList(ColorStateList.valueOf(typedValue.data));
            btnCapture.setEnabled(true);

            if (currentVideoPath != null) saveVideoToGallery(currentVideoPath);
            Toast.makeText(getContext(), "Recording saved!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to stop recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        muxerStarted = false;
        trackIndex = -1;
    }

    private void prepareVideoEncoder(int width, int height) throws IOException {
        width &= ~1;
        height &= ~1;
        String outputPath = getOutputFilePath();
        currentVideoPath = outputPath;

        MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 4000000);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);

        mediaCodec = MediaCodec.createEncoderByType("video/avc");
        mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        inputSurface = mediaCodec.createInputSurface();
        mediaCodec.start();

        mediaMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        muxerStarted = false;
    }

    private void captureFrame() {
        if (inputSurface == null) return;
        WebView webView = getWebViewAtPosition(selectedCameraPosition);
        if (webView == null) return;

        Canvas canvas = inputSurface.lockCanvas(null);
        try {
            canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);

            float scaleX = (float) canvas.getWidth() / webView.getWidth();
            float scaleY = (float) canvas.getHeight() / webView.getHeight();
            float scale = Math.min(scaleX, scaleY);
            float offsetX = (canvas.getWidth() - webView.getWidth() * scale) / 2f;
            float offsetY = (canvas.getHeight() - webView.getHeight() * scale) / 2f;

            canvas.save();
            canvas.translate(offsetX, offsetY);
            canvas.scale(scale, scale);
            webView.draw(canvas);
            canvas.restore();
        } finally {
            inputSurface.unlockCanvasAndPost(canvas);
        }
        drainEncoder(false);
    }

    private void drainEncoder(boolean endOfStream) {
        if (endOfStream) mediaCodec.signalEndOfInputStream();

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (true) {
            int outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
            if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break;
            } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat newFormat = mediaCodec.getOutputFormat();
                trackIndex = mediaMuxer.addTrack(newFormat);
                mediaMuxer.start();
                muxerStarted = true;
            } else if (outputIndex >= 0) {
                ByteBuffer encodedData = mediaCodec.getOutputBuffer(outputIndex);
                if (encodedData != null && muxerStarted) {
                    encodedData.position(bufferInfo.offset);
                    encodedData.limit(bufferInfo.offset + bufferInfo.size);
                    mediaMuxer.writeSampleData(trackIndex, encodedData, bufferInfo);
                }
                mediaCodec.releaseOutputBuffer(outputIndex, false);
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) break;
            }
        }
    }

    private String getOutputFilePath() {
        File moviesDir = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES), "ScreenRecords");
        if (!moviesDir.exists()) moviesDir.mkdirs();
        return new File(moviesDir, "recording_" + System.currentTimeMillis() + ".mp4").getAbsolutePath();
    }


    private void saveVideoToGallery(String videoPath) {
        try {
            File videoFile = new File(videoPath);
            if (!videoFile.exists()) return;

            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/IntruSight");
            values.put(MediaStore.Video.Media.DISPLAY_NAME, videoFile.getName());
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            values.put(MediaStore.Video.Media.IS_PENDING, 1);


            Uri uri = getContext().getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                OutputStream out = getContext().getContentResolver().openOutputStream(uri);
                FileInputStream in = new FileInputStream(videoFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) out.write(buffer, 0, length);
                in.close();
                out.close();
                videoFile.delete();

                values.clear();
                values.put(MediaStore.Video.Media.IS_PENDING, 0);
                requireContext().getContentResolver().update(uri, values, null, null);
            }
            localVideoAdapter.notifyDataSetChanged();

            if (isAutoUploadEnabled(getContext())) {
                File videosDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MOVIES), "IntruSight");
                File savedFile = new File(videosDir, new File(videoPath).getName());
                uploadToFirebase(savedFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void observeFolder(String relativePath, boolean isImageFolder) {
        if (fileObserver != null) {
            fileObserver.stopWatching();
        }

        File dir = new File(Environment.getExternalStoragePublicDirectory(relativePath).getAbsolutePath());
        fileObserver = new FileObserver(dir.getAbsolutePath(),
                FileObserver.CREATE | FileObserver.MOVED_TO | FileObserver.DELETE | FileObserver.MOVED_FROM) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                if (path != null) {
                    requireActivity().runOnUiThread(() -> {
                        if (isImageFolder) {
                            loadLocalImages();
                        } else {
                            loadLocalVideos();
                        }
                    });
                }
            }
        };
        fileObserver.startWatching();
    }


    private boolean isAutoUploadEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AutoUpload", Context.MODE_PRIVATE);
        return prefs.getBoolean("autoUpload", false);
    }

    private void uploadToFirebase(File file) {
        if (file == null || !file.exists()) return;
        Log.d("UPLOAD", "AutoUpload enabled: ");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        Uri fileUri = Uri.fromFile(file);
        StorageReference fileRef = storageRef.child("media" + "/" + file.getName());

        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot ->
                        Toast.makeText(getContext(), "Uploaded: " + file.getName(), Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }


    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isRecording) stopRecording();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isRecording) stopRecording();
    }
}
