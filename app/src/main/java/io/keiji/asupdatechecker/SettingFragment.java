package io.keiji.asupdatechecker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import io.keiji.asupdatechecker.service.CheckService;

public class SettingFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
    private static final String TAG = SettingFragment.class.getSimpleName();

    private static final int LOADER_ID = 0x01;

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("MM/dd HH:mm:ss");
    private static final SimpleDateFormat FORMATTER_YEAR = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    public static class CHeckUpdateLoader extends AsyncTaskLoader<UpdateState> {

        public CHeckUpdateLoader(Context context) {
            super(context);
        }

        @Override
        public UpdateState loadInBackground() {
            UpdateState result = null;
            try {
                result = Endpoint.getUpdateState();
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
            }

            return result;
        }
    }

    private final LoaderManager.LoaderCallbacks<UpdateState> mLoaderCallback = new LoaderManager.LoaderCallbacks<UpdateState>() {
        @Override
        public Loader<UpdateState> onCreateLoader(int id, Bundle args) {
            CHeckUpdateLoader loader = new CHeckUpdateLoader(getContext());
            loader.forceLoad();
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<UpdateState> loader, UpdateState data) {

            List<UpdateState.Product.Channel> updatedChannelList
                    = PreferenceUtils.checkUpdate(mSharedPreferences, data);

            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            calendar.setTimeInMillis(System.currentTimeMillis());
            mStatus.setTitle("最終確認 " + FORMATTER.format(calendar.getTime()));

            if (updatedChannelList.size() > 0) {
                StringBuffer sb = new StringBuffer();
                for (UpdateState.Product.Channel channel : updatedChannelList) {
                    UpdateState.Product.Channel.Build build = channel.builds.get(0);
                    sb.append(String.format(Locale.US, "%s - %s:%s\n",
                            channel.status, build.version, build.number));
                }
                mStatus.setSummary(sb.toString());

                CheckService.showNotification(getContext(), updatedChannelList);

                PreferenceUtils.save(mSharedPreferences, data);

            } else {
                Map<String, UpdateState.Product.Channel> channelList = data
                        .products.get(data.products.keySet().iterator().next())
                        .channels;

                StringBuffer sb = new StringBuffer();
                for (UpdateState.Product.Channel channel : channelList.values()) {
                    UpdateState.Product.Channel.Build build = channel.builds.get(0);
                    sb.append(String.format(Locale.US, "%s - %s:%s\n",
                            channel.status, build.version, build.number));
                }
                mStatus.setSummary(sb.toString());
            }
        }

        @Override
        public void onLoaderReset(Loader<UpdateState> loader) {

        }
    };

    private Preference mStatus;

    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.setting);

        Preference version = findPreference("version");
        version.setSummary("Version " + BuildConfig.VERSION_NAME);

        mStatus = findPreference("status");

        findPreference("check_update_immediately").setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        checkUpdate();
                        return false;
                    }
                });
        findPreference("auto_check").setOnPreferenceChangeListener(this);

        findPreference("license").setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        LicenseDialogFragment.newInstance().show(getFragmentManager(),
                                LicenseDialogFragment.class.getSimpleName());
                        return true;
                    }
                });

        long lastUpdate = mSharedPreferences.getLong("last_update", -1);
        if (lastUpdate < 0) {
            checkUpdate();
        } else {
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            int nowYear = calendar.get(Calendar.YEAR);

            calendar.setTimeInMillis(lastUpdate);
            int checkedYear = calendar.get(Calendar.YEAR);

            SimpleDateFormat formatter = nowYear != checkedYear ? FORMATTER_YEAR : FORMATTER;
            mStatus.setTitle("最終確認 " + formatter.format(calendar.getTime()));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        CheckService.setNextTimer(getContext());
        return true;
    }

    private void checkUpdate() {
        getLoaderManager().restartLoader(LOADER_ID, null, mLoaderCallback);
    }
}
