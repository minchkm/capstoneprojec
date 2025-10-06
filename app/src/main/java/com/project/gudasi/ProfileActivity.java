package com.project.gudasi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView profileName;
    private TextView profileEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Firebase 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();
        // 데이터베이스 연결
        db = FirebaseFirestore.getInstance();

        // 뷰 초기화
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        TextView totalSubscriptionAmount = findViewById(R.id.totalSubscriptionAmount);
        TextView totalSubscriptionCount = findViewById(R.id.totalSubscriptionCount);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.logoutLayout).setOnClickListener(view -> showLogoutDialog());

        // HomeActivity에서 전달된 구독 정보 표시
        Intent intent = getIntent();
        int totalAmount = intent.getIntExtra("totalOverallAmount", 0);
        int totalCount = intent.getIntExtra("subscriptionCount", 0);
        totalSubscriptionAmount.setText("₩ " + String.format("%,d", totalAmount));
        totalSubscriptionCount.setText(String.format("%d개", totalCount));

        // Firestore에서 사용자 정보 가져오기
        loadUserProfile();
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String email = documentSnapshot.getString("email");

                            profileName.setText(name + " 님");
                            profileEmail.setText(email);
                            Log.d("ProfileActivity", "Firestore에서 사용자 정보 로드 성공");
                        } else {
                            Log.w("ProfileActivity", "Firestore에 사용자 문서가 존재하지 않음");
                            Toast.makeText(this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ProfileActivity", "Firestore 정보 로드 실패", e);
                        Toast.makeText(this, "정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // 사용자가 로그인하지 않은 경우 (이론적으로는 발생하기 어려움)
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            goToLoginActivity();
        }
    }

    private void showLogoutDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_logout, null);
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialog)
                .setView(dialogView)
                .create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true);

        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnLogout = dialogView.findViewById(R.id.btn_logout);

        btnCancel.setOnClickListener(view -> dialog.dismiss());
        btnLogout.setOnClickListener(view -> {
            dialog.dismiss();
            signOut(); // 로그아웃 처리
        });

        dialog.show();
    }

    private void signOut() {
        mAuth.signOut(); // Firebase에서 로그아웃
        Toast.makeText(ProfileActivity.this, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show();
        goToLoginActivity();
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}