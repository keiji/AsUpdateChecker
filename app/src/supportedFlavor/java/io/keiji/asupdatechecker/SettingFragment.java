package io.keiji.asupdatechecker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import io.keiji.asupdatechecker.service.CheckService;

public class SettingFragment extends PreferenceFragmentCompat {
    private static final String TAG = SettingFragment.class.getSimpleName();

    private static final int LOADER_ID = 0x01;

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("MM/dd HH:mm:ss");
    private static final SimpleDateFormat FORMATTER_YEAR = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    public static class CheckUpdateLoader extends AsyncTaskLoader<EndpointResult> {

        public CheckUpdateLoader(Context context) {
            super(context);
        }

        @Override
        public EndpointResult loadInBackground() {
            EndpointResult result = null;
            try {
                result = new EndpointResult(Endpoint.getUpdateState());
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
                result = new EndpointResult(e);
            }

            return result;
        }
    }

    private final LoaderManager.LoaderCallbacks<EndpointResult> mLoaderCallback
            = new LoaderManager.LoaderCallbacks<EndpointResult>() {
        @Override
        public Loader<EndpointResult> onCreateLoader(int id, Bundle args) {
            CheckUpdateLoader loader = new CheckUpdateLoader(getContext());
            loader.forceLoad();
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<EndpointResult> loader, EndpointResult data) {
            if (data.exception != null) {
                mStatus.setTitle(data.exception.getMessage());
                return;
            }

            List<UpdateState.Product.Channel> updatedChannelList
                    = PreferenceUtils.checkUpdate(mSharedPreferences, data.updateState);

            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            calendar.setTimeInMillis(System.currentTimeMillis());
            mStatus.setTitle(getText(R.string.last_check_at) + " " + FORMATTER.format(calendar.getTime()));

            String format = getContext().getString(R.string.state_format);

            StringBuffer sb = new StringBuffer();

            if (updatedChannelList.size() > 0) {
                CheckService.showNotification(getContext(), updatedChannelList);
            } else {
                updatedChannelList = new ArrayList<>(data.updateState
                        .products.get(data.updateState.products.keySet().iterator().next())
                        .channels.values());
            }

            for (UpdateState.Product.Channel channel : updatedChannelList) {
                UpdateState.Product.Channel.Build build = channel.builds.get(0);
                sb.append(String.format(Locale.US, format, build.version, channel.status))
                        .append('\n');
            }

            sb.delete(sb.length() - 1, sb.length());

            mStatus.setSummary(sb.toString());

            PreferenceUtils.save(mSharedPreferences, data.updateState);
        }

        @Override
        public void onLoaderReset(Loader<EndpointResult> loader) {

        }
    };

    private Preference mStatus;

    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.setting);

        Preference version = findPreference("version");
        version.setSummary("Version " + BuildConfig.VERSION_NAME + " " + BuildConfig.FLAVOR);

        mStatus = findPreference("status");

        findPreference("check_update_immediately").setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        checkUpdate();
                        return false;
                    }
                });

        final CheckBoxPreference autoCheck = (CheckBoxPreference) findPreference("auto_check");
        autoCheck.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                CheckService.setNextTimer(getContext(), (Boolean) o);
                return true;
            }
        });
        findPreference("auto_check_interval").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                CheckService.setNextTimer(getContext(), autoCheck.isChecked(), (String) o);
                return true;
            }
        });

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
            mStatus.setTitle(getText(R.string.last_check_at) + " "
                    + formatter.format(calendar.getTime()));
        }
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private void checkUpdate() {
        getLoaderManager().restartLoader(LOADER_ID, null, mLoaderCallback);
    }
}
