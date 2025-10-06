package com.project.gudasi;

import android.app.Dialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UsageStatsActivity extends AppCompatActivity {

    private LinearLayout subscriptionUsageLayout;
    private List<Subscription> subscriptionList;
    private static final Map<String, String> servicePackageMap = new HashMap<>();

    // 각 서비스 이름과 패키지 이름을 매핑합니다.
    static {
        servicePackageMap.put("youtube", "com.google.android.youtube");
        servicePackageMap.put("netflix", "com.netflix.mediaclient");
        servicePackageMap.put("tving", "net.cj.cjhv.gs");
        servicePackageMap.put("wavve", "pooq.android.player");
        servicePackageMap.put("coupang", "com.coupang.mobile");
        servicePackageMap.put("disney+", "com.disney.disneyplus");
        servicePackageMap.put("watcha", "com.frograms.watcha");
        servicePackageMap.put("spotify", "com.spotify.music");
        servicePackageMap.put("melon", "com.iloen.melon");
        // 필요한 다른 앱들도 여기에 추가할 수 있습니다.
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_stats);

        subscriptionUsageLayout = findViewById(R.id.subscriptionUsageLayout);

        Intent intent = getIntent();
        int totalCurrentMonth = intent.getIntExtra("totalCurrentMonth", 0);
        subscriptionList = (List<Subscription>) intent.getSerializableExtra("subscriptionList");

        TextView monthlyUsage = findViewById(R.id.monthlyUsage);
        monthlyUsage.setText(String.format(Locale.getDefault(), "₩ %,d", totalCurrentMonth));

        if (!hasUsageStatsPermission()) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        } else {
            if (subscriptionList != null && !subscriptionList.isEmpty()) {
                displayAppUsage();
            }
        }

        setupBottomNavigation();
    }

    private boolean hasUsageStatsPermission() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long now = System.currentTimeMillis();
        List<UsageStats> stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, now - (1000 * 60), now);
        return stats != null && !stats.isEmpty();
    }

    private void displayAppUsage() {
        LayoutInflater inflater = LayoutInflater.from(this);
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long now = System.currentTimeMillis();
        long oneMonthAgo = now - TimeUnit.DAYS.toMillis(30);

        List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_MONTHLY, oneMonthAgo, now);

        for (Subscription sub : subscriptionList) {
            String serviceName = sub.getServiceName().toLowerCase();
            String packageName = getPackageNameForService(serviceName);
            long usageTime = getAppUsage(stats, packageName);

            View usageView = inflater.inflate(R.layout.item_app_usage, subscriptionUsageLayout, false);

            ImageView appIcon = usageView.findViewById(R.id.app_icon);
            TextView appName = usageView.findViewById(R.id.app_name);
            TextView usageTimeText = usageView.findViewById(R.id.usage_time);
            TextView usageNote = usageView.findViewById(R.id.usage_note);
            ProgressBar progressBar = usageView.findViewById(R.id.progressBar);
            MaterialButton cancelButton = usageView.findViewById(R.id.btnCancel);

            appIcon.setImageResource(ServiceIconMapper.iconOfCanonical(serviceName));
            appName.setText(sub.getServiceName());
            usageTimeText.setText("사용시간: " + formatMillis(usageTime));

            updateUsageUI(progressBar, usageNote, usageTime);

            cancelButton.setOnClickListener(v -> showCancelDialog(sub.getServiceName(), getCancelUrlForService(serviceName)));

            subscriptionUsageLayout.addView(usageView);
        }
    }

    private String getPackageNameForService(String serviceName) {
        for (Map.Entry<String, String> entry : servicePackageMap.entrySet()) {
            if (serviceName.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        // 기본적으로는 서비스 이름을 기반으로 추측 (정확하지 않을 수 있음)
        return "com.project." + serviceName;
    }


    private long getAppUsage(List<UsageStats> stats, String packageName) {
        if (packageName == null) return 0;
        for (UsageStats usage : stats) {
            if (packageName.equals(usage.getPackageName())) {
                return usage.getTotalTimeInForeground();
            }
        }
        return 0;
    }

    private String formatMillis(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d시간 %d분", hours, minutes);
        } else {
            return String.format(Locale.getDefault(), "%d분", minutes);
        }
    }

    private void updateUsageUI(ProgressBar pb, TextView tv, long usageMillis) {
        long usageHours = TimeUnit.MILLISECONDS.toHours(usageMillis);
        int low = 2;    // 사용량 적음 기준 (2시간 미만)
        int normal = 10; // 사용량 적정 기준 (10시간 미만)
        int high = 20;  // 최대 기준 시간 (프로그레스바 계산용)

        int progress = (int) Math.min(((float) usageMillis / TimeUnit.HOURS.toMillis(high)) * 100, 100);
        pb.setProgress(progress);

        if (usageHours < low) {
            tv.setText("사용량 적음");
            tv.setTextColor(Color.parseColor("#F44336")); // 빨강
            pb.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bar_gradient_red, getTheme()));
        } else if (usageHours < normal) {
            tv.setText("사용량 적정");
            tv.setTextColor(Color.parseColor("#FFC107")); // 노랑
            pb.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bar_gradient_yellow, getTheme()));
        } else {
            tv.setText("사용량 많음");
            tv.setTextColor(Color.parseColor("#4CAF50")); // 초록
            // progress_bar_gradient_green.xml 파일이 없으므로 노란색으로 대체합니다.
            // 필요하다면 해당 파일을 추가하세요.
            pb.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bar_gradient_yellow, getTheme()));
        }
    }


    private void showCancelDialog(String appName, String cancelUrl) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_low_usage);
        dialog.setCancelable(true);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            window.setAttributes(layoutParams);
        }

        TextView appNameTextView = dialog.findViewById(R.id.dialog_app_name);
        if (appNameTextView != null) {
            appNameTextView.setText(appName);
        }

        MaterialButton btnCancel = dialog.findViewById(R.id.btn_cancel);
        MaterialButton btnIgnore = dialog.findViewById(R.id.btn_ignore);

        btnCancel.setOnClickListener(view -> {
            if (cancelUrl != null && !cancelUrl.isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(cancelUrl));
                startActivity(browserIntent);
            }
            dialog.dismiss();
        });

        btnIgnore.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }


    private String getCancelUrlForService(String serviceName) {
        if (serviceName.contains("youtube")) {
            return "https://www.youtube.com/paid_memberships";
        }
        if (serviceName.contains("netflix")) {
            return "https://www.netflix.com/cancelplan";
        }
        // 다른 서비스들의 해지 URL 추가
        return null; // URL이 없는 경우
    }


    private void setupBottomNavigation() {
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

        int defaultColor = Color.parseColor("#8A94A4");
        int selectedColor = Color.parseColor("#007BFF");

        homeIcon.setColorFilter(defaultColor);
        homeText.setTextColor(defaultColor);
        homeText.setTypeface(null, android.graphics.Typeface.NORMAL);

        chatIcon.setColorFilter(defaultColor);
        chatText.setTextColor(defaultColor);
        chatText.setTypeface(null, android.graphics.Typeface.NORMAL);

        usageIcon.setColorFilter(selectedColor);
        usageText.setTextColor(selectedColor);
        usageText.setTypeface(null, android.graphics.Typeface.BOLD);


        btnHome.setOnClickListener(v -> {
            startActivity(new Intent(UsageStatsActivity.this, HomeActivity.class));
            finish();
        });

        btnChat.setOnClickListener(v -> {
            startActivity(new Intent(UsageStatsActivity.this, ChatActivity.class));
            finish();
        });

        btnAppUsage.setOnClickListener(v -> {
            // 현재 페이지이므로 아무 동작 없음
        });
    }
}