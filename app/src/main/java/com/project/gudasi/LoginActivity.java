package com.project.gudasi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private RecyclerView recyclerView;
    private AppIconAdapter adapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable autoScrollRunnable;

    private static final int SCROLL_DISTANCE = 10;
    private static final long SCROLL_DELAY = 150;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // ▼▼▼▼▼▼▼▼▼▼ 수정된 부분 ▼▼▼▼▼▼▼▼▼▼
        // 클릭 리스너의 대상을 'sign_in_button'에서 'sign_in_button_layout'으로 변경
        findViewById(R.id.sign_in_button_layout).setOnClickListener(v -> signIn());
        // ▲▲▲▲▲▲▲▲▲▲ 수정된 부분 ▲▲▲▲▲▲▲▲▲▲

        recyclerView = findViewById(R.id.appCarouselRecyclerView);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3, GridLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        List<AppItem> appList = Arrays.asList(
                new AppItem(R.drawable.ic_netflix, "넷플릭스"),
                new AppItem(R.drawable.ic_spotify, "스포티파이"),
                new AppItem(R.drawable.ic_youtube_music, "유튜브"),
                new AppItem(R.drawable.ic_melon, "멜론"),
                new AppItem(R.drawable.ic_coupang, "쿠팡"),
                new AppItem(R.drawable.ic_watcha, "왓챠")
        );

        adapter = new AppIconAdapter(appList);
        recyclerView.setAdapter(adapter);

        startAutoScroll();
    }

    private void signIn() {
        Log.d("Login", "Google 로그인 인텐트 시작");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("GoogleLogin", "ID Token: " + account.getIdToken());

                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "구글 로그인 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d("FirebaseAuth", "User: " + user);
                        if (user != null) {
                            Log.d("FirebaseAuth", "Name=" + user.getDisplayName() + " Email=" + user.getEmail());
                            String name = user.getDisplayName();
                            String email = user.getEmail();

                            DBHelper dbHelper = new DBHelper(this);
                            dbHelper.insertUser(name, email);

                            Intent intent = new Intent(LoginActivity.this, ConnectedActivity.class);
                            intent.putExtra("name", name);
                            intent.putExtra("email", email);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e("FirebaseAuth", "User is null even though task is successful");
                            Toast.makeText(this, "사용자 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("FirebaseAuth", "Login failed", task.getException());
                        Toast.makeText(this, "로그인 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startAutoScroll() {
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                recyclerView.scrollBy(SCROLL_DISTANCE, 0);
                handler.postDelayed(this, SCROLL_DELAY);
            }
        };
        handler.post(autoScrollRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(autoScrollRunnable);
    }
}