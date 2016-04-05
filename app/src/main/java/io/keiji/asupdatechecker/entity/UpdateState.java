package io.keiji.asupdatechecker.entity;

import android.text.TextUtils;

import io.keiji.asupdatechecker.util.ChannelDefinition;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmQuery;

public class UpdateState extends RealmObject {

    private long time;

    private RealmList<Product> products = new RealmList<>();
    private int mSummary;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public RealmList<Product> getProducts() {
        return products;
    }

    public void setProducts(RealmList<Product> products) {
        this.products = products;
    }

    public String getSummary(boolean updatedOnly, String indent) {
        if (TextUtils.isEmpty(indent)) {
            indent = "";
        }

        StringBuilder sb = new StringBuilder();

        RealmQuery<Product> products = getProducts().where();
        if (updatedOnly) {
            products = products.equalTo("update", true);
        }

        for (Product product : products.findAll()) {

            RealmQuery<Channel> channels = product.getChannels().where();
            if (updatedOnly) {
                channels = channels.equalTo("update", true);
            }

            for (Channel channel : channels.findAll()) {
                sb.append(indent);

                RealmQuery<Build> builds = channel.getBuilds().where();

                for (Build build : builds.findAll()) {
                    sb.append(indent).append(build.getVersion());
                }

                sb.append(" - ")
                        .append(ChannelDefinition.getChannelLabel(channel))
                        .append(indent).append('(').append(channel.getId()).append(')')
                        .append('\n');

            }
        }

        sb.delete(sb.length() - 1, sb.length());

        return sb.toString();
    }
}
