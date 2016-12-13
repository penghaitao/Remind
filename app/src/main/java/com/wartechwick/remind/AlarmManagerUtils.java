package com.wartechwick.remind;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.wartechwick.remind.service.AlarmReceiver;

import static android.app.AlarmManager.INTERVAL_FIFTEEN_MINUTES;
import static android.content.Context.ALARM_SERVICE;

public class AlarmManagerUtils {

    public static void register(Context context) {

        Intent intent = new Intent("com.wartechwick.remind.alarm");
        intent.setClass(context, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
//        PendingIntent broadcast = PendingIntent.getBroadcast(context, 520, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

//        manager.set(AlarmManager.RTC_WAKEUP, today.getTimeInMillis(), broadcast);
        manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + INTERVAL_FIFTEEN_MINUTES, INTERVAL_FIFTEEN_MINUTES, alarmIntent);

    }

    public static void unRegister(Context context) {
        Intent intent = new Intent("com.wartechwick.remind.alarm");
        intent.setClass(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        alarmManager.cancel(sender);
    }
}
