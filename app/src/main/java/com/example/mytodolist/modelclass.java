package com.example.mytodolist;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class modelclass {
    private int id;
    private String text;
    private boolean isCompleted;
    private String date;
    private String sidenote;

    public modelclass(String text,String date,String sidenote) {
        this.text = text;
        this.date=date;
        this.sidenote=sidenote != null ? sidenote : "";
        this.isCompleted = false;
    }
    public String getSidenote(){
        return sidenote!=null?sidenote:"";
    }
    public void setSidenote(String sidenote){
        this.sidenote=sidenote != null ? sidenote.trim() : "";
    }
    public String getDate(){
        return date;
    }
    public void setDate(String date){
        this.date=date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
    public String getcurrentdatetime(){
        SimpleDateFormat newformat=new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        Date date=new Date();
        return newformat.format(date);
    }
    public String getFormattedDate() {
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
            Date dateObj = dbFormat.parse(this.date);
            return displayFormat.format(dateObj);
        } catch (Exception e) {
            return date;
        }
    }
}
