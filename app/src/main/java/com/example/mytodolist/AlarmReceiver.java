package com.example.mytodolist;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra("task_id", -1);
        String taskText = intent.getStringExtra("task_text");

        DB db = new DB(context);
        modelclass task = db.getTaskId(taskId);
        db.close();

        if (task != null && task.isNotificationEnabled()) {
            cancelAlarm(context, taskId);
            return;
        }
        // Show notification
        NotificationHelper.showNotification(
                context,
                "Task Reminder",
                taskText
        );
        // Reschedule with the task's custom interval
        rescheduleAlarm(context, intent, task);
    }
    private void cancelAlarm(Context context, int taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    // Reschedule with the task's custom interval
    private void rescheduleAlarm(Context context, Intent originalIntent, modelclass task) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                task.getId(),
                originalIntent,
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
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
            );
        }
    }
}

