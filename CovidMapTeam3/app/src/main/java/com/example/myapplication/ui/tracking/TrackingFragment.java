package com.example.myapplication.ui.tracking;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.example.myapplication.R;
import com.example.myapplication.ui.home.SharedViewModel;

import org.w3c.dom.Text;

public class TrackingFragment extends Fragment {

    private static final String TAG = "TrackingFragment";
    private TrackingViewModel trackingViewModel;
    private SharedViewModel sharedViewModel;
    CalendarView calendar;
    TextView date_view;
    String dateSelected;

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

        calendar = (CalendarView)
                root.findViewById(R.id.calender);
        date_view = (TextView)
                root.findViewById(R.id.date_view);

        // Add Listener in calendar
        calendar
                .setOnDateChangeListener(
                        new CalendarView
                                .OnDateChangeListener() {
                            @Override

                            // In this Listener have one method
                            // and in this method we will
                            // get the value of DAYS, MONTH, YEARS
                            public void onSelectedDayChange(
                                    @NonNull CalendarView view,
                                    int year,
                                    int month,
                                    int dayOfMonth)
                            {

                                // Store the value of date with
                                // format in String type Variable
                                // Add 1 in month because month
                                // index is start with 0
                                dateSelected
                                        = dayOfMonth + "/"
                                        + (month + 1) + "/" + year;

                                // set this date in TextView for Display
                                date_view.setText(dateSelected);

                                TextView travelHistory = root.findViewById(R.id.travel_history);
                                String history = "On " + dateSelected + ", you have been to dummy city Los Angeles";
                                travelHistory.setText(history);
                            }


                        });

//        TextView travelHistory = root.findViewById(R.id.travel_history);
//        String history = "On " + dateSelected + ", you have been to dummy city Los Angeles";
//        travelHistory.setText(history);

        return root;
    }
}