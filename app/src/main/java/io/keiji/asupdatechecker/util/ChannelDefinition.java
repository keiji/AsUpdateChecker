package io.keiji.asupdatechecker.util;

import io.keiji.asupdatechecker.UpdateState;
import io.keiji.asupdatechecker.entity.Channel;

public enum ChannelDefinition {
    // /tools/idea/platform/platform-impl/src/com/intellij/openapi/updateSettings/impl/ChannelStatus.java
    eap("Canary"),
    milestone("Dev"),
    beta("Beta"),
    release("Stable");

    public final String label;

    ChannelDefinition(String label) {
        this.label = label;
    }

    public static String getChannelLabel(UpdateState.Product.Channel channel) {
        return ChannelDefinition.valueOf(channel.status).label;
    }

    public static String getChannelLabel(Channel channel) {
        return ChannelDefinition.valueOf(channel.getStatus()).label;
    }
}
