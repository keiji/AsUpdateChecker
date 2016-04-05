package io.keiji.asupdatechecker.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import io.keiji.asupdatechecker.R;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 0x1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.content, SettingFragment.newInstance(), SettingFragment.class.getSimpleName())
                    .commit();
        }
    }
}
