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

import com.google.firebase.FirebaseApp; // FirebaseApp import 추가
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private FirebaseFirestore db;

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

        // 기본 DB에 연결합니다.
        db = FirebaseFirestore.getInstance();

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
                new AppItem(R.drawable.ic_watcha, "왓챠"),
                new AppItem(R.drawable.ic_disney_plus, "디즈니플러스"),
                new AppItem(R.drawable.ic_tving, "티빙"),
                new AppItem(R.drawable.ic_wavve, "웨이브"),
                new AppItem(R.drawable.ic_apple_tv, "애플티비"),
                new AppItem(R.drawable.ic_kakao_emoticon, "카카오이모티콘"),
                new AppItem(R.drawable.ic_bugs, "벅스"),
                new AppItem(R.drawable.ic_genie, "지니"),
                new AppItem(R.drawable.ic_flo, "플로")
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
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                firebaseAuthWithGoogle(account.getIdToken());
            }
        } catch (ApiException e) {
            Log.w("LoginActivity", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // ▼▼▼▼▼ 기존 SQLite 코드를 아래 코드로 변경 ▼▼▼▼▼
                            saveUserToFirestore(user);
                            // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲
                        }
                    } else {
                        // 로그인 실패 처리
                    }
                });
    }

    // ▼▼▼▼▼ 사용자 정보를 Firestore에 저장하는 새 함수 ▼▼▼▼▼
    private void saveUserToFirestore(FirebaseUser user) {
        String name = user.getDisplayName();
        String email = user.getEmail();
        String uid = user.getUid(); // 사용자의 고유 ID

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("name", name);
        userProfile.put("email", email);

        // "users" 라는 컬렉션(폴더)에 사용자의 UID를 문서(파일) 이름으로 하여 정보를 저장
        db.collection("users").document(uid).set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    Log.d("LoginActivity", "Firestore에 사용자 정보 저장 성공");
                    // 저장 성공 후 다음 화면으로 이동
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w("LoginActivity", "Firestore에 사용자 정보 저장 실패", e);
                    // 실패 처리 (예: 에러 메시지 표시)
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