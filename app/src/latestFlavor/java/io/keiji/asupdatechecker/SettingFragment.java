package io.keiji.asupdatechecker;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import io.keiji.asupdatechecker.service.CheckService;

public class SettingFragment extends PreferenceFragment {
    private static final String TAG = SettingFragment.class.getSimpleName();

    private static final int LOADER_ID = 0x01;

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    public static class CHeckUpdateLoader extends AsyncTaskLoader<EndpointResult> {

        public CHeckUpdateLoader(Context context) {
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
            CHeckUpdateLoader loader = new CHeckUpdateLoader(getActivity());
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
                    = PreferenceUtils.checkUpdate(mSetting, data.updateState);

            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            calendar.setTimeInMillis(System.currentTimeMillis());
            mStatus.setTitle(getText(R.string.last_check_at) + " "
                    + Formatter.MONTH_DAY.format(calendar.getTime()));

            String format = getActivity().getString(R.string.state_format);

            StringBuilder sb = new StringBuilder();

            if (updatedChannelList.size() > 0) {
                CheckService.showNotification(getActivity(), updatedChannelList);
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

            PreferenceUtils.save(mSetting, data.updateState);
        }

        @Override
        public void onLoaderReset(Loader<EndpointResult> loader) {

        }
    };

    private Preference mStatus;

    private Setting mSetting;

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
                CheckService.setNextTimer(getActivity(), (Boolean) o);
                return true;
            }
        });
        findPreference("auto_check_interval").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                CheckService.setNextTimer(getActivity(), autoCheck.isChecked(), (String) o);
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
    }

    @Override
    public void onStart() {
        super.onStart();

        mSetting = Setting.getInstance(getActivity());

        long lastUpdate = mSetting.getLastupdate();
        if (lastUpdate < 0) {
            checkUpdate();
        } else {
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            int nowYear = calendar.get(Calendar.YEAR);

            calendar.setTimeInMillis(lastUpdate);
            int checkedYear = calendar.get(Calendar.YEAR);

            SimpleDateFormat formatter = nowYear != checkedYear
                    ? Formatter.YEAR_MONTH_DAY : Formatter.MONTH_DAY;
            mStatus.setTitle(getText(R.string.last_check_at) + " "
                    + formatter.format(calendar.getTime()));
        }
    }

    private void checkUpdate() {
        getLoaderManager().restartLoader(LOADER_ID, null, mLoaderCallback);
    }
}
