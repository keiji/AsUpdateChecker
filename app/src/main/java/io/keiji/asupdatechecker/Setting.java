package io.keiji.asupdatechecker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Setting {

    private static final String KEY_VERSION = "_version";
    private static final String KEY_NUMBER = "_number";

    private static final String DEFAULT_INTERVAL = String.valueOf(6 * 60 * 60);

    private final SharedPreferences mSharedPreferences;

    public static Setting getInstance(Context context) {
        return new Setting(context);
    }

    private Setting(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void set(UpdateState.Product.Channel channel, UpdateState.Product.Channel.Build build) {

        mSharedPreferences
                .edit()
                .putString(channel.id + KEY_VERSION, build.version)
                .putString(channel.id + KEY_NUMBER, build.number)
                .commit();
    }

    public String getVersion(UpdateState.Product.Channel channel) {
        return mSharedPreferences.getString(channel.id + KEY_VERSION, "");
    }

    public String getNumber(UpdateState.Product.Channel channel) {
        return mSharedPreferences.getString(channel.id + KEY_NUMBER, "");
    }

    public void updateLastupdate() {
        mSharedPreferences.edit()
                .putLong("last_update", System.currentTimeMillis())
                .commit();
    }

    public boolean isAutoCheckEnabled() {
        return mSharedPreferences.getBoolean("auto_check", false);
    }

    public long getLastupdate() {
        return mSharedPreferences.getLong("last_update", -1);
    }

    public String getCheckInterval() {
        return mSharedPreferences.getString("auto_check_interval", DEFAULT_INTERVAL);
    }
}
