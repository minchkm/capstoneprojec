package com.project.gudasi;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "subscription_reminder_channel";
    private static final String CHANNEL_NAME = "구독 결제 알림";

    // 알림을 생성하고 표시하는 메서드
    public static void showNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android 8.0 (Oreo) 이상에서는 Notification Channel을 생성해야 함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        // 알림 UI 구성
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm) // 알림 아이콘
                .setContentTitle(title) // 알림 제목 (예: "Netflix 결제 3일 전")
                .setContentText(message) // 알림 내용 (예: "곧 13,500원이 결제될 예정이에요.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true); // 사용자가 탭하면 알림이 사라짐

        // 알림 표시 (각 알림이 고유 ID를 갖도록 현재 시간을 ID로 사용)
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}