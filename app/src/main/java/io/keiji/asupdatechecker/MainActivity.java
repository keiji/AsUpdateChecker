package io.keiji.asupdatechecker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

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

        try {
            String result = Endpoint.getUpdateState();
            mState.setText(result);
        } catch (IOException e) {
        }

    }
}
