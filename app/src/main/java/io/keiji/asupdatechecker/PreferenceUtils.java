package io.keiji.asupdatechecker;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PreferenceUtils {

    private static final String KEY_VERSION = "_version";
    private static final String KEY_NUMBER = "_number";

    public static List<UpdateState.Product.Channel> checkUpdate(SharedPreferences pref, UpdateState updateState) {
        List<UpdateState.Product.Channel> updatedChannel = new ArrayList<>();

        Map<String, UpdateState.Product.Channel> channelList = updateState
                .products.get(updateState.products.keySet().iterator().next())
                .channels;

        for (UpdateState.Product.Channel channel : channelList.values()) {
            String version = pref.getString(channel.status + KEY_VERSION, "");
            String number = pref.getString(channel.status + KEY_NUMBER, "");

            UpdateState.Product.Channel.Build build = channel.builds.get(0);

            if (/* BuildConfig.DEBUG || */ !build.version.equals(version) || !build.number.equals(number)) {
                updatedChannel.add(channel);
            }
        }

        return updatedChannel;
    }

    public static void save(SharedPreferences pref, UpdateState updateState) {
        Map<String, UpdateState.Product.Channel> channelList = updateState
                .products.get(updateState.products.keySet().iterator().next())
                .channels;

        SharedPreferences.Editor editor = pref.edit();

        for (UpdateState.Product.Channel channel : channelList.values()) {
            UpdateState.Product.Channel.Build build = channel.builds.get(0);

            editor.putString(channel.status + KEY_VERSION, build.version);
            editor.putString(channel.status + KEY_NUMBER, build.number);
        }

        editor.putLong("last_update", System.currentTimeMillis())
                .apply();
    }
}
