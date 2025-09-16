package com.project.gudasi; // ← 앱 패키지에 맞게 수정

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SubscriptionAdapter adapter;
    private List<Subscription> subscriptionList = new ArrayList<>();
    private FirebaseFirestore firedb;
    private TextView totalSubscriptionAmount;
    private TextView totalOverallAmount;
    private TextView nextPaymentDate;
    private TextView nextPaymentPrice;
    private TextView paymentComplete;
    private TextView mainTitle;

    public int totalCurrentMonth = 0; // 이번 달 결제액


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // 현재 액티비티 레이아웃

        mainTitle = findViewById(R.id.mainTitle);

        totalSubscriptionAmount = findViewById(R.id.totalSubscriptionAmount);
        totalOverallAmount = findViewById(R.id.totalOverallAmount);

        nextPaymentDate = findViewById(R.id.nextPaymentDate);
        nextPaymentPrice = findViewById(R.id.nextpaymentPrice);

        paymentComplete = findViewById(R.id.paymentComplete);

        FirebaseApp.initializeApp(this);
        firedb = FirebaseFirestore.getInstance();

        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM user", null);

        // --- 수정된 부분: 프로필 이미지 클릭 리스너 추가 ---
        ImageView profileImage = findViewById(R.id.profileImage);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ProfileActivity로 이동하는 Intent
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
        // --------------------------------------------------

        if (cursor != null) {
            Log.d("DB_DEBUG", "Cursor count: " + cursor.getCount());
            Log.d("DB_DEBUG", "Columns: " + Arrays.toString(cursor.getColumnNames()));
        }

        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex("name");
            int emailIndex = cursor.getColumnIndex("email");

            if (nameIndex != -1) {
                String name = cursor.getString(nameIndex); // DB에서 직접 가져오기
                TextView userName = findViewById(R.id.userName);
                userName.setText(name + "님");
            }

            if (emailIndex != -1) {
                String email = cursor.getString(emailIndex); // 이메일
                Log.d("DB_DEBUG", "로그인한 이메일: " + email);

                loadSubscriptions(email);
            } else {
                Log.e("DB_ERROR", "컬럼 인덱스를 찾을 수 없습니다.");
            }
        } else {
            Toast.makeText(this, "사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show();
        }


        if (cursor != null) cursor.close();
        db.close();

        recyclerView = findViewById(R.id.subscriptionRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubscriptionAdapter(subscriptionList);
        recyclerView.setAdapter(adapter);

        FirebaseApp.initializeApp(this);
        recyclerView = findViewById(R.id.subscriptionRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SubscriptionAdapter(subscriptionList);
        recyclerView.setAdapter(adapter);


        // 하단 메뉴 버튼 처리
        setupBottomNavigation();
    }

    private class NextPayment {
        String serviceName;
        int price;
        long daysLeft;
    }

    private NextPayment getNextPayment(List<Subscription> subscriptions) {
        NextPayment nextPayment = null;
        Calendar now = Calendar.getInstance();

        for (Subscription s : subscriptions) {
            try {
                Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(s.getDate());
                Calendar paymentDate = Calendar.getInstance();
                paymentDate.setTime(startDate);

                String renewalStr = s.getRenewalPrice();
                String[] parts = renewalStr.split("/");
                String unit = parts.length > 1 ? parts[1] : "1개월";

                int price = parsePriceString(s.getRenewalPrice());

                // 반복되는 결제일 계산
                while (paymentDate.before(now)) {
                    if (unit.contains("년")) {
                        paymentDate.add(Calendar.YEAR, 1); // 1년 단위
                    } else {
                        paymentDate.add(Calendar.MONTH, 1); // 1개월 단위
                    }
                }

                long diffMillis = paymentDate.getTimeInMillis() - now.getTimeInMillis();
                long diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis);

                // 가장 가까운 결제일 선택
                if (nextPayment == null || diffDays < nextPayment.daysLeft) {
                    nextPayment = new NextPayment();
                    nextPayment.serviceName = s.getServiceName();
                    nextPayment.price = price;
                    nextPayment.daysLeft = diffDays;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return nextPayment;
    }

    private void loadSubscriptions(String email) {
        firedb.collection("subscriptions")
                .document(email)
                .collection("items")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        subscriptionList.clear();


                        int totalOverall = 0;      // 총 지출총액

                        Calendar now = Calendar.getInstance();
                        int currentYear = now.get(Calendar.YEAR);
                        int currentMonth = now.get(Calendar.MONTH) + 1; // Calendar.MONTH는 0~11

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Subscription s = doc.toObject(Subscription.class);
                            subscriptionList.add(s);

                            try {
                                // 구독 시작일
                                Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(s.getDate());
                                Calendar subStart = Calendar.getInstance();
                                subStart.setTime(startDate);
                                int subYear = subStart.get(Calendar.YEAR);
                                int subMonth = subStart.get(Calendar.MONTH) + 1;

                                // 이번 달 결제액 계산
                                int monthlyPayment = getMonthlyPayment(s.getRenewalPrice(), subMonth, subYear, currentMonth, currentYear);
                                totalCurrentMonth += monthlyPayment;

                                // 총 지출총액 계산
                                int months = getMonthsBetween(s.getDate());
                                if (s.getRenewalPrice().contains("년")) {
                                    int yearsPassed = months / 12 + 1;
                                    totalOverall += yearsPassed * parsePriceString(s.getRenewalPrice());
                                } else {
                                    totalOverall += months * parsePriceString(s.getRenewalPrice());
                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        adapter.notifyDataSetChanged();

                        // 이번 달 결제액, 총 지출총액 업데이트
                        totalSubscriptionAmount.setText("₩" + String.format("%,d", totalCurrentMonth));
                        paymentComplete.setText(currentMonth + "월 결제 완료");
                        totalOverallAmount.setText("₩" + String.format("%,d", totalOverall));

                        // 다음 결제 예정 계산
                        NextPayment nextPay = getNextPayment(subscriptionList);
                        if (nextPay != null) {
                            nextPaymentDate.setText(nextPay.daysLeft + "일 뒤 : ");
                            nextPaymentPrice.setText(String.format("₩%,d", nextPay.price));
                            mainTitle.setText(nextPay.serviceName + ", " + nextPay.daysLeft + "일 뒤 결제돼요");
                        } else {
                            nextPaymentDate.setText("다음 결제 예정 없음");
                            nextPaymentPrice.setText("");
                        }

                    } else {
                        Log.e("FirestoreDebug", "Failed to fetch subscriptions", task.getException());
                    }
                });
    }


    // 개월 수를 계산하는 함수
    private int getMonthsBetween(String startDateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date startDate = sdf.parse(startDateStr);
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);

            Calendar now = Calendar.getInstance();

            int yearDiff = now.get(Calendar.YEAR) - startCal.get(Calendar.YEAR);
            int monthDiff = now.get(Calendar.MONTH) - startCal.get(Calendar.MONTH);

            return yearDiff * 12 + monthDiff + 1; // 구독 시작 월 포함
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // 가격 문자열 숫자만 추출
    private int parsePriceString(String priceStr) {
        try {
            String[] parts = priceStr.split("/");
            return Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    //
    private int getMonthlyPayment(String renewalStr, int startMonth, int startYear, int currentMonth, int currentYear) {
        // renewalStr 예: "₩14,000/1년", "₩12,000/1개월"
        try {
            String[] parts = renewalStr.split("/");
            int price = Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
            String unit = parts.length > 1 ? parts[1] : "1개월";

            if (unit.contains("년")) {
                // 연 단위는 구독 시작월과 현재 월이 같은 경우에만 이번 달 결제액에 포함
                if (startMonth == currentMonth && startYear <= currentYear) {
                    return price; // 1년에 한 번만 계산
                } else {
                    return 0; // 나머지 달에는 0
                }
            } else {
                // 월 단위는 그대로
                return price;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void setupBottomNavigation() {
        View bottomBar = findViewById(R.id.bottom_bar_include);

        // 하단 메뉴 버튼들 참조
        ImageView btnHome = bottomBar.findViewById(R.id.btnHome);
        ImageView btnRanking = bottomBar.findViewById(R.id.btnRanking);
        ImageView btnAppUsage = bottomBar.findViewById(R.id.btnAppUsage);

        // 이 밖의 다른 버튼이 있다면 여기에 포함

        int defaultColor = Color.parseColor("#666666");
        int selectedColor = Color.parseColor("#FFFFFF");

        // 기본 색상 설정 (현재 Home이 선택됨)
        btnHome.setColorFilter(selectedColor);
        btnRanking.setColorFilter(defaultColor);
        btnAppUsage.setColorFilter(defaultColor);

        // 버튼 클릭 리스너 설정
        btnHome.setOnClickListener(v -> {
            // 현재 페이지이므로 아무것도 하지 않음
        });

        btnRanking.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, RankingActivity.class));
        });

        // 여기서 btnAppUsage의 클릭 동작을 챗봇 화면으로 변경
        btnAppUsage.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ChatActivity.class));
        });
    }
}