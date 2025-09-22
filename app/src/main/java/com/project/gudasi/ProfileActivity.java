package com.project.gudasi;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView totalSubscriptionAmount = findViewById(R.id.totalSubscriptionAmount);
        TextView totalSubscriptionCount = findViewById(R.id.totalSubscriptionCount);

        Intent intent = getIntent();
        int totalAmount = intent.getIntExtra("totalOverallAmount", 0);
        int totalCount = intent.getIntExtra("subscriptionCount", 0);

        totalSubscriptionAmount.setText("₩ " + String.format("%,d", totalAmount));
        totalSubscriptionCount.setText(String.format("%,d", totalCount) + "개");

        // --- 뒤로가기 버튼 클릭 리스너 추가 ---
        ImageView backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        // ------------------------------------

        // --- 데이터베이스에서 사용자 정보 가져오기 ---
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM user", null);

        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex("name");
            int emailIndex = cursor.getColumnIndex("email");

            if (nameIndex != -1 && emailIndex != -1) {
                String name = cursor.getString(nameIndex);
                String email = cursor.getString(emailIndex);

                TextView profileName = findViewById(R.id.profileName);
                if (profileName != null) {
                    profileName.setText(name);
                }

                TextView profileEmail = findViewById(R.id.profileEmail);
                if (profileEmail != null) {
                    profileEmail.setText(email);
                }
            } else {
                Log.e("DB_ERROR", "컬럼 인덱스를 찾을 수 없습니다.");
            }
        } else {
            Toast.makeText(this, "사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show();
        }

        if (cursor != null) cursor.close();
        db.close();

        // --- ProfileActivity에 존재하는 UI 요소에 대한 리스너 설정 ---
        LinearLayout showInfoLayout = findViewById(R.id.showInfoLayout);
        if (showInfoLayout != null) {
            showInfoLayout.setOnClickListener(v -> {
                Intent intent2 = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent2);
                finish();
            });
        }

        LinearLayout logoutLayout = findViewById(R.id.logoutLayout);
        if (logoutLayout != null) {
            logoutLayout.setOnClickListener(view -> showLogoutDialog());
        }
    }

    private void showLogoutDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_logout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.show();

        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnLogout = dialogView.findViewById(R.id.btn_logout);

        btnCancel.setOnClickListener(view -> dialog.dismiss());

        btnLogout.setOnClickListener(view -> {
            Toast.makeText(ProfileActivity.this, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });
    }
}