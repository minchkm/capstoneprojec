package com.project.gudasi;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private RecyclerView rvPayments;
    private PaymentAdapter paymentAdapter;

    // XML 레이아웃과 일치하도록 TextView ID 수정
    private TextView tvMonthTitle, tvMonthlyTotal, tvMonthlyCount, tvSelectedDate, tvSelectedDateTotal;

    private List<Subscription> subscriptionList;
    private HashMap<CalendarDay, List<Subscription>> paymentMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // XML의 ID에 맞게 뷰 초기화
        calendarView = findViewById(R.id.calendarView);
        tvMonthTitle = findViewById(R.id.tvMonthTitle);
        tvMonthlyTotal = findViewById(R.id.tvMonthlyTotal);
        tvMonthlyCount = findViewById(R.id.tvMonthlyCount);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSelectedDateTotal = findViewById(R.id.tvSelectedDateTotal);
        rvPayments = findViewById(R.id.rvPayments);

        rvPayments.setLayoutManager(new LinearLayoutManager(this));
        paymentAdapter = new PaymentAdapter(new ArrayList<>());
        rvPayments.setAdapter(paymentAdapter);

        // HomeActivity에서 데이터 가져오기
        Intent intent = getIntent();
        subscriptionList = (List<Subscription>) intent.getSerializableExtra("subscriptionList");
        int totalCurrentMonth = intent.getIntExtra("totalCurrentMonth", 0);

        // --- 상단 헤더 정보 업데이트 로직 수정 ---
        SimpleDateFormat monthFormat = new SimpleDateFormat("M월", Locale.getDefault());
        String currentMonthStr = monthFormat.format(new Date());
        tvMonthTitle.setText(currentMonthStr + " 지출 총액");
        tvMonthlyTotal.setText(String.format("%,d원", totalCurrentMonth));

        // 날짜별 결제일 계산 및 월별 건수 계산
        List<CalendarDay> allPaymentDates = new ArrayList<>();
        int currentMonthCount = 0;
        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH);

        if (subscriptionList != null) {
            for (Subscription s : subscriptionList) {
                List<CalendarDay> dates = getPaymentDates(s);
                allPaymentDates.addAll(dates);

                for (CalendarDay d : dates) {
                    if (!paymentMap.containsKey(d)) {
                        paymentMap.put(d, new ArrayList<>());
                    }
                    paymentMap.get(d).add(s);
                    // 이번 달 결제 건수만 카운트
                    if (d.getMonth() - 1 == currentMonth) {
                        currentMonthCount++;
                    }
                }
            }
        }
        tvMonthlyCount.setText("· " + currentMonthCount + "건");


        // --- 불필요한 코드 제거 ---
        // XML에서 스타일을 적용하므로 자바 코드는 삭제합니다.
        // calendarView.setWeekDayTextAppearance(R.style.CalendarWhiteText);
        // calendarView.setDateTextAppearance(R.style.CalendarWhiteText);


        // --- 캘린더 데코레이터 적용 (수정) ---
        calendarView.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator(),
                new EventDecorator(this, allPaymentDates) // Context 전달
        );

        // --- 날짜 클릭 이벤트 수정 ---
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            // 날짜 선택 시 하단 정보 업데이트
            String selectedDateStr = new SimpleDateFormat("d일", Locale.getDefault()).format(date.getDate());
            tvSelectedDate.setText(selectedDateStr);

            List<Subscription> services = paymentMap.get(date);
            if (services != null && !services.isEmpty()) {
                paymentAdapter.updateData(services);
                long dailyTotal = 0;
                for(Subscription s : services) {
                    try {
                        // "원"과 ","를 제거하고 숫자로 변환
                        dailyTotal += Long.parseLong(s.getRenewalPrice().replaceAll("[^0-9]", ""));
                    } catch (NumberFormatException e) {
                        // 가격 형식이 잘못된 경우 무시
                    }
                }
                tvSelectedDateTotal.setText(String.format("%,d원 · %d건", dailyTotal, services.size()));

            } else {
                paymentAdapter.updateData(new ArrayList<>());
                tvSelectedDateTotal.setText("0원 · 0건");
                Toast.makeText(this, "이 날은 결제 예정이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // --- 뒤로가기 버튼 클릭 리스너 ---
        ImageButton backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    // 구독 결제일 계산 (기존 코드 유지)
    private List<CalendarDay> getPaymentDates(Subscription s) {
        List<CalendarDay> result = new ArrayList<>();
        String dateStr = s.getDate();
        if (dateStr == null || dateStr.isEmpty()) return result;

        String renewal = s.getRenewalPrice();
        String[] parts = renewal.split("/");
        String unit = parts.length > 1 ? parts[1] : "1개월";

        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 1);

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

    // --- Decorator 클래스 수정 ---

    // 일요일 (빨간색)
    public class SundayDecorator implements DayViewDecorator {
        private final Calendar calendar = Calendar.getInstance();
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            day.copyTo(calendar);
            int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
            return weekDay == Calendar.SUNDAY;
        }
        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(Color.parseColor("#EF4444")));
        }
    }

    // 토요일 (파란색)
    public class SaturdayDecorator implements DayViewDecorator {
        private final Calendar calendar = Calendar.getInstance();
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            day.copyTo(calendar);
            int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
            return weekDay == Calendar.SATURDAY;
        }
        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(Color.parseColor("#3B82F6")));
        }
    }

    // 이벤트 날짜 (주황색 별)
    public class EventDecorator implements DayViewDecorator {
        private final HashSet<CalendarDay> dates;
        private final Drawable starDrawable;

        public EventDecorator(Context context, Collection<CalendarDay> dates) {
            this.dates = new HashSet<>(dates);
            this.starDrawable = ContextCompat.getDrawable(context, R.drawable.ic_star_decorator);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            if (starDrawable != null) {
                view.addSpan(new ImageSpan(starDrawable, ImageSpan.ALIGN_CENTER));
            }
        }
    }
}