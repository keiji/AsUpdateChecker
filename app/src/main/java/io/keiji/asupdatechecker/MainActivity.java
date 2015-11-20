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

    public static class CHeckUpdateLoader extends AsyncTaskLoader<String> {

        public CHeckUpdateLoader(Context context) {
            super(context);
        }

        @Override
        public String loadInBackground() {
            String result = null;
            try {
                result = Endpoint.getUpdateState();
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
            }

            return result;
        }
    }

    private final LoaderManager.LoaderCallbacks<String> mLoaderCallback = new LoaderManager.LoaderCallbacks<String>() {
        @Override
        public Loader<String> onCreateLoader(int id, Bundle args) {
            CHeckUpdateLoader loader = new CHeckUpdateLoader(MainActivity.this);
            loader.forceLoad();
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<String> loader, String data) {
            mState.setText(data);

        }

        @Override
        public void onLoaderReset(Loader<String> loader) {

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
