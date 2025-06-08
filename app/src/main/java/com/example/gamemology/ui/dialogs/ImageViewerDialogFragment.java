package com.example.gamemology.ui.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.request.target.Target;
import com.example.gamemology.R;
import com.example.gamemology.databinding.DialogImageViewerBinding;

public class ImageViewerDialogFragment extends DialogFragment {

    private DialogImageViewerBinding binding;
    private String imageUrl;

    public static ImageViewerDialogFragment newInstance(String imageUrl) {
        ImageViewerDialogFragment fragment = new ImageViewerDialogFragment();
        Bundle args = new Bundle();
        args.putString("imageUrl", imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageUrl = getArguments().getString("imageUrl");
        }
        // Use a more specific dialog style with no title and proper background
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FullScreenDialogStyle);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // Ensure dialog window has no title
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Setup dialog window early
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.BLACK));

            // Set layout flags early
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.setStatusBarColor(Color.BLACK);
        }

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DialogImageViewerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set system UI flags for fullscreen
        view.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        // Show loading indicator
        binding.progressIndicator.setVisibility(View.VISIBLE);

        // Load image using PhotoView with enhanced loading
        Glide.with(this)
                .load(imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .error(R.drawable.ic_error_image)
                .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<android.graphics.drawable.Drawable> target,
                                                boolean isFirstResource) {
                        binding.progressIndicator.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource,
                                                   Object model,
                                                   Target<android.graphics.drawable.Drawable> target,
                                                   DataSource dataSource,
                                                   boolean isFirstResource) {
                        binding.progressIndicator.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(binding.photoView);

        // Set close button click listener with ripple effect
        binding.btnClose.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            // Ensure dialog is full screen with proper background
            Window window = dialog.getWindow();
            if (window != null) {
                // Set layout to full screen
                WindowManager.LayoutParams params = window.getAttributes();
                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.height = WindowManager.LayoutParams.MATCH_PARENT;
                params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
                params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                window.setAttributes(params);

                // Solid black background to prevent any transparency
                window.setBackgroundDrawable(new ColorDrawable(Color.BLACK));

                // Handle system bars visibility based on API level
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(false);
                    window.getInsetsController().setSystemBarsBehavior(
                            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                    window.getInsetsController().hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                } else {
                    @SuppressWarnings("deprecation")
                    int flags = View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                    window.getDecorView().setSystemUiVisibility(flags);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Apply system UI flags again in case they were reset
        if (getDialog() != null && getDialog().getWindow() != null) {
            View decorView = getDialog().getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LOW_PROFILE |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}