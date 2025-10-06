package com.project.gudasi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.Manifest;
import android.content.pm.PackageManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity {

    // 알람관련
    private static final int NOTIFICATION_PERMISSION_CODE = 100;

    private RecyclerView recyclerView;
    private SubscriptionAdapter adapter;
    private List<Subscription> subscriptionList = new ArrayList<>();
    private ArrayList<String> paymentDates = new ArrayList<>();
    private TextView totalSubscriptionAmount;
    private TextView totalOverallAmount;
    private TextView nextPaymentDate;
    private TextView nextPaymentPrice;
    private TextView paymentComplete;
    private TextView mainTitle;
    private ImageView calendarButton;

    public int totalCurrentMonth = 0;
    int totalOverall = 0;

    private FirebaseFirestore firedb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // XML ID와 일치하도록 뷰 초기화
        mainTitle = findViewById(R.id.mainTitle);
        totalSubscriptionAmount = findViewById(R.id.totalSubscriptionAmount);
        totalOverallAmount = findViewById(R.id.totalOverallAmount);
        nextPaymentDate = findViewById(R.id.nextPaymentDate);
        nextPaymentPrice = findViewById(R.id.nextpaymentPrice);
        paymentComplete = findViewById(R.id.paymentComplete);
        calendarButton = findViewById(R.id.calendarButton);
        TextView userName = findViewById(R.id.userName); // userName 뷰를 여기서 초기화

        // ▼▼▼▼▼ 수정된 부분 시작 ▼▼▼▼▼

        firedb = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            // onCreate에서 초기화한 firedb를 사용
            firedb.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String email = documentSnapshot.getString("email");
                            userName.setText(name + "님");

                            if (email != null) {
                                loadSubscriptions(email);
                            } else {
                                Toast.makeText(this, "사용자 이메일 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
        }

        // ▲▲▲▲▲ 수정된 부분 끝 ▲▲▲▲▲

        recyclerView = findViewById(R.id.subscriptionRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubscriptionAdapter(subscriptionList);
        recyclerView.setAdapter(adapter);

        TextView profileImage = findViewById(R.id.profileImage);
        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            intent.putExtra("totalOverallAmount", totalOverall);
            intent.putExtra("subscriptionCount", subscriptionList.size());
            startActivity(intent);
        });

        calendarButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CalendarActivity.class);
            intent.putExtra("totalCurrentMonth", totalCurrentMonth);
            intent.putStringArrayListExtra("paymentDates", paymentDates);
            intent.putExtra("subscriptionList", (Serializable) subscriptionList);
            startActivity(intent);
        });

        // 알림 권한 요청
        checkAndRequestNotificationPermission();

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
                Date startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(s.getDate());
                Calendar paymentDate = Calendar.getInstance();
                if (startDate != null) {
                    paymentDate.setTime(startDate);
                }

                String renewalStr = s.getRenewalPrice();
                String[] parts = renewalStr.split("/");
                String unit = parts.length > 1 ? parts[1] : "1개월";

                int price = parsePriceString(s.getRenewalPrice());

                while (paymentDate.before(now)) {
                    if (unit.contains("년")) {
                        paymentDate.add(Calendar.YEAR, 1);
                    } else {
                        paymentDate.add(Calendar.MONTH, 1);
                    }
                }

                long diffMillis = paymentDate.getTimeInMillis() - now.getTimeInMillis();
                long diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis);

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
                        paymentDates.clear();
                        totalCurrentMonth = 0;
                        totalOverall = 0;

                        Calendar now = Calendar.getInstance();
                        int currentYear = now.get(Calendar.YEAR);
                        int currentMonthVal = now.get(Calendar.MONTH) + 1;

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Subscription s = doc.toObject(Subscription.class);
                            subscriptionList.add(s);

                            if (s.getDate() != null && !s.getDate().isEmpty()) {
                                paymentDates.add(s.getDate());
                            }

                            try {
                                Date startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(s.getDate());
                                Calendar subStart = Calendar.getInstance();
                                if(startDate != null) {
                                    subStart.setTime(startDate);
                                }
                                int subYear = subStart.get(Calendar.YEAR);
                                int subMonth = subStart.get(Calendar.MONTH) + 1;

                                totalCurrentMonth += getMonthlyPayment(s.getRenewalPrice(), subMonth, subYear, currentMonthVal, currentYear);

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

                        totalSubscriptionAmount.setText(String.format(Locale.getDefault(), "₩%,d", totalCurrentMonth));
                        paymentComplete.setText(currentMonthVal + "월");
                        totalOverallAmount.setText(String.format(Locale.getDefault(), "₩%,d", totalOverall));

                        NextPayment nextPay = getNextPayment(subscriptionList);
                        if (nextPay != null) {
                            nextPaymentDate.setText(nextPay.daysLeft + "일 뒤");
                            nextPaymentPrice.setText(String.format(Locale.getDefault(), "₩%,d", nextPay.price));
                            mainTitle.setText(nextPay.serviceName + ", " + nextPay.daysLeft + "일 뒤 결제돼요");

                            // 계산된 다음 결제 정보를 기반으로 알림을 예약합니다.
                            scheduleNotification(nextPay);

                        } else {
                            nextPaymentDate.setText("결제 예정 없음");
                            nextPaymentPrice.setText("");
                            mainTitle.setText("새로운 구독을 추가해보세요!");
                        }

                    } else {
                        Log.e("FirestoreDebug", "Failed to fetch subscriptions", task.getException());
                    }
                });
    }

    private int getMonthsBetween(String startDateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date startDate = sdf.parse(startDateStr);
            Calendar startCal = Calendar.getInstance();
            if(startDate != null) {
                startCal.setTime(startDate);
            }

            Calendar now = Calendar.getInstance();
            int yearDiff = now.get(Calendar.YEAR) - startCal.get(Calendar.YEAR);
            int monthDiff = now.get(Calendar.MONTH) - startCal.get(Calendar.MONTH);

            return yearDiff * 12 + monthDiff + 1;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private int parsePriceString(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) return 0;
        try {
            String[] parts = priceStr.split("/");
            return Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private int getMonthlyPayment(String renewalStr, int startMonth, int startYear, int currentMonth, int currentYear) {
        if (renewalStr == null || renewalStr.isEmpty()) return 0;
        try {
            String[] parts = renewalStr.split("/");
            int price = Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
            String unit = parts.length > 1 ? parts[1] : "1개월";

            if (unit.contains("년")) {
                return (startMonth == currentMonth && startYear <= currentYear) ? price : 0;
            } else {
                return price;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // 알림 권한이 있는지 확인하고, 없으면 요청하는 메서드
    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 (API 33) 이상
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    // 결제일 3일 전에 알림을 보내도록 AlarmManager에 예약을 설정하는 메서드
    private void scheduleNotification(NextPayment payment) {
        if (payment.daysLeft < 3) return; // 3일 미만 남은 결제는 알림을 설정하지 않음

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("title", payment.serviceName + " 결제 3일 전");
        intent.putExtra("message", "곧 " + String.format(Locale.getDefault(), "%,d", payment.price) + "원이 결제될 예정이에요.");

        // 각 알림이 고유하도록 PendingIntent에 고유한 requestCode를 부여합니다.
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, payment.serviceName.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        /*
        // 알림 시간 계산 (오늘 날짜 + (남은 일수 - 3)일)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, (int) (payment.daysLeft - 3));
        // 알림은 아침 9시에 울리도록 설정
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long triggerTime = calendar.getTimeInMillis();

        */

        // 임시 테스트 (10초 후)ㅗ
        long triggerTime = System.currentTimeMillis() + 10 * 1000;

        // 현재 시간보다 과거이면 알림을 설정하지 않음
        if (triggerTime < System.currentTimeMillis()) {
            return;
        }

        try {
            // 정확한 시간에 알림을 설정 (Doze 모드에서도 동작)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } catch (SecurityException e) {
            e.printStackTrace();
            // 사용자가 '알람 및 리마인더' 권한을 허용하지 않은 경우
            // 이 경우, 일반적인 set() 메서드를 사용할 수 있지만 정확성이 떨어집니다.
            // alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    private void setupBottomNavigation() {
        View bottomBar = findViewById(R.id.bottom_bar_include);

        // --- 수정된 부분: ImageView 대신 View로 선언 ---
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

        homeIcon.setColorFilter(selectedColor);
        homeText.setTextColor(selectedColor);
        homeText.setTypeface(null, android.graphics.Typeface.BOLD);

        chatIcon.setColorFilter(defaultColor);
        chatText.setTextColor(defaultColor);
        chatText.setTypeface(null, android.graphics.Typeface.NORMAL);

        usageIcon.setColorFilter(defaultColor);
        usageText.setTextColor(defaultColor);
        usageText.setTypeface(null, android.graphics.Typeface.NORMAL);

        btnHome.setOnClickListener(v -> {});

        btnAppUsage.setOnClickListener(v -> {

            // 1. UsageStatsActivity로 보낼 값을 변수에 할당합니다.
            // 이 값은 앱의 로직에 따라 실제 계산된 값이어야 합니다.
            int totalCurrentMonthValue = totalCurrentMonth; // 예시 값

            // 2. Intent를 생성하여 출발지와 목적지를 지정합니다.
            Intent intent = new Intent(HomeActivity.this, UsageStatsActivity.class);

            // 3. putExtra() 메소드를 사용하여 데이터를 Intent에 담습니다.
            //    "key"와 value 쌍으로 데이터를 넣습니다.
            //    key 값("totalCurrentMonth")은 받는 쪽과 반드시 동일해야 합니다.
            intent.putExtra("totalCurrentMonth", totalCurrentMonthValue);
            intent.putExtra("subscriptionList", (Serializable) subscriptionList);

            // 4. startActivity()를 호출하여 Intent를 시스템에 전달하고 Activity를 시작합니다.
            startActivity(intent);
            finish();
        });

        btnChat.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ChatActivity.class));
            finish();
        });
    }
}