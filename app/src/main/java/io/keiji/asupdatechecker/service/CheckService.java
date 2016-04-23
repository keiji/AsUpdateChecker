package io.keiji.asupdatechecker.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.keiji.asupdatechecker.Endpoint;
import io.keiji.asupdatechecker.R;
import io.keiji.asupdatechecker.Setting;
import io.keiji.asupdatechecker.UpdateState;
import io.keiji.asupdatechecker.entity.Build;
import io.keiji.asupdatechecker.entity.Channel;
import io.keiji.asupdatechecker.entity.Product;
import io.keiji.asupdatechecker.ui.MainActivity;
import io.realm.Realm;
import io.realm.Sort;

public class CheckService extends Service {
    private static final String TAG = CheckService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 0x01;

    public static final int REQUEST_CODE = 0x01;

    private final Callback DUMMY_CALLBACK = () -> Log.d(TAG, "onCheckCompleted dummy");

    private Callback mCallback = DUMMY_CALLBACK;

    public void setCallback(Callback callback) {
        if (callback == null) {
            mCallback = DUMMY_CALLBACK;
            return;
        }

        mCallback = callback;
    }

    private final Handler mHandler = new Handler();

    public static Intent newIntent(Context context) {
        return new Intent(context, CheckService.class);
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public CheckService getService() {
            return CheckService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private Setting mSetting;
    private ExecutorService mExecutorService;

    @Override
    public void onCreate() {
        super.onCreate();

        mSetting = Setting.getInstance(this);
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mExecutorService.shutdown();
    }

    private final Runnable mCheckRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "starting Update Check...");

            Endpoint.EndpointResult result = Endpoint.getUpdateState();

            if (result.updateState == null) {
                return;
            }

            Realm realm = Realm.getDefaultInstance();

            boolean updated = false;

            UPDATED:
            for (UpdateState.Product product : result.updateState.products) {
                for (UpdateState.Product.Channel channel : product.channels) {
                    for (UpdateState.Product.Channel.Build build : channel.builds) {
                        if (realm.where(Build.class)
                                .equalTo("channelId", channel.id)
                                .equalTo("number", build.number)
                                .equalTo("version", build.version)
                                .count() == 0) {
                            updated = true;
                            break UPDATED;
                        }
                    }
                }
            }

            if (updated) {
                saveUpdateState(realm, result.updateState);

                io.keiji.asupdatechecker.entity.UpdateState latestUpdateState
                        = realm.where(io.keiji.asupdatechecker.entity.UpdateState.class)
                        .findAllSorted("time", Sort.DESCENDING)
                        .first();
                showNotification(CheckService.this, latestUpdateState);
            }

            realm.close();

            mSetting.updateLastupdate();

            mHandler.post(() -> mCallback.onCheckCompleted());
        }
    };

    @NonNull
    private List<Channel> saveUpdateState(Realm realm, UpdateState data) {
        realm.beginTransaction();

        io.keiji.asupdatechecker.entity.UpdateState updateState
                = new io.keiji.asupdatechecker.entity.UpdateState();
        updateState.setTime(System.currentTimeMillis());

        updateState = realm.copyToRealm(updateState);

        List<Channel> updatedChannelList = new ArrayList<>();

        for (UpdateState.Product product : data.products) {
            Product productData = realm.copyToRealm(new Product().copyFrom(product));
            updateState.getProducts().add(productData);

            for (UpdateState.Product.Channel channel : product.channels) {
                Channel channelData = realm.copyToRealm(new Channel().copyFrom(channel));
                productData.getChannels().add(channelData);

                for (UpdateState.Product.Channel.Build build : channel.builds) {

                    // buildのアップデートを上位にも記録
                    if (realm.where(Build.class)
                            .equalTo("channelId", channel.id)
                            .equalTo("number", build.number)
                            .equalTo("version", build.version)
                            .count() == 0) {
                        productData.setUpdate(true);
                        channelData.setUpdate(true);
                    }

                    Build buildData = realm.copyToRealm(new Build().copyFrom(build));
                    buildData.setChannelId(channel.id);
                    channelData.getBuilds().add(buildData);
                }

                if (channelData.isUpdate()) {
                    updatedChannelList.add(channelData);
                }
            }
        }

        realm.commitTransaction();

        return updatedChannelList;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        checkUpdate();

        setNextTimer(getApplicationContext());

        return super.onStartCommand(intent, flags, startId);
    }

    public void checkUpdate() {
        mExecutorService.submit(mCheckRunnable);
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
        if (!autoCheck) {
            return;
        }

        long interval = Long.parseLong(intervalString) * 1000;

        Log.d(TAG, "try set Update Check..." + interval / 1000);

        Intent service = CheckService.newIntent(context);
        PendingIntent pendingIntent = PendingIntent.getService(context,
                CheckService.REQUEST_CODE, service, 0x0);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        am.cancel(pendingIntent);

        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + interval, pendingIntent);

        Log.d(TAG, "set Update Check..." + SystemClock.elapsedRealtime() + interval);
    }

    public static void showNotification(Context context,
                                        io.keiji.asupdatechecker.entity.UpdateState updateState) {

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

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle(builder)
                .setBigContentTitle(context.getText(R.string.new_android_studio_version_available));
        builder.setStyle(inboxStyle);

        String[] lines = updateState.getSummary(true, null).split("\n");

        for (String line : lines) {
            inboxStyle.addLine(line);
        }

        NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, builder.build());
    }

    public interface Callback {
        void onCheckCompleted();
    }
}
