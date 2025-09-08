package com.project.gudasi;

import android.graphics.Color;
import android.os.Bundle;
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
//        TextView tvYoutubeStatus = findViewById(R.id.tvYoutubeStatus);
       // TextView tvMemoStatus = findViewById(R.id.tvMemoStatus);

        // 시간 기준 사용량 계산 및 설정
     //   updateUsage(pbYoutube, tvYoutubeStatus, youtubeUsageHours);
   //     updateUsage(pbMemo, tvMemoStatus, memoWidgetUsageHours);

        // 하단 버튼 클릭 이벤트
        ImageView btnHome = findViewById(R.id.btnHome);
        ImageView btnRanking = findViewById(R.id.btnRanking);        ImageView btnAppUsage = findViewById(R.id.btnAppUsage);
        ImageView btnMyPage = findViewById(R.id.btnMyPage);

        btnHome.setOnClickListener(v -> showToast("홈 클릭!"));
        btnRanking.setOnClickListener(v -> showToast("랭킹 클릭!"));
        btnAppUsage.setOnClickListener(v -> showToast("앱 사용량 클릭!"));
        btnMyPage.setOnClickListener(v -> showToast("마이페이지 클릭!"));
    }

    // 시간 기준 ProgressBar 및 상태 텍스트 업데이트
    private void updateUsage(ProgressBar pb, TextView tv, int usageHours) {
        int low = 3;    // 사용량 적음 기준
        int normal = 5; // 사용량 적정 기준
        int high = 10;  // 사용량 많음 기준

        // ProgressBar 퍼센트 (0~100)
        int progress = Math.min((int)((float)usageHours / high * 100), 100);
        pb.setProgress(progress);

        // 상태 텍스트 및 ProgressBar 색상 설정
        if (usageHours <= low) {
            tv.setText("📌 사용량 적음");
            tv.setTextColor(Color.parseColor("#FF5252")); // 빨강
            pb.setProgressTintList(getResources().getColorStateList(android.R.color.holo_red_light));
        } else if (usageHours <= normal) {
            tv.setText("📌 사용량 적정");
            tv.setTextColor(Color.parseColor("#FFD700")); // 노랑
            pb.setProgressTintList(getResources().getColorStateList(android.R.color.holo_orange_light));
        } else {
            tv.setText("📌 사용량 많음");
            tv.setTextColor(Color.parseColor("#00FF00")); // 초록
            pb.setProgressTintList(getResources().getColorStateList(android.R.color.holo_green_light));
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
