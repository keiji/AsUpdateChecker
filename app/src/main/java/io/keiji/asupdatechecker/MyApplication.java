package io.keiji.asupdatechecker;

import android.app.Application;
import android.content.Context;

import java.io.File;

import io.keiji.asupdatechecker.entity.Migration;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MyApplication extends Application {

    private static final String REALM_FILE_NAME = "ap_list.realm";

    public static File getRealmFile(Context context) {
        return context.getDatabasePath(REALM_FILE_NAME);
    }

    public static File getRealmDir(Context context) {
        File dir = getRealmFile(context).getParentFile();
        dir.mkdirs();

        return dir;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        File realmFile = getRealmFile(this);

        RealmConfiguration.Builder builder = new RealmConfiguration
                .Builder(getRealmDir(this))
                .name(realmFile.getName())
                .setModules(Realm.getDefaultModule())
                .migration(new Migration())
                .schemaVersion(BuildConfig.VERSION_CODE);

        if (BuildConfig.DEBUG) {
            builder.deleteRealmIfMigrationNeeded();
        }

        Realm.setDefaultConfiguration(builder.build());
    }
}
