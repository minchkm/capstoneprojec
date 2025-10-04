package com.project.gudasi;

import android.app.Dialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Locale;

public class UsageStatsActivity extends AppCompatActivity {

    private TextView youtubeTimeText;
    private TextView youtubeNoteText;
    private TextView widgetTimeText;
    private TextView widgetNoteText;
    private MaterialButton btnCancelYoutube;
    private MaterialButton btnCancelMemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_stats);

        int totalCurrentMonth = getIntent().getIntExtra("totalCurrentMonth", 0);

        TextView monthlyUsage = findViewById(R.id.monthlyUsage);
        monthlyUsage.setText(String.format(Locale.getDefault(), "₩ %,d", totalCurrentMonth));

        youtubeTimeText = findViewById(R.id.youtube_time);
        youtubeNoteText = findViewById(R.id.youtube_note);
        widgetTimeText = findViewById(R.id.widget_time);
        widgetNoteText = findViewById(R.id.widget_note);

        btnCancelYoutube = findViewById(R.id.btnCancelYoutube);
        btnCancelMemo = findViewById(R.id.btnCancelMemo);


        if (!hasUsageStatsPermission()) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        } else {
            updateAppUsage();
        }

        setupBottomNavigation();

        btnCancelYoutube.setOnClickListener(v -> showCancelDialog("Youtube", "https://www.youtube.com/paid_memberships"));
        // "메모위젯"의 해지 URL이 없으므로 null을 전달합니다.
        btnCancelMemo.setOnClickListener(v -> showCancelDialog("메모위젯", null));
    }

    private void showCancelDialog(String appName, String cancelUrl) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_low_usage);
        dialog.setCancelable(true);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
        }

        // ▼▼▼▼▼▼▼▼▼▼ 수정된 부분 ▼▼▼▼▼▼▼▼▼▼
        // XML에 추가한 ID를 사용하여 TextView를 찾습니다.
        TextView appNameTextView = dialog.findViewById(R.id.dialog_app_name);
        if (appNameTextView != null) {
            appNameTextView.setText(appName);
        }
        // ▲▲▲▲▲▲▲▲▲▲ 수정된 부분 ▲▲▲▲▲▲▲▲▲▲


        MaterialButton btnCancel = dialog.findViewById(R.id.btn_cancel);
        MaterialButton btnIgnore = dialog.findViewById(R.id.btn_ignore);

        btnCancel.setOnClickListener(view -> {
            if (cancelUrl != null && !cancelUrl.isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(cancelUrl));
                startActivity(browserIntent);
            }
            dialog.dismiss();
        });

        btnIgnore.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }


    private boolean hasUsageStatsPermission() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long now = System.currentTimeMillis();
        List<UsageStats> stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, now - (1000 * 60), now);
        return stats != null && !stats.isEmpty();
    }

    private void updateAppUsage() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long now = System.currentTimeMillis();
        // 지난 한 달간의 사용 기록을 가져옵니다.
        long oneMonthAgo = now - TimeUnit.DAYS.toMillis(30);

        List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_MONTHLY, oneMonthAgo, now);

        long youtubeTime = getAppUsage(stats, "com.google.android.youtube");
        // '메모위젯'의 실제 패키지 이름으로 변경해야 정확한 데이터가 나옵니다.
        long widgetTime = getAppUsage(stats, "com.project.gudasi");

        youtubeTimeText.setText("사용시간: " + formatMillis(youtubeTime));
        widgetTimeText.setText("사용시간: " + formatMillis(widgetTime));

        // 사용 시간에 따라 안내 문구와 색상 변경
        if (youtubeTime < TimeUnit.HOURS.toMillis(2)) { // 2시간 미만 사용 시
            youtubeNoteText.setText("사용량 적음");
            youtubeNoteText.setTextColor(Color.parseColor("#F44336"));
        } else {
            youtubeNoteText.setText("사용량 적정");
            youtubeNoteText.setTextColor(Color.parseColor("#FFC107"));
        }

        if (widgetTime < TimeUnit.HOURS.toMillis(1)) { // 1시간 미만 사용 시
            widgetNoteText.setText("사용량 적음");
            widgetNoteText.setTextColor(Color.parseColor("#F44336"));
        } else {
            widgetNoteText.setText("사용량 충분");
            widgetNoteText.setTextColor(Color.parseColor("#4CAF50"));
        }
    }
    private String formatMillis(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d시간 %d분", hours, minutes);
        } else {
            return String.format(Locale.getDefault(), "%d분", minutes);
        }
    }


    private long getAppUsage(List<UsageStats> stats, String packageName) {
        for (UsageStats usage : stats) {
            if (usage.getPackageName().equals(packageName)) {
                return usage.getTotalTimeInForeground();
            }
        }
        return 0;
    }

    private void setupBottomNavigation() {
        View bottomBar = findViewById(R.id.bottom_bar_include);

        View btnHome = bottomBar.findViewById(R.id.homeButton);
        View btnChat = bottomBar.findViewById(R.id.chatButton);
        View btnAppUsage = bottomBar.findViewById(R.id.usageTimeButton);

        ImageView homeIcon = bottomBar.findViewById(R.id.homeIcon);
        TextView homeText = bottomBar.findViewById(R.id.homeText);
        ImageView chatIcon = bottomBar.findViewById(R.id.chatIcon);
        TextView chatText = bottomBar.findViewById(R.id.chatText);
        ImageView usageIcon = bottomBar.findViewById(R.id.usageTimeIcon);
        TextView usageText = bottomBar.findViewById(R.id.usageTimeText);

        int defaultColor = Color.parseColor("#8A94A4");
        int selectedColor = Color.parseColor("#007BFF");

        homeIcon.setColorFilter(defaultColor);
        homeText.setTextColor(defaultColor);
        homeText.setTypeface(null, android.graphics.Typeface.NORMAL);

        chatIcon.setColorFilter(defaultColor);
        chatText.setTextColor(defaultColor);
        chatText.setTypeface(null, android.graphics.Typeface.NORMAL);

        usageIcon.setColorFilter(selectedColor);
        usageText.setTextColor(selectedColor);
        usageText.setTypeface(null, android.graphics.Typeface.BOLD);


        btnHome.setOnClickListener(v -> {
            startActivity(new Intent(UsageStatsActivity.this, HomeActivity.class));
            finish();
        });

        btnChat.setOnClickListener(v -> {
            startActivity(new Intent(UsageStatsActivity.this, ChatActivity.class));
            finish();
        });

        btnAppUsage.setOnClickListener(v -> {
            // 현재 페이지이므로 아무 동작 없음
        });
    }
}