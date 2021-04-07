package com.example.myapplication.ui.setting;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterViewAnimator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.example.myapplication.R;
import com.example.myapplication.activity.AboutActivity;
import com.example.myapplication.ui.home.SharedViewModel;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class SettingFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private SettingViewModel settingViewModel;
    private SharedViewModel sharedViewModel;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingViewModel =
                new ViewModelProvider(this).get(SettingViewModel.class);
        View root = inflater.inflate(R.layout.fragment_setting, container, false);
        final TextView textView = root.findViewById(R.id.text_setting);
        settingViewModel.getCity().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        Spinner spinner = root.findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.cities, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        // Shared View Model
        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);

        // Button About
        root.findViewById(R.id.about_btn).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AboutActivity.class);
            startActivity(intent);
        });

        // dark mode button
        Button btnToggleDark;
        btnToggleDark
                = root.findViewById(R.id.dark_btn);
        AtomicBoolean isDark = new AtomicBoolean(false);
        btnToggleDark.setOnClickListener(
                view -> {
                    if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO){
                        AppCompatDelegate
                                .setDefaultNightMode(
                                        AppCompatDelegate
                                                .MODE_NIGHT_YES);
                        isDark.set(true);
                    }else{
                        AppCompatDelegate
                                .setDefaultNightMode(
                                        AppCompatDelegate
                                                .MODE_NIGHT_NO);

                        isDark.set(false);
                    }

                    if(isDark.get()){
                        btnToggleDark.setText("Dark Model");
                    }
                    else{
                        btnToggleDark.setText("Dark Model");
                    }
                });

        // clear data button
        root.findViewById(R.id.clear_btn).setOnClickListener(v -> {
            AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(getContext());
            alertDialogBuilder.setTitle("Clear All Data");
            alertDialogBuilder.setMessage("All your data has been deleted!");
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show(); // Show Dialog
        });

        // send a notification button
        Button sendNotification;
        sendNotification = root.findViewById(R.id.notification_btn);
        sendNotification.setOnClickListener(
                view -> {
                    CharSequence name = getString(R.string.common_google_play_services_notification_channel_name);
                    String description = getString(R.string.common_google_play_services_notification_channel_name);
                    int importance = NotificationManager.IMPORTANCE_DEFAULT;
                    NotificationChannel channel = new NotificationChannel("my_channel_1", name, importance);
                    channel.setDescription(description);

                    PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, new Intent(), 0);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), "my_channel_1")
                            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                            .setContentTitle("COVID-19 NOTIFICATION")
                            .setContentText("This is a test notification!")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());
                    // notificationId is a unique int for each notification that you must define
                    notificationManager.notify(1, builder.build());
                }
        );

        // show storage size
        File appBaseFolder = this.getContext().getFilesDir().getParentFile();
        long totalSize = browseFiles(appBaseFolder);
        TextView storageSize = root.findViewById(R.id.text_storage);
        storageSize.setText("Storage Size: " + totalSize + " total bytes.");

        return root;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();
        Log.d("On Item Selected", text);
        settingViewModel.setCity(text);
        sharedViewModel.setNameData(text);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    // browse file helper function
    private long browseFiles(File dir) {
        long dirSize = 0;
        for (File f: dir.listFiles()) {
            dirSize += f.length();
            // Log.d(STORAGE_TAG, dir.getAbsolutePath() + "/" + f.getName() + " uses " + f.length() + " bytes");
            if (f.isDirectory()) {
                dirSize += browseFiles(f);
            }
        }
        // Log.d(STORAGE_TAG, dir.getAbsolutePath() + " uses " + dirSize + " bytes");
        return dirSize;
    }
}
