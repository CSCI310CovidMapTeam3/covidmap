package com.example.myapplication.ui.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterViewAnimator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.myapplication.BuildConfig;
import com.example.myapplication.DataBase.HistoryDBHelper;
import com.example.myapplication.DataBase.TestCenterDBHelper;
import com.example.myapplication.DataBase.WebSpider;
import com.example.myapplication.R;
import com.example.myapplication.activity.AboutActivity;
import com.example.myapplication.activity.Recorder;
import com.example.myapplication.ui.home.SharedViewModel;

import java.io.File;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class SettingFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private SettingViewModel settingViewModel;
    private SharedViewModel sharedViewModel;
    private Uri sound;
    private final long Milliseconds_Per_Day = 1000 * 60 * 60 * 24;
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
                textView.setText("");
            }
        });

        Spinner spinner = root.findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.cities, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        Spinner ringtone = root.findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity(), R.array.ringtoneChoices, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ringtone.setAdapter(adapter2);
        SharedPreferences oldSavedValues = PreferenceManager.getDefaultSharedPreferences(getContext());

        String oldSoundSetting = oldSavedValues.getString("count", "default");
        if (oldSoundSetting.equals("Fight On")) {
            ringtone.setSelection(1);
        }
        ringtone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = parent.getItemAtPosition(position).toString();
                Log.d("On Ring Item Selected", text);
                SharedPreferences saved_values = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = saved_values.edit();
                editor.putString("count",text);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("On Ring Item Not Select", "Ring Item Not Select");
            }
        });

        // set sound
        sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + BuildConfig.APPLICATION_ID + "/" + R.raw.fighton);

        // RingtoneManager.getRingtone(getContext(), sound).play();

        Spinner dataRetention = root.findViewById(R.id.spinner3);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(getActivity(), R.array.dataRetentionChoices, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataRetention.setAdapter(adapter3);
        dataRetention.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = parent.getItemAtPosition(position).toString();
                Log.d("On Retention Selected", text);
                HistoryDBHelper inst = HistoryDBHelper.getInstance(getContext());
                Date currentDate = new Date();
                Date removeDate = new Date();;
                switch (text) {
                    case "21":
                        Log.d("On Retention Selected", "Text Equals 21");
                        removeDate = new Date(currentDate.getTime() - Milliseconds_Per_Day * 21);
                        inst.deleteBeforeDate(removeDate);
                        break;
                    case "15":
                        Log.d("On Retention Selected", "Text Equals 15");
                        removeDate = new Date(currentDate.getTime() - Milliseconds_Per_Day * 15);
                        inst.deleteBeforeDate(removeDate);
                        break;
                    case "7":
                        Log.d("On Retention Selected", "Text Equals 7");
                        removeDate = new Date(currentDate.getTime() - Milliseconds_Per_Day * 7);
                        inst.deleteBeforeDate(removeDate);
                        break;
                    default:
                        Log.d("On Retention Selected", "Text Equals Default");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("On Retention Not Select", "Retention Not Selected");
            }
        });
        
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
            WebSpider.clearData();
            HistoryDBHelper inst1 = HistoryDBHelper.getInstance(getContext());
            inst1.clear();
            AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(getContext());
            alertDialogBuilder.setTitle("Clear All Data");
            alertDialogBuilder.setMessage("All your data has been deleted!");
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show(); // Show Dialog
        });

        // test record
        root.findViewById(R.id.button2).setOnClickListener(v -> {
            WorkRequest uploadWorkRequest =
                    new OneTimeWorkRequest.Builder(Recorder.class)
                            .build();
            WorkManager
                    .getInstance(getContext())
                    .enqueue(uploadWorkRequest);
            Log.d("Setting Fragment", "On Test Record: ");
        });

        // send a notification button
        // Zhian Li: Code from https://stackoverflow.com/questions/48986856/android-notification-setsound-is-not-working
        Button sendNotification;
        sendNotification = root.findViewById(R.id.notification_btn);
        sendNotification.setOnClickListener(
                view -> {
                    SharedPreferences saved_values = PreferenceManager.getDefaultSharedPreferences(getContext());

                    String currentSoundSetting = saved_values.getString("count", "default");
                    Log.d("SoundSetting", "onCreateView: "+ currentSoundSetting);


                    int currentNotificationSetting = saved_values.getInt("notification", 1);

                    if (currentNotificationSetting == 0){
                        AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(getContext());
                        alertDialogBuilder.setTitle("Notification Off");
                        alertDialogBuilder.setMessage("You turned off your notification!");
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show(); // Show Dialog
                        return;
                    }
                    Log.d("SoundSetting", "onCreateView: "+ currentSoundSetting);

                    if (!currentSoundSetting.equals("Fight On")) {
                        sendNotificationDefaultSound();
                    } else{
                        sendNotificationCustomizedSound();
                    }

                }
        );

        // show storage size
        // File appBaseFolder = this.getContext().getFilesDir().getParentFile();
        // long totalSize = browseFiles(appBaseFolder);
        // storageSize.setText("Storage Size: " + totalSize + " total bytes.");
        TextView storageSize = root.findViewById(R.id.text_storage);
        String storageInfo = "" + "Storage Size: " +
                getStorageSize() +
                " MB.";
        storageSize.setText(storageInfo);

        Switch notificationSwitch = (Switch) root.findViewById(R.id.switch1);

        // check current state of a Switch (true or false).
        int oldNotificationSetting = oldSavedValues.getInt("notification", -1);
        if (oldNotificationSetting != 0) {
            notificationSwitch.setChecked(true);
        }
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences saved_values = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = saved_values.edit();
                if (isChecked){
                    Log.d("TAG", "Notification Switch Changed to On");
                    editor.putInt("notification",1);
                } else{
                    Log.d("TAG", "Notification Switch Changed to Off");
                    editor.putInt("notification",0);
                }
                editor.apply();

            }
        });
        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendNotificationDefaultSound(){
        CharSequence name = getString(R.string.common_google_play_services_notification_channel_name);
        String description = getString(R.string.common_google_play_services_notification_channel_name);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("my_channel_1", name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, new Intent(), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "my_channel_1")
                .setSound(sound)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle("COVID-19 NOTIFICATION")
                .setContentText("This is a test notification!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);


        // NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendNotificationCustomizedSound(){
        String CHANNEL_ID="3469";

        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        CharSequence name = getString(R.string.common_google_play_services_notification_channel_name);
        String description = getString(R.string.common_google_play_services_notification_channel_name);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
        channel.setSound(sound, audioAttributes);

        if (notificationManager != null) {
            notificationManager.createNotificationChannel( channel );
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, new Intent(), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle("COVID-19 NOTIFICATION")
                .setContentText("This is a test notification!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setSound(sound)
                .setAutoCancel(true);


        // NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, builder.build());
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();
        Log.d("On City Item Selected", text);
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
            if (f.isDirectory()) {
                dirSize += browseFiles(f);
            }
        }
        return dirSize;
    }

    public long getStorageSize() {
        return getFileDataSize() + getFolderSize();
    }

    // Return File Size in MB
    public long getFileDataSize() {
        long size = 0;
        File filedir = this.getContext().getFilesDir().getParentFile();
        for (File fdir : filedir.listFiles()) {
            if (fdir.isDirectory()) {
                long dirfiles = browseFiles(fdir);
                size += dirfiles;
            } else {
                size += fdir.length();
            }
        }
        return size / (1024 * 1024);
    }

    // Return APP Size in MB
    public long getFolderSize() {
        try {
            long appsize = new File(getActivity().getPackageManager().getApplicationInfo(getActivity().getPackageName(), 0).sourceDir).length();
            return appsize / (1024 * 1024);
        } catch (Exception e) {
            return 0;
        }
    }
}


