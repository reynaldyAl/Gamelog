package com.example.gamemology.ui.settings;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.gamemology.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AboutDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.about_app)
                .setMessage(R.string.about_app_message)
                .setPositiveButton(R.string.ok, null)
                .create();
    }
}