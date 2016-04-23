package io.keiji.asupdatechecker.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import io.keiji.asupdatechecker.R;

public class LicenseDialogFragment extends DialogFragment {

    private enum OssLibrary {
        MaterialIcons("Material icons", "licenses/creative_commons-4.0.txt"),
        AOSP("Android Open Source Project", "licenses/aosp.txt"),
        V7Support("Android v7 Support Libraries", "licenses/aosp.txt"),
        DesignSupport("Android Design Support Library", "licenses/aosp.txt"),
        StreamApi("Lightweight-Stream-API", "licenses/stream_api.txt"),
        Retrolambda("Gradle Retrolambda Plugin", "licenses/retrolambda.txt"),
        Realm("Realm Java", "licenses/realm.txt");

        public final String name;
        public final String fileName;

        OssLibrary(String name, String file) {
            this.name = name;
            fileName = file;
        }
    }

    private static final Map<String, String> sOssLicenseCache = new HashMap<>(OssLibrary.values().length);

    private final LoaderManager.LoaderCallbacks<Void> mLoaderCallback = new LoaderManager.LoaderCallbacks<Void>() {
        @Override
        public Loader<Void> onCreateLoader(int id, Bundle args) {
            LicenseLoader loader = new LicenseLoader(getActivity());
            loader.forceLoad();
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Void> loader, Void data) {
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onLoaderReset(Loader<Void> loader) {
        }
    };

    public static LicenseDialogFragment newInstance() {
        return new LicenseDialogFragment();
    }

    RecyclerView mRecyclerView;

    private OpenSourceRecyclerViewAdapter mAdapter;

    public LicenseDialogFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.dialog_open_source, null);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);

        mAdapter = new OpenSourceRecyclerViewAdapter(getActivity());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        Dialog dialog = new Dialog(getActivity());
        dialog.setTitle(R.string.license);
        dialog.setContentView(view);

        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        getLoaderManager().initLoader(0, new Bundle(), mLoaderCallback);
    }

    public static class LicenseLoader extends AsyncTaskLoader<Void> {
        private static final String TAG = LicenseLoader.class.getSimpleName();

        public LicenseLoader(Context context) {
            super(context);
        }

        @Override
        public Void loadInBackground() {
            for (OssLibrary ossLibrary : OssLibrary.values()) {
                load(ossLibrary);
            }
            return null;
        }

        private void load(OssLibrary ossLibrary) {
            InputStream is = null;
            try {
                is = getContext().getAssets().open(ossLibrary.fileName);
                sOssLicenseCache.put(ossLibrary.fileName, toString(is));
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        private static String toString(InputStream is) throws IOException {

            StringBuffer sb = new StringBuffer();

            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }

            br.close();

            return sb.toString();
        }
    }

    public static class OpenSourceRecyclerViewAdapter
            extends RecyclerView.Adapter<OpenSourceRecyclerViewAdapter.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;

        public OpenSourceRecyclerViewAdapter(Context context) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_license, parent, false);
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final OpenSourceRecyclerViewAdapter.ViewHolder holder, int position) {
            OssLibrary value = OssLibrary.values()[position];
            holder.mName.setText(value.name);
            holder.mLicense.setText(sOssLicenseCache.get(value.fileName));
        }

        @Override
        public int getItemCount() {
            return OssLibrary.values().length;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mName;
            public final TextView mLicense;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mName = (TextView) view.findViewById(R.id.tv_name);
                mLicense = (TextView) view.findViewById(R.id.tv_license);
            }
        }

    }
}
