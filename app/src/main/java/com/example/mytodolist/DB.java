package com.example.mytodolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DB extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "TodoDB";
    private static final String TABLE_TODO = "todo_items";
    private static final String KEY_ID = "id";
    private static final String KEY_TASK = "task";
    private static final String KEY_COMPLETED = "completed";

    public DB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_TODO + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TASK + " TEXT,"
                + KEY_COMPLETED + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO);
        onCreate(db);
    }


    public boolean addTask(modelclass task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TASK, task.getText());
        values.put(KEY_COMPLETED, task.isCompleted() ? 1 : 0);
        long result = db.insert(TABLE_TODO, null, values);
        db.close();
        return result != -1;
    }


    public List<modelclass> getAllTasks() {
        List<modelclass> taskList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TODO;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                modelclass task = new modelclass(cursor.getString(1));
                task.setId(cursor.getInt(0));
                task.setCompleted(cursor.getInt(2) == 1);
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }


    public boolean updateTask(modelclass task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TASK, task.getText());
        values.put(KEY_COMPLETED, task.isCompleted() ? 1 : 0);
        int result = db.update(TABLE_TODO, values, KEY_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
        return result > 0;
    }


    public boolean deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows=db.delete(TABLE_TODO, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows>0;
    }
}
