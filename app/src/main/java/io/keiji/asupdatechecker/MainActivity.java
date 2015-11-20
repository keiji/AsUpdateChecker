package io.keiji.asupdatechecker;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;


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
            mState.setText(data.products.get(data.products.keySet().iterator().next())
                    .channels.get("release")
                    .builds.get(0)
                    .version);
        }

        @Override
        public void onLoaderReset(Loader<UpdateState> loader) {

        }
    };

    private TextView mState;
    private Button mCheckUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mState = (TextView) findViewById(R.id.tv_state);
        mCheckUpdate = (Button) findViewById(R.id.btn_check);
        mCheckUpdate.setOnClickListener(this);
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
