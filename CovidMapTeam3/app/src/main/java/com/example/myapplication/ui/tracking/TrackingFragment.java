package com.example.myapplication.ui.tracking;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.example.myapplication.R;
import com.example.myapplication.ui.home.SharedViewModel;

public class TrackingFragment extends Fragment {

    private static final String TAG = "TrackingFragment";
    private TrackingViewModel trackingViewModel;
    private SharedViewModel sharedViewModel;

    private String currentCounty = "Los Angeles County";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        trackingViewModel =
                new ViewModelProvider(this).get(TrackingViewModel.class);
        View root = inflater.inflate(R.layout.fragment_tracking, container, false);
        final TextView textView = root.findViewById(R.id.text_tracking);
        trackingViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });


        // Shared View Model
        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);

        sharedViewModel.getCountyData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
                if (!s.equals("Los Angeles County")){
                    AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setTitle("Friendly Warning");
                    alertDialogBuilder.setMessage("Out of Los Angeles County \n(Current Service Area)");
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show(); // Show Dialog
                }
            }
        });

        return root;
    }
}