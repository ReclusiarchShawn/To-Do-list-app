package com.example.mytodolist;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button add;
    private EditText addtext;
    private RecyclerView rv;
    private List<modelclass> list;
    private customadapter custom;
    private DB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        db=new DB(this);
        rv=findViewById(R.id.view);
        addtext=findViewById(R.id.write);
        add=findViewById(R.id.make);
        rv.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        custom = new customadapter(this, list, db);
        rv.setAdapter(custom);
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


    @Override
    protected void onResume() {
        super.onResume();
        load();
    }
}