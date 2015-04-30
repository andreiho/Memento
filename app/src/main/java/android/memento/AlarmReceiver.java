package android.memento;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private static final int reqCode = 2;

    @Override
    public void onReceive(Context context, Intent intent) {

        showNotification(context);
    }

    public void showNotification(Context context) {

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, reqCode, intent, 0);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String data = prefs.getString("memo_alarm", "Default value.");

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notifications_on)
                .setColor(Color.parseColor("#FF4081"))
                .setContentTitle("Remember to...")
                .setContentText(data);

        mBuilder.setContentIntent(pi);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(reqCode, mBuilder.build());

    }

}
