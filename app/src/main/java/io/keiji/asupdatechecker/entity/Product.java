package io.keiji.asupdatechecker.entity;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Product extends RealmObject {

    private boolean update;

    private String name;
    private RealmList<Channel> channels = new RealmList<>();

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmList<Channel> getChannels() {
        return channels;
    }

    public void setChannels(RealmList<Channel> channels) {
        this.channels = channels;
    }

    public Product copyFrom(io.keiji.asupdatechecker.UpdateState.Product product) {
        setName(product.name);
        return this;
    }
}
