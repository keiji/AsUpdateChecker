package io.keiji.asupdatechecker.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.keiji.asupdatechecker.Endpoint;
import io.keiji.asupdatechecker.PreferenceUtils;
import io.keiji.asupdatechecker.R;
import io.keiji.asupdatechecker.UpdateState;

public class CheckService extends IntentService {
    private static final String TAG = CheckService.class.getSimpleName();

    private static final String DEFAULT_INTERVAL = String.valueOf(6 * 60 * 60);

    public static final int REQUEST_CODE = 0x01;

    public static Intent newIntent(Context context) {
        return new Intent(context, CheckService.class);
    }

    public CheckService() {
        super(CheckService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "starting Update Check...");

        SharedPreferences sharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        try {
            UpdateState data = Endpoint.getUpdateState();

            List<UpdateState.Product.Channel> updatedChannelList
                    = PreferenceUtils.checkUpdate(sharedPreferences, data);

            if (updatedChannelList.size() > 0) {

                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                        .setTicker("新しいAndroid Studioがリリースされました")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setDefaults(Notification.DEFAULT_VIBRATE);

                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle(builder);
                inboxStyle.setBigContentTitle("新しいAndroid Studioがリリースされました");

                for (UpdateState.Product.Channel channel : updatedChannelList) {
                    UpdateState.Product.Channel.Build build = channel.builds.get(0);
                    inboxStyle.addLine(String.format(Locale.US, "%s - %s:%s\n",
                            channel.status, build.version, build.number));
                }

                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.notify(0x01, builder.build());

                PreferenceUtils.save(sharedPreferences, data);
            }

        } catch (IOException e) {
            Log.e(TAG, "IOException", e);

        }

        setNextTimer(getApplicationContext());
    }

    public static void setNextTimer(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String intervalString = sharedPreferences.getString("auto_check_interval", DEFAULT_INTERVAL);
        long interval = Long.parseLong(intervalString) * 1000;

        Intent service = CheckService.newIntent(context);
        PendingIntent pendingIntent = PendingIntent.getService(context,
                CheckService.REQUEST_CODE, service, 0x0);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        am.cancel(pendingIntent);

        if (sharedPreferences.getBoolean("auto_check", false)) {
            am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval, pendingIntent);
        }
    }

    public static void showNotification(Context context, List<UpdateState.Product.Channel> updatedChannelList) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle("New Android Studio")
                .setContentText("New Android Studio version available")
                .setTicker("New Android Studio version available")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(Notification.DEFAULT_VIBRATE);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle(builder);
        inboxStyle.setBigContentTitle("New Android Studio version available");

        for (UpdateState.Product.Channel channel : updatedChannelList) {
            UpdateState.Product.Channel.Build build = channel.builds.get(0);
            inboxStyle.addLine(String.format(Locale.US, "%s in %s channel.\n",
                    build.version, channel.status));
        }

        NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        nm.notify(0x01, builder.build());
    }
}
