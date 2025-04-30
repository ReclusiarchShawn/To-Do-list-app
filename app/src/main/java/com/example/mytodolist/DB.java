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
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "TodoDB";
    private static final String TABLE_TODO = "todo_items";
    private static final String KEY_ID = "id";
    private static final String KEY_TASK = "task";
    private static final String KEY_COMPLETED = "completed";
    private static final String CREATED_TIME="created_at";
    private static final String UPDATE_AT ="date";
    private static final String SIDENOTE="sidenote";

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
                + SIDENOTE + " TEXT)";
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
        long result = db.insert(TABLE_TODO, null, cv);
        db.close();
        return result != -1;
    }


    public List<modelclass> getAllTasks() {
        List<modelclass> taskList = new ArrayList<>();
        //String query = "SELECT * FROM " + TABLE_TODO;
        String[] columns = {KEY_ID, KEY_TASK, KEY_COMPLETED, CREATED_TIME, UPDATE_AT, SIDENOTE};
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
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return taskList;
    }


    public boolean updateTask(modelclass task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_TASK, task.getText());
        cv.put(KEY_COMPLETED, task.isCompleted() ? 1 : 0);
        cv.put(UPDATE_AT,task.getDate());
        cv.put(SIDENOTE,task.getSidenote());
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
