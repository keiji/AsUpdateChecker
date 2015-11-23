package io.keiji.asupdatechecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PreferenceUtils {

    public static List<UpdateState.Product.Channel> checkUpdate(Setting setting, UpdateState updateState) {
        List<UpdateState.Product.Channel> updatedChannel = new ArrayList<>();

        Map<String, UpdateState.Product.Channel> channelList = updateState
                .products.get(updateState.products.keySet().iterator().next())
                .channels;

        for (UpdateState.Product.Channel channel : channelList.values()) {
            String version = setting.getVersion(channel);
            String number = setting.getNumber(channel);

            UpdateState.Product.Channel.Build build = channel.builds.get(0);

            if (/* BuildConfig.DEBUG || */ !build.version.equals(version) || !build.number.equals(number)) {
                updatedChannel.add(channel);
            }
        }

        return updatedChannel;
    }

    public static void save(Setting setting, UpdateState updateState) {
        Map<String, UpdateState.Product.Channel> channelList = updateState
                .products.get(updateState.products.keySet().iterator().next())
                .channels;

        for (UpdateState.Product.Channel channel : channelList.values()) {
            UpdateState.Product.Channel.Build build = channel.builds.get(0);
            setting.set(channel, build);
        }

        setting.updateLastupdate();
    }
}
