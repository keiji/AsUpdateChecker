package io.keiji.asupdatechecker;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.keiji.asupdatechecker.service.CheckService;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int LOADER_ID = 0x01;

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
            CHeckUpdateLoader loader = new CHeckUpdateLoader(MainActivity.this);
            loader.forceLoad();
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<UpdateState> loader, UpdateState data) {

            List<UpdateState.Product.Channel> updatedChannelList
                    = PreferenceUtils.checkUpdate(mSharedPreferences, data);

            if (updatedChannelList.size() > 0) {

                mState.setText("新しいAndroid Studioがリリースされました！\n");

                for (UpdateState.Product.Channel channel : updatedChannelList) {
                    UpdateState.Product.Channel.Build build = channel.builds.get(0);
                    mState.append(String.format(Locale.US, "%s - %s:%s\n",
                            channel.status, build.version, build.number));
                }

                ////

                CheckService.showNotification(getApplicationContext(), updatedChannelList);

                ////

                PreferenceUtils.save(mSharedPreferences, data);
            } else {
                mState.setText("アップデートはありません\n");
            }
        }

        @Override
        public void onLoaderReset(Loader<UpdateState> loader) {

        }
    };

    private TextView mState;
    private Button mCheckUpdate;

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mState = (TextView) findViewById(R.id.tv_state);
        mCheckUpdate = (Button) findViewById(R.id.btn_check);
        mCheckUpdate.setOnClickListener(this);

        CheckService.setNextTimer(getApplicationContext());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_check:
                checkUpdate();
                break;
        }
    }

    private void checkUpdate() {
        getSupportLoaderManager().restartLoader(LOADER_ID, null, mLoaderCallback);
    }
}
