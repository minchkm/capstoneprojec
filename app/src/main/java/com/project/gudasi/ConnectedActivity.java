package com.project.gudasi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ConnectedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected); // 연결 완료 레이아웃

        String uid = getIntent().getStringExtra("uid");
        String name = getIntent().getStringExtra("name");
        String email = getIntent().getStringExtra("email");

        Button confirmButton = findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(v -> {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(ConnectedActivity.this, HomeActivity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("name", name);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            }, 200);
        });
    }
}
