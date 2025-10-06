package com.project.gudasi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Intent로부터 알림 제목과 메시지를 가져옴
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        // NotificationHelper를 사용하여 알림을 표시
        if (title != null && message != null) {
            NotificationHelper.showNotification(context, title, message);
        }
    }
}