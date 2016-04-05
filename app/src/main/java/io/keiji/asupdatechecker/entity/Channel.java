package io.keiji.asupdatechecker.entity;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Channel extends RealmObject {

    private boolean update;

    private String id;
    private int majorVersion;
    private String status;
    private RealmList<Build> builds = new RealmList<>();

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public RealmList<Build> getBuilds() {
        return builds;
    }

    public void setBuilds(RealmList<Build> builds) {
        this.builds = builds;
    }

    public Channel copyFrom(io.keiji.asupdatechecker.UpdateState.Product.Channel channel) {
        setId(channel.id);
        setMajorVersion(channel.majorVersion);
        setStatus(channel.status);

        return this;
    }
}
