package com.project.gudasi;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private RecyclerView rvPayments;
    private PaymentAdapter paymentAdapter;
    private TextView tvMonthlyTotal;

    private List<Subscription> subscriptionList;
    private HashMap<CalendarDay, List<Subscription>> paymentMap = new HashMap<>();

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

        // HomeActivity에서 데이터 가져오기
        Intent intent = getIntent();
        subscriptionList = (List<Subscription>) intent.getSerializableExtra("subscriptionList");
        int totalCurrentMonth = intent.getIntExtra("totalCurrentMonth", 0);

        tvMonthlyTotal.setText("이번 달 결제액: ₩" + String.format("%,d", totalCurrentMonth));

        // 날짜별 결제일 계산
        List<CalendarDay> allPaymentDates = new ArrayList<>();
        if (subscriptionList != null) {
            for (Subscription s : subscriptionList) {
                List<CalendarDay> dates = getPaymentDates(s);
                allPaymentDates.addAll(dates);

                for (CalendarDay d : dates) {
                    if (!paymentMap.containsKey(d)) {
                        paymentMap.put(d, new ArrayList<>());
                    }
                    paymentMap.get(d).add(s);
                }
            }
        }

        calendarView.setWeekDayTextAppearance(R.style.CalendarWhiteText);
        calendarView.setDateTextAppearance(R.style.CalendarWhiteText);


        // 캘린더 데코레이터 적용
        calendarView.addDecorators(
                new OutOfMonthDecorator(),
                new EventDecorator(allPaymentDates),
                new SaturdayDecorator(),
                new SundayDecorator(),
                new WeekdayDecorator()
        );

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

        backButton = findViewById(R.id.backButton);
        // --- 뒤로가기 버튼 클릭 리스너 추가 ---
        ImageButton backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    // 구독 결제일 계산 (연/월 단위 반영)
    private List<CalendarDay> getPaymentDates(Subscription s) {
        List<CalendarDay> result = new ArrayList<>();
        String dateStr = s.getDate();
        if (dateStr == null || dateStr.isEmpty()) return result;

        String renewal = s.getRenewalPrice();
        String[] parts = renewal.split("/");
        String unit = parts.length > 1 ? parts[1] : "1개월";

        Calendar now = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 1); // 1년 범위 내 표시, 필요 시 더 늘릴 수 있음

        Calendar payment = Calendar.getInstance();
        try {
            String[] ymd = dateStr.split("-");
            payment.set(Integer.parseInt(ymd[0]), Integer.parseInt(ymd[1]) - 1, Integer.parseInt(ymd[2]));

            while (!payment.after(end)) {
                result.add(CalendarDay.from(payment));

                if (unit.contains("년")) {
                    payment.add(Calendar.YEAR, 1);
                } else {
                    payment.add(Calendar.MONTH, 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    // 이전/다음 달 날짜 회색
    public class OutOfMonthDecorator implements DayViewDecorator {
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            Calendar today = Calendar.getInstance();
            return day.getMonth() != today.get(Calendar.MONTH);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new android.text.style.ForegroundColorSpan(Color.GRAY));
        }
    }

    // 토요일 파랑
    public class SaturdayDecorator implements DayViewDecorator {
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            Calendar c = Calendar.getInstance();
            c.set(day.getYear(), day.getMonth(), day.getDay());
            return c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new android.text.style.ForegroundColorSpan(Color.BLUE));
        }
    }

    // 일요일 빨강
    public class SundayDecorator implements DayViewDecorator {
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            Calendar c = Calendar.getInstance();
            c.set(day.getYear(), day.getMonth(), day.getDay());
            return c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new android.text.style.ForegroundColorSpan(Color.RED));
        }
    }

    // 이벤트 날짜 (하이라이트)
    public class EventDecorator implements DayViewDecorator {
        private final HashSet<CalendarDay> dates;

        public EventDecorator(Collection<CalendarDay> dates) {
            this.dates = new HashSet<>(dates);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            // 등록된 결제 날짜만 하이라이트
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            // 동그라미 drawable
            view.setSelectionDrawable(ContextCompat.getDrawable(CalendarActivity.this, R.drawable.circle_background_black));
            // 글자 색 검은색
            view.addSpan(new ForegroundColorSpan(Color.BLACK));
        }
    }



    // 평일 흰색
    public class WeekdayDecorator implements DayViewDecorator {
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            Calendar c = Calendar.getInstance();
            c.set(day.getYear(), day.getMonth(), day.getDay());
            int dow = c.get(Calendar.DAY_OF_WEEK);
            return dow != Calendar.SATURDAY && dow != Calendar.SUNDAY;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new android.text.style.ForegroundColorSpan(Color.WHITE));
        }
    }
}
