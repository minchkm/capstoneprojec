package com.project.gudasi;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private TextView tvMonthlyTotal;
    private RecyclerView rvPayments;
    private PaymentAdapter paymentAdapter;

    private List<Subscription> subscriptionList;
    private Map<CalendarDay, List<Subscription>> paymentMap = new HashMap<>();

    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);
        tvMonthlyTotal = findViewById(R.id.tvMonthlyTotal);
        rvPayments = findViewById(R.id.rvPayments);

        rvPayments.setLayoutManager(new LinearLayoutManager(this));
        paymentAdapter = new PaymentAdapter(new ArrayList<>());
        rvPayments.setAdapter(paymentAdapter);

        backButton = findViewById(R.id.backButton);

        // --- 뒤로가기 버튼 클릭 리스너 추가 ---
        ImageButton backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // HomeActivity에서 받은 데이터
        subscriptionList = (List<Subscription>) getIntent().getSerializableExtra("subscriptionList");
        int totalCurrentMonth = getIntent().getIntExtra("totalCurrentMonth", 0);

        tvMonthlyTotal.setText("이번 달 결제액: ₩" + String.format("%,d", totalCurrentMonth));

        List<CalendarDay> allPaymentDates = new ArrayList<>();

        if (subscriptionList != null) {
            for (Subscription s : subscriptionList) {
                try {
                    Date startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(s.getDate());
                    Calendar paymentCal = Calendar.getInstance();
                    paymentCal.setTime(startDate);

                    String renewalStr = s.getRenewalPrice();
                    String[] parts = renewalStr.split("/");
                    String unit = parts.length > 1 ? parts[1] : "1개월";

                    Calendar now = Calendar.getInstance();

                    // 반복 결제일 계산
                    while (paymentCal.before(now)) {
                        if (unit.contains("년")) paymentCal.add(Calendar.YEAR, 1);
                        else paymentCal.add(Calendar.MONTH, 1);
                    }

                    // 앞으로 12개월 또는 1년 단위 반복
                    Calendar tempCal = (Calendar) paymentCal.clone();
                    for (int i = 0; i < 12; i++) {
                        CalendarDay day = CalendarDay.from(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH) + 1, tempCal.get(Calendar.DAY_OF_MONTH));
                        allPaymentDates.add(day);

                        if (!paymentMap.containsKey(day)) {
                            paymentMap.put(day, new ArrayList<>());
                        }
                        paymentMap.get(day).add(s);

                        if (unit.contains("년")) tempCal.add(Calendar.YEAR, 1);
                        else tempCal.add(Calendar.MONTH, 1);
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        // 캘린더 하이라이트
        calendarView.addDecorator(new EventDecorator(Color.WHITE, allPaymentDates));

        // 날짜 클릭 이벤트
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            List<Subscription> services = paymentMap.get(date);
            if (services != null && !services.isEmpty()) {
                paymentAdapter.updateData(services);
            } else {
                paymentAdapter.updateData(new ArrayList<>());
                Toast.makeText(this, "이 날은 결제 예정이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
