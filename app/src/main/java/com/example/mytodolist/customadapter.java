package com.example.mytodolist;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class customadapter extends RecyclerView.Adapter<customadapter.ViewHolder> {
    private final List<modelclass> list;
    private final Context context;
    private final DB db;

    public customadapter(Context context, List<modelclass> list,DB db) {
        this.context=context;
        this.list=list;
        this.db=db;
    }

    @NonNull
    @Override
    public customadapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.customtodo,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        modelclass var = list.get(position);
        holder.cb.setText(var.getText());
        holder.cb.setChecked(var.isCompleted());
        updateTaskAppearance(holder, var.isCompleted());
        holder.editconstraint.setVisibility(View.GONE);
         holder.date.setText(var.getFormattedDate());
         holder.noteconstraint.setVisibility(View.GONE);
         holder.timelayout.setVisibility(var.isNotificationEnabled() ? View.VISIBLE : View.GONE);
        holder.mainconstraint.setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
            if (holder.editconstraint.getVisibility() == View.VISIBLE) {
                holder.editconstraint.setVisibility(View.GONE);
            } else {
                holder.editconstraint.setVisibility(View.VISIBLE);
                holder.editText.setText(var.getText());
                holder.editText.requestFocus();
                ((RecyclerView) holder.itemView.getParent()).smoothScrollToPosition(position);
            }
        });
        holder.cb.setOnCheckedChangeListener(null);
        holder.cb.setChecked(var.isCompleted());
        holder.cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            var.setCompleted(isChecked);
            db.updateTask(var);
            updateTaskAppearance(holder, isChecked);
            if(isChecked&&!TextUtils.isEmpty(var.getSidenote())){
            AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("SIDE NOTE");
                builder.setMessage(var.getSidenote());
                builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                builder.show();
            }
        });
        holder.switchnotification.setChecked(var.isNotificationEnabled());
        holder.timesetting.setText(String.valueOf(var.getNotificationInterval()));
        holder.switchnotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            var.setNotificationEnabled(isChecked);
            holder.timelayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            db.updateTask(var);
            if (isChecked) {
                scheduleNotification(var);
            } else {
                cancelNotification(var);
            }
        });
        holder.settime.setOnClickListener(v -> {
            try {
                int interval = Integer.parseInt(holder.timesetting.getText().toString());
                if (interval < 1 || interval > 1440) {
                    Toast.makeText(context, "Please enter between 1 and 1440 minutes", Toast.LENGTH_SHORT).show();
                    return;
                }
                var.setNotificationInterval(interval);
                db.updateTask(var);
                // Reschedule with new interval if notifications are enabled
                if (var.isNotificationEnabled()) {
                    cancelNotification(var);
                    scheduleNotification(var);
                }
                Toast.makeText(context, "Notification interval updated", Toast.LENGTH_SHORT).show();
            }catch (NumberFormatException e){
                Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });


        holder.savebtn.setOnClickListener(v -> {
           String newdate=holder.date.getText().toString();
            String newText = holder.editText.getText().toString().trim();
            if (newText.isEmpty()) {
                Toast.makeText(context, "Task cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newText.equals(var.getText())) {
                holder.editconstraint.setVisibility(View.GONE);
                return;
            }
            var.setDate(newdate);
            var.setText(newText);
            if (db.updateTask(var)) {
                notifyItemChanged(position);
                holder.editconstraint.setVisibility(View.GONE);
                Toast.makeText(context, "Task updated", Toast.LENGTH_SHORT).show();
            }
        });

        holder.delbtn.setOnClickListener(v -> {
            new AlertDialog.Builder(context).setTitle("DELETE").setMessage("Confirm Delete?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        db.deleteTask(var.getId());
                        list.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, list.size());
                    }).setNegativeButton("Cancel", null).show();
        });
        holder.addsidenote.setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition((ViewGroup) holder.itemView);
            if(holder.isNoteShowing){
                holder.noteconstraint.setVisibility(View.GONE);
            }else{
                holder.noteconstraint.setVisibility(View.VISIBLE);
                holder.notetext.setText(var.getSidenote());
                holder.notetext.requestFocus();
                ((RecyclerView) holder.itemView.getParent()).smoothScrollToPosition(position);
            }
        });
        holder.savenote.setOnClickListener(v -> {
            String note=holder.notetext.getText().toString().trim();
            var.setSidenote(note);
            db.updateTask(var);
            holder.noteconstraint.setVisibility(View.GONE);
            holder.isNoteShowing = false;
            Toast.makeText(context, "Note saved", Toast.LENGTH_SHORT).show();
        });


    }
    private void updateTaskAppearance(ViewHolder holder, boolean isCompleted) {
        if (isCompleted) {
            holder.cb.setPaintFlags(holder.cb.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.mainconstraint.setBackgroundColor(ContextCompat.getColor(context, R.color.completed_task_bg));
        } else {
            holder.cb.setPaintFlags(holder.cb.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.mainconstraint.setBackgroundColor(ContextCompat.getColor(context, R.color.normal_task_bg));
        }
    }
    private void scheduleNotification(modelclass task) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("task_id", task.getId());
        intent.putExtra("task_text", task.getText());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                task.getId(),
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        long intervalMillis = task.getNotificationInterval() * 60 * 1000;
        long triggerAtMillis = System.currentTimeMillis() + intervalMillis;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
            );
        } else {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    intervalMillis,
                    pendingIntent
            );
        }
    }
    private void cancelNotification(modelclass task) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, AlarmReceiver.class);

            // Create the same PendingIntent that was used to schedule the alarm
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    task.getId(), // Must match the ID used when scheduling
                    intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            // Cancel the alarm
            alarmManager.cancel(pendingIntent);

            // Cancel the PendingIntent itself
            pendingIntent.cancel();

        } catch (Exception e) {
            Log.e("Notification", "Error cancelling notification for task " + task.getId(), e);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cb;
        Switch switchnotification;
         Button delbtn,savebtn, addsidenote,savenote,settime;
         EditText editText,notetext,timesetting;
         TextView date,timetext;
        boolean isNoteShowing = false;

        ConstraintLayout mainconstraint, editconstraint,noteconstraint,timelayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timelayout=itemView.findViewById(R.id.timesettingslayout);
            switchnotification=itemView.findViewById(R.id.notificationSwitch);
            timesetting=itemView.findViewById(R.id.notificationEditText);
            settime=itemView.findViewById(R.id.saveNotificationButton);
            timetext=itemView.findViewById(R.id.notificationtext);
            savenote=itemView.findViewById(R.id.savenote);
            noteconstraint=itemView.findViewById(R.id.noteconstraint);
            notetext=itemView.findViewById(R.id.note);
            addsidenote =itemView.findViewById(R.id.sidenote);
            date=itemView.findViewById(R.id.Autodate);
            mainconstraint=itemView.findViewById(R.id.mainconstraint);
            editconstraint=itemView.findViewById(R.id.editconstraint);
            cb=itemView.findViewById(R.id.checkBox);
            delbtn=itemView.findViewById(R.id.deleteButton);
            editText=itemView.findViewById(R.id.edittext);
            savebtn=itemView.findViewById(R.id.savebutton);

        }
    }
}
