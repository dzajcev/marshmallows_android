package com.dzaitsev.marshmallow.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dzaitsev.marshmallow.R;
import com.github.chrisbanes.photoview.PhotoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
public class FullScreenImageDialogFragment extends DialogFragment {

    private static final String ARG_IMAGE_URL = "arg_image_url";

    public static FullScreenImageDialogFragment newInstance(String imageUrl) {
        FullScreenImageDialogFragment fragment = new FullScreenImageDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_URL, imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fullscreen_image, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        PhotoView photoView = view.findViewById(R.id.photoView);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        String imageUrl = requireArguments().getString(ARG_IMAGE_URL);
        progressBar.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(imageUrl)
                .error(R.drawable.error)
                .listener(new RequestListener<>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false; // Glide покажет error drawable
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false; // Glide установит изображение
                    }
                })
                .into(photoView);

        // Закрытие по тапу вне изображения
        photoView.setOnOutsidePhotoTapListener(v -> dismiss());

        // Double-tap zoom подсказка (PhotoView поддерживает по умолчанию)
        photoView.setOnDoubleTapListener(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // Можно добавить анимацию или toast
                Toast.makeText(getContext(), "Double tap to zoom", Toast.LENGTH_SHORT).show();
                return false; // PhotoView выполнит zoom
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.black);
        }
    }
}