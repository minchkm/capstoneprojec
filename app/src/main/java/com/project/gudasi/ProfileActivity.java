package com.project.gudasi;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

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
        totalSubscriptionCount.setText(String.format("%d개", totalCount));

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());


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
                profileName.setText(name + " 님");


                TextView profileEmail = findViewById(R.id.profileEmail);
                profileEmail.setText(email);

            } else {
                Log.e("DB_ERROR", "컬럼 인덱스를 찾을 수 없습니다.");
            }
        } else {
            Toast.makeText(this, "사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show();
        }

        if (cursor != null) cursor.close();
        db.close();

        TextView logoutLayout = findViewById(R.id.logoutLayout);
        logoutLayout.setOnClickListener(view -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_logout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true);
        dialog.show();


        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnLogout = dialogView.findViewById(R.id.btn_logout);

        btnCancel.setOnClickListener(view -> dialog.dismiss());

        btnLogout.setOnClickListener(view -> {
            Toast.makeText(ProfileActivity.this, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            // DBHelper를 사용하여 사용자 정보 삭제
            DBHelper dbHelper = new DBHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("DELETE FROM user");
            db.close();


            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}