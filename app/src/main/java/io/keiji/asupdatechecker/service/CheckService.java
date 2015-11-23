package io.keiji.asupdatechecker.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.keiji.asupdatechecker.Endpoint;
import io.keiji.asupdatechecker.MainActivity;
import io.keiji.asupdatechecker.PreferenceUtils;
import io.keiji.asupdatechecker.R;
import io.keiji.asupdatechecker.Setting;
import io.keiji.asupdatechecker.UpdateState;

public class CheckService extends IntentService {
    private static final String TAG = CheckService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 0x01;

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

        Setting setting = Setting.getInstance(getApplicationContext());

        try {
            UpdateState data = Endpoint.getUpdateState();

            List<UpdateState.Product.Channel> updatedChannelList
                    = PreferenceUtils.checkUpdate(setting, data);

            if (updatedChannelList.size() > 0) {
                showNotification(getApplicationContext(), updatedChannelList);
            }

            PreferenceUtils.save(setting, data);

        } catch (IOException e) {
            Log.e(TAG, "IOException", e);

        }

        setNextTimer(getApplicationContext());
    }

    public static void setNextTimer(Context context) {
        Setting setting = Setting.getInstance(context);
        setNextTimer(context, setting.isAutoCheckEnabled());
    }

    public static void setNextTimer(Context context, boolean autoCheck) {
        Setting setting = Setting.getInstance(context);
        setNextTimer(context, autoCheck, setting.getCheckInterval());
    }

    public static void setNextTimer(Context context, boolean autoCheck, String intervalString) {
        long interval = Long.parseLong(intervalString) * 1000;

        Log.d(TAG, "try set Update Check..." + interval / 1000);

        Intent service = CheckService.newIntent(context);
        PendingIntent pendingIntent = PendingIntent.getService(context,
                CheckService.REQUEST_CODE, service, 0x0);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        am.cancel(pendingIntent);

        if (autoCheck) {
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + interval, pendingIntent);

            Log.d(TAG, "set Update Check..." + SystemClock.elapsedRealtime() + interval);

        }
    }

    public static void showNotification(Context context, List<UpdateState.Product.Channel> updatedChannelList) {
        if (updatedChannelList.size() == 0) {
            return;
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent activity = PendingIntent.getActivity(
                context, MainActivity.REQUEST_CODE, intent, 0x0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getText(R.string.new_android_studio))
                .setContentText(context.getText(R.string.new_android_studio_version_available))
                .setTicker(context.getText(R.string.new_android_studio_version_available))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(activity);

        String format = context.getString(R.string.state_format);

        if (updatedChannelList.size() == 1) {
            UpdateState.Product.Channel channel = updatedChannelList.get(0);
            UpdateState.Product.Channel.Build build = channel.builds.get(0);
            builder.setContentText(String.format(Locale.US, format,
                    build.version, channel.status));
        } else {
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle(builder)
                    .setBigContentTitle(context.getText(R.string.new_android_studio_version_available));

            for (UpdateState.Product.Channel channel : updatedChannelList) {
                UpdateState.Product.Channel.Build build = channel.builds.get(0);
                inboxStyle.addLine(String.format(Locale.US, format,
                        build.version, channel.status));
            }
        }

        NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, builder.build());
    }
}
