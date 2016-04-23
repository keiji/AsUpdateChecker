package io.keiji.asupdatechecker.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import io.keiji.asupdatechecker.BuildConfig;
import io.keiji.asupdatechecker.R;
import io.keiji.asupdatechecker.Setting;
import io.keiji.asupdatechecker.entity.UpdateState;
import io.keiji.asupdatechecker.service.CheckService;
import io.keiji.asupdatechecker.util.Formatter;
import io.realm.Realm;
import io.realm.Sort;

public class SettingFragment extends PreferenceFragment implements CheckService.Callback {
    private static final String TAG = SettingFragment.class.getSimpleName();

    private Preference mCheckUpdateImmediately;

    private Setting mSetting;

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    private boolean mBound;
    private CheckService mService;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            CheckService.LocalBinder binder = (CheckService.LocalBinder) service;
            mService = binder.getService();
            mService.setCallback(SettingFragment.this);
            mBound = true;

            showLatestUpdateState();
            mCheckUpdateImmediately.setEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mCheckUpdateImmediately.setEnabled(false);
            mBound = false;
        }
    };

    private Preference mStatus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSetting = Setting.getInstance(getActivity());

        addPreferencesFromResource(R.xml.setting);

        Preference version = findPreference("version");
        version.setSummary("Version " + BuildConfig.VERSION_NAME + " " + BuildConfig.FLAVOR);

        mStatus = findPreference("status");

        mCheckUpdateImmediately = findPreference("check_update_immediately");
        mCheckUpdateImmediately.setOnPreferenceClickListener(preference -> {
            mService.checkUpdate();
            return false;
        });
        mCheckUpdateImmediately.setEnabled(false);

        CheckBoxPreference autoCheck = (CheckBoxPreference) findPreference("auto_check");
        autoCheck.setOnPreferenceChangeListener((preference, o) -> {
            CheckService.setNextTimer(getActivity(), (Boolean) o);
            return true;
        });
        findPreference("auto_check_interval").setOnPreferenceChangeListener((preference, o) -> {
            CheckService.setNextTimer(getActivity(), autoCheck.isChecked(), (String) o);
            return true;
        });

        findPreference("license").setOnPreferenceClickListener(preference -> {
            LicenseDialogFragment.newInstance().show(getFragmentManager(),
                    LicenseDialogFragment.class.getSimpleName());
            return true;
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        Intent intent = new Intent(getActivity(), CheckService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mBound) {
            mService.setCallback(null);
            getActivity().unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onCheckCompleted() {
        showLatestUpdateState();
    }

    private void showLatestUpdateState() {
        mStatus.setTitle(getText(R.string.last_check_at) + " "
                + Formatter.MONTH_DAY.format(mSetting.getLastupdate()));

        Realm realm = Realm.getDefaultInstance();

        if (realm.where(UpdateState.class).count() > 0) {
            UpdateState updateState = realm.where(UpdateState.class)
                    .findAllSorted("time", Sort.DESCENDING)
                    .first();

            mStatus.setSummary(updateState.getSummary(false, " "));
        } else {
            mService.checkUpdate();
        }

        realm.close();
    }
}
