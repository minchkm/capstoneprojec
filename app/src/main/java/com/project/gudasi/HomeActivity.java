package com.project.gudasi;

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

    private RecyclerView recyclerView;
    private SubscriptionAdapter adapter;
    private List<Subscription> subscriptionList = new ArrayList<>();
    private ArrayList<String> paymentDates = new ArrayList<>();
    private FirebaseFirestore firedb;
    private TextView totalSubscriptionAmount;
    private TextView totalOverallAmount;
    private TextView nextPaymentDate;
    private TextView nextPaymentPrice;
    private TextView paymentComplete;
    private TextView mainTitle;
    private ImageView calendarButton;

    public int totalCurrentMonth = 0;
    int totalOverall = 0;

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

        FirebaseApp.initializeApp(this);
        firedb = FirebaseFirestore.getInstance();

        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name, email FROM user LIMIT 1", null);

        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex("name");
            int emailIndex = cursor.getColumnIndex("email");

            if (nameIndex != -1) {
                String name = cursor.getString(nameIndex);
                TextView userName = findViewById(R.id.userName);
                userName.setText(name + "님");
            }

            if (emailIndex != -1) {
                String email = cursor.getString(emailIndex);
                Log.d("DB_DEBUG", "로그인한 이메일: " + email);
                loadSubscriptions(email);
            } else {
                Log.e("DB_ERROR", "이메일 컬럼 인덱스를 찾을 수 없습니다.");
            }
            cursor.close();
        } else {
            Toast.makeText(this, "사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show();
        }
        db.close();

        recyclerView = findViewById(R.id.subscriptionRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubscriptionAdapter(subscriptionList);
        recyclerView.setAdapter(adapter);

        // --- 수정된 부분: profileImage를 ImageView에서 TextView로 변경 ---
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
            startActivity(new Intent(HomeActivity.this, UsageStatsActivity.class));
            finish();
        });

        btnChat.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ChatActivity.class));
            finish();
        });
    }
}