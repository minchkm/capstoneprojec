package com.project.gudasi; // ← 앱 패키지에 맞게 수정

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class UsageStatsActivity extends AppCompatActivity {

    private TextView youtubeTimeText;
    private TextView youtubeNoteText;
    private TextView widgetTimeText;
    private TextView widgetNoteText;
    private Button btnCancelYoutube;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_stats); // 현재 액티비티 레이아웃

        int totalCurrentMonth = getIntent().getIntExtra("totalCurrentMonth", 0);

        TextView monthlyUsage = findViewById(R.id.monthlyUsage);

        monthlyUsage.setText(String.format("%,d", totalCurrentMonth));

        youtubeTimeText = findViewById(R.id.youtube_time);
        youtubeNoteText = findViewById(R.id.youtube_note);
        widgetTimeText = findViewById(R.id.widget_time);
        widgetNoteText = findViewById(R.id.widget_note);

        btnCancelYoutube = findViewById(R.id.btnCancelYoutube);

        // 권한 확인 후 앱 사용 시간 업데이트
        if (!hasUsageStatsPermission()) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        } else {
            updateAppUsage();
        }

        View bottomBar = findViewById(R.id.bottom_bar_include);
        // 하단 메뉴 버튼들 참조
        ImageView btnHome = bottomBar.findViewById(R.id.btnHome);
        ImageView btnRanking = bottomBar.findViewById(R.id.btnRanking);  // 수정: 래퍼 LinearLayout에도 ID 추가
        ImageView btnAppUsage = bottomBar.findViewById(R.id.btnAppUsage);
        // ImageView btnMyPage = findViewById(R.id.btnMyPage);

        int defaultColor = Color.parseColor("#666666");
        int selectedColor = Color.parseColor("#FFFFFF");

        btnHome.setColorFilter(defaultColor);
        btnRanking.setColorFilter(defaultColor);
        btnAppUsage.setColorFilter(selectedColor);

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UsageStatsActivity.this, HomeActivity.class);
                startActivity(intent);
                finish(); // 현재 액티비티 종료 (선택)
            }
        });

        btnRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UsageStatsActivity.this, RankingActivity.class);
                startActivity(intent);
                finish(); // 현재 액티비티 종료 (선택)
            }
        });

        btnAppUsage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UsageStatsActivity.this, UsageStatsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnCancelYoutube.setOnClickListener(v -> {
            // 커스텀 다이얼로그 생성
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_low_usage); // xml 파일 지정
            dialog.setCancelable(true);

            // 가로 크기 조절
            Window window = dialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(window.getAttributes());
                layoutParams.width = (int)(getResources().getDisplayMetrics().widthPixels * 0.85); // 화면 가로의 85%
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                window.setAttributes(layoutParams);
            }

            // 다이얼로그 안의 버튼
            Button btnCancel = dialog.findViewById(R.id.btn_cancel);

            btnCancel.setOnClickListener(view -> {
                // YouTube 구독 해지 페이지로 이동
                String youtubeCancelUrl = "https://www.youtube.com/paid_memberships";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeCancelUrl));
                startActivity(browserIntent);
                dialog.dismiss(); // 다이얼로그 닫기
            });

            // "무시" 버튼
            Button btnIgnore = dialog.findViewById(R.id.btn_ignore);
            btnIgnore.setOnClickListener(view -> {
                dialog.dismiss(); // 다이얼로그 닫기
            });

            dialog.show();
        });

    }

    // 사용 기록 접근 권한 확인
    private boolean hasUsageStatsPermission() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long now = System.currentTimeMillis();
        List<UsageStats> stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, now - 1000 * 60, now);
        return stats != null && !stats.isEmpty();
    }

    // 앱 사용 시간 가져오기
    private void updateAppUsage() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long now = System.currentTimeMillis();
        long oneDayAgo = now - 1000 * 60 * 60 * 24; // 지난 24시간

        List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, oneDayAgo, now);

        long youtubeTime = getAppUsage(stats, "com.google.android.youtube");
        long widgetTime = getAppUsage(stats, "com.example.memo_widget"); // 패키지 이름 수정

        // 밀리초 -> 분 단위
        youtubeTimeText.setText("사용시간: " + TimeUnit.MILLISECONDS.toMinutes(youtubeTime) + "분");
        widgetTimeText.setText("사용시간: " + TimeUnit.MILLISECONDS.toMinutes(widgetTime) + "분");

        // 안내 문구 조건
        youtubeNoteText.setText(youtubeTime < 10 * 60 * 1000
                ? "📌 사용량이 적어요. 구독을 다시 고려해보세요"
                : "📌 사용량이 충분해요!");

        widgetNoteText.setText(widgetTime < 10 * 60 * 1000
                ? "📌 사용량이 적어요. 구독을 다시 고려해보세요"
                : "📌 사용량이 충분해요!");
    }

    private long getAppUsage(List<UsageStats> stats, String packageName) {
        for (UsageStats usage : stats) {
            if (usage.getPackageName().equals(packageName)) {
                return usage.getTotalTimeInForeground();
            }
        }
        return 0;
    }


}
