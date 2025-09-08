package com.project.gudasi; // ← 앱 패키지에 맞게 수정

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class UsageStatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_stats); // 현재 액티비티 레이아웃

        View bottomBar = findViewById(R.id.bottom_bar_include);
        // 하단 메뉴 버튼들 참조
        ImageView btnHome = bottomBar.findViewById(R.id.btnHome);
        ImageView btnRanking = bottomBar.findViewById(R.id.btnRanking);  // 수정: 래퍼 LinearLayout에도 ID 추가
        ImageView btnAppUsage = bottomBar.findViewById(R.id.btnAppUsage);
        // ImageView btnMyPage = findViewById(R.id.btnMyPage);

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

    }
}
