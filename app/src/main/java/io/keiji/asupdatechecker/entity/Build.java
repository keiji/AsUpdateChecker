package io.keiji.asupdatechecker.entity;

import io.keiji.asupdatechecker.UpdateState;
import io.realm.Realm;
import io.realm.RealmObject;

public class Build extends RealmObject {

    private String channelId;

    private String number;
    private String version;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Build copyFrom(UpdateState.Product.Channel.Build build) {
        setNumber(build.number);
        setVersion(build.version);

        return this;
    }
}
