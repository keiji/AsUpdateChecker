package io.keiji.asupdatechecker;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 0x1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setIcon(R.mipmap.ic_launcher);

        getFragmentManager()
                .beginTransaction()
                .add(R.id.content, SettingFragment.newInstance(), SettingFragment.class.getSimpleName())
                .commit();
    }
}
