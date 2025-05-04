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
    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "TodoDB";
    private static final String TABLE_TODO = "todo_items";
    private static final String KEY_ID = "id";
    private static final String KEY_TASK = "task";
    private static final String KEY_COMPLETED = "completed";
    private static final String CREATED_TIME="created_at";
    private static final String UPDATE_AT ="date";
    private static final String SIDENOTE="sidenote";
    private static final String NOTIFICATION_ENABLED = "notification_enabled";
    private static final String NOTIFICATION_INTERVAL = "notification_interval";


    public DB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_TODO + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TASK + " TEXT,"
                + KEY_COMPLETED + " INTEGER DEFAULT 0,"
                + CREATED_TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + UPDATE_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + SIDENOTE + " TEXT,"
                + NOTIFICATION_ENABLED + " INTEGER DEFAULT 0,"
                + NOTIFICATION_INTERVAL + " INTEGER DEFAULT 15)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO);
        onCreate(db);
    }


    public boolean addTask(modelclass task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_TASK, task.getText());
        cv.put(KEY_COMPLETED, task.isCompleted() ? 1 : 0);
        cv.put(UPDATE_AT, task.getDate());
        cv.put(CREATED_TIME,task.getcurrentdatetime());
        cv.put(SIDENOTE,task.getSidenote());
        cv.put(NOTIFICATION_ENABLED, task.isNotificationEnabled() ? 1 : 0);
        cv.put(NOTIFICATION_INTERVAL, task.getNotificationInterval());
        long result = db.insert(TABLE_TODO, null, cv);
        db.close();
        return result != -1;
    }


    public List<modelclass> getAllTasks() {
        List<modelclass> taskList = new ArrayList<>();
        //String query = "SELECT * FROM " + TABLE_TODO;
        String[] columns = {KEY_ID, KEY_TASK, KEY_COMPLETED, CREATED_TIME, UPDATE_AT, SIDENOTE, NOTIFICATION_ENABLED, NOTIFICATION_INTERVAL};
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_TODO, columns, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                modelclass task = new modelclass(cursor.getString(1),
                        cursor.getString(4),
                        cursor.getString(5));
                task.setId(cursor.getInt(0));
                task.setCompleted(cursor.getInt(2) == 1);
                task.setDate(cursor.getString(3));
                task.setNotificationEnabled(cursor.getInt(6) == 1);
                task.setNotificationInterval(cursor.getInt(7));
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }
    public modelclass getTaskId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        modelclass task = null;
        Cursor cursor = db.query(TABLE_TODO,
                new String[]{KEY_ID, KEY_TASK, KEY_COMPLETED, CREATED_TIME, UPDATE_AT, SIDENOTE, NOTIFICATION_ENABLED, NOTIFICATION_INTERVAL},
                KEY_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            task = new modelclass(
                    cursor.getString(1), // task text
                    cursor.getString(4), // update_at (date)
                    cursor.getString(5)  // sidenote
            );
            task.setId(cursor.getInt(0));
            task.setCompleted(cursor.getInt(2) == 1);
            task.setNotificationEnabled(cursor.getInt(6) == 1);
            task.setNotificationInterval(cursor.getInt(7));
            cursor.close();
        }

        db.close();
        return task;
    }


    public boolean updateTask(modelclass task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_TASK, task.getText());
        cv.put(KEY_COMPLETED, task.isCompleted() ? 1 : 0);
        cv.put(UPDATE_AT,task.getDate());
        cv.put(SIDENOTE,task.getSidenote());
        cv.put(NOTIFICATION_ENABLED, task.isNotificationEnabled() ? 1 : 0);
        cv.put(NOTIFICATION_INTERVAL, task.getNotificationInterval());
        int result = db.update(TABLE_TODO, cv, KEY_ID + " = ?", new String[]{String.valueOf(task.getId())});
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