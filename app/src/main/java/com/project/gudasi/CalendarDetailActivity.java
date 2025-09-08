package com.project.gudasi;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CalendarDetailActivity extends AppCompatActivity {

    private TextView totalAmount;
    private CalendarView calendarView;
    private Map<String, Double> subscriptionMap; // 날짜별 구독 금액

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_detail);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // 뒤로가기

        totalAmount = findViewById(R.id.totalAmount);
        calendarView = findViewById(R.id.calendarView);

        initSubscriptions();
        updateTotalAmount();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String key = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            Double amount = subscriptionMap.getOrDefault(key, 0.0);
            showSubscriptionDialog(key, amount);
        });
    }

    // 예시 구독 데이터 초기화
    private void initSubscriptions() {
        subscriptionMap = new HashMap<>();
        // 예: 특정 날짜에 구독 금액 입력
        subscriptionMap.put("2025-08-05", 9.99);
        subscriptionMap.put("2025-08-10", 14.99);
        subscriptionMap.put("2025-08-20", 7.49);
    }

    // 총 금액 계산
    private void updateTotalAmount() {
        double total = 0.0;
        for (Double value : subscriptionMap.values()) {
            total += value;
        }
        totalAmount.setText(String.format("총 구독 금액: $%.2f", total));
    }

    // 날짜 클릭 시 구독 내역 팝업
    private void showSubscriptionDialog(String date, double amount) {
        String message = amount > 0 ? "구독 금액: $" + amount : "해당 날짜 구독 없음";
        new AlertDialog.Builder(this)
                .setTitle(date)
                .setMessage(message)
                .setPositiveButton("확인", null)
                .show();
    }
}
