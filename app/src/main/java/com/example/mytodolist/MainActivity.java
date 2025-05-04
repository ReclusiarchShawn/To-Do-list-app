package com.example.mytodolist;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.Manifest;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    private Button add;
    private EditText addtext;
    private RecyclerView rv;
    private List<modelclass> list;
    private customadapter custom;
    private DB db;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        NotificationHelper.createNotificationChannel(this);


        db=new DB(this);
        rv=findViewById(R.id.view);
        addtext=findViewById(R.id.write);
        add=findViewById(R.id.make);
        rv.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        custom = new customadapter(this, list, db);
        rv.setAdapter(custom);
        checkAndRequestPermissions();
        load();
        add.setOnClickListener(v -> {
            String taskText = addtext.getText().toString().trim();
            if (!taskText.isEmpty()) {
                modelclass newTask = new modelclass(taskText,"","");
                if (db.addTask(newTask)) {
                    addtext.setText("");
                    load();
                } else {
                    Toast.makeText(this, "Failed to add task", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter a task", Toast.LENGTH_SHORT).show();
            }

        });

    }

    private void load(){
    list.clear();
    list.addAll(db.getAllTasks());
    custom.notifyDataSetChanged();
    }
    private void checkAndRequestPermissions() {
        // For Android 13+ notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        // For Android 12+ exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                showExactAlarmPermissionDialog();
            }
        }
    }
        private void showExactAlarmPermissionDialog() {
            new AlertDialog.Builder(this)
                    .setTitle("Exact Alarm Permission Needed")
                    .setMessage("This app needs exact alarm permission to remind you of tasks at the exact time you set.")
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        startActivity(intent);
                    })
                    .setNegativeButton("Later", null)
                    .show();
        }





        @Override
    protected void onResume() {
        super.onResume();
            // Re-check permissions when returning to app
            checkAndRequestPermissions();
        load();
    }
}