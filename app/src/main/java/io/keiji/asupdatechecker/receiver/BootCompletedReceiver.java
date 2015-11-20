package io.keiji.asupdatechecker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.keiji.asupdatechecker.service.CheckService;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        CheckService.setNextTimer(context);
    }
}
