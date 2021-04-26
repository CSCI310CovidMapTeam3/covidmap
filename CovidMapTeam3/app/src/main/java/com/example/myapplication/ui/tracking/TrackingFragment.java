package com.example.myapplication.ui.tracking;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.example.myapplication.DataBase.HistoryDBHelper;
import com.example.myapplication.DataBase.HistoryItem;
import com.example.myapplication.R;
import com.example.myapplication.ui.home.SharedViewModel;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
                                StringBuilder sbDate = new StringBuilder();
                                if (dayOfMonth < 10){
                                    sbDate.append(0);
                                }
                                sbDate.append(dayOfMonth);
                                sbDate.append("/");
                                if (month < 10){
                                    sbDate.append(0);
                                }
                                sbDate.append(month + 1);
                                sbDate.append("/");
                                sbDate.append(year);
                                dateSelected = sbDate.toString();

                                // set this date in TextView for Display
                                date_view.setText(dateSelected);

                                // change the prompt
                                TextView prompt = (TextView) root.findViewById(R.id.prompt);
                                prompt.setText("Scroll down to view all history.");

                                // fill the tracking table
                                TableLayout tl = (TableLayout) root.findViewById(R.id.tracking_table); // tracking history table
                                tl.removeAllViews();

                                String title = "name, latitude, longitude, time";
                                TableRow titleRow = new TableRow(getContext());
                                TextView titleText = new TextView(getContext());
                                titleText.setText(title);
                                titleRow.addView(titleText);
                                tl.addView(titleRow,0);

                                ArrayList<String> historyList = new ArrayList<>(); // history on that date
                                HistoryDBHelper history = HistoryDBHelper.getInstance(getContext()); // get history class
                                ArrayList<HistoryItem> historyFromAllDates = history.getAllListHistory(); // all history

                                for (HistoryItem hi : historyFromAllDates){
                                    Date tempDate = hi.getTimestamp();
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                    String string  = dateFormat.format(tempDate);
                                    if (string.equals(dateSelected)){ // if equal

                                        StringBuilder sb = new StringBuilder();
                                        sb.append(hi.getCityName());
                                        sb.append(", ");
                                        sb.append(hi.getLat());
                                        sb.append(", ");
                                        sb.append(hi.getLon());
                                        sb.append(", ");
                                        sb.append(hi.getTimestamp().toString());
                                        historyList.add(sb.toString());
                                    }
                                }

                                int i=0;
                                for (String s : historyList){
                                    TableRow tr = new TableRow(getContext());
                                    TextView tv = new TextView(getContext());
                                    tv.setText(s);
                                    tr.addView(tv);
                                    tl.addView(tr,++i);
                                }
                            }
                        });

        return root;
    }
}