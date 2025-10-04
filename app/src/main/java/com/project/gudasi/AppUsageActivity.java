package com.project.gudasi;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AppUsageActivity extends AppCompatActivity {

    // 샘플 앱별 사용 시간(시간 단위)
    private int youtubeUsageHours = 4; // 4시간
    private int memoWidgetUsageHours = 2; // 2시간

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_stats);

        // ProgressBar & 상태 TextView 연결
        ProgressBar pbYoutube = findViewById(R.id.pbYoutube);
        ProgressBar pbMemo = findViewById(R.id.pbMemo);
        TextView tvYoutubeNote = findViewById(R.id.youtube_note);
        TextView tvWidgetNote = findViewById(R.id.widget_note);

        // 시간 기준 사용량 계산 및 설정
        updateUsage(pbYoutube, tvYoutubeNote, youtubeUsageHours);
        updateUsage(pbMemo, tvWidgetNote, memoWidgetUsageHours);

        // --- 수정된 부분: 하단 네비게이션 바 버튼 처리 ---
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

        int defaultColor = Color.parseColor("#888888");
        int selectedColor = Color.parseColor("#007BFF");

        // 현재 '사용시간' 탭이 선택된 상태로 설정
        homeIcon.setColorFilter(defaultColor);
        homeText.setTextColor(defaultColor);
        chatIcon.setColorFilter(defaultColor);
        chatText.setTextColor(defaultColor);
        usageIcon.setColorFilter(selectedColor);
        usageText.setTextColor(selectedColor);
        usageText.setTypeface(null, android.graphics.Typeface.BOLD);
        homeText.setTypeface(null, android.graphics.Typeface.NORMAL);


        btnHome.setOnClickListener(v -> {
            startActivity(new Intent(AppUsageActivity.this, HomeActivity.class));
            finish();
        });

        btnChat.setOnClickListener(v -> {
            startActivity(new Intent(AppUsageActivity.this, ChatActivity.class));
            finish();
        });

        btnAppUsage.setOnClickListener(v -> {
            // 현재 페이지이므로 아무 동작 없음
            showToast("현재 사용시간 화면입니다.");
        });
    }

    /**
     * 사용 시간에 따라 ProgressBar 및 상태 텍스트를 업데이트하는 메서드
     * @param pb 업데이트할 ProgressBar
     * @param tv 업데이트할 TextView
     * @param usageHours 사용 시간 (시간 단위)
     */
    private void updateUsage(ProgressBar pb, TextView tv, int usageHours) {
        int low = 3;    // 사용량 적음 기준
        int normal = 5; // 사용량 적정 기준
        int high = 10;  // 사용량 많음 기준

        // ProgressBar 퍼센트 (0~100)
        int progress = Math.min((int)((float)usageHours / high * 100), 100);
        pb.setProgress(progress);

        // 상태 텍스트 및 ProgressBar 색상 설정
        if (usageHours <= low) {
            tv.setText("사용량 적음");
            tv.setTextColor(Color.parseColor("#F44336")); // 빨강
            pb.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bar_gradient_red, getTheme()));
        } else if (usageHours <= normal) {
            tv.setText("사용량 적정");
            tv.setTextColor(Color.parseColor("#FFC107")); // 노랑
            pb.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bar_gradient_yellow, getTheme()));
        } else {
            tv.setText("사용량 많음");
            tv.setTextColor(Color.parseColor("#4CAF50")); // 초록
            // '많음' 상태에 대한 progress bar drawable이 없으므로 임의로 노란색을 사용합니다.
            // R.drawable.progress_bar_gradient_green 와 같은 파일을 추가해야 합니다.
            pb.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bar_gradient_yellow, getTheme()));
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}