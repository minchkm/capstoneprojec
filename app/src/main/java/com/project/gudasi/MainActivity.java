package com.project.gudasi;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webView);

        dbHelper = new DBHelper(this);
        String htmlContent = generateHTML();
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 뒤로가기 아이콘 보이게 설정
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_24); // 원하면 커스텀 아이콘 지정
    }

    private String generateHTML() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        StringBuilder html = new StringBuilder();

        html.append("<html><head>")
                .append("<style>")
                .append("table { width: 100%; border-collapse: collapse; margin-bottom: 30px; }")
                .append("th, td { border: 1px solid #ccc; padding: 8px; text-align: center; }")
                .append("th { background-color: #f2f2f2; }")
                .append("h2 { text-align: center; margin-top: 40px; }")
                .append("</style>")
                .append("</head><body>");

        // 각 테이블 출력
        html.append(getTableHTML(db, "user", new String[]{"uid", "name", "email"}, "User Info"));
        html.append(getTableHTML(db, "subscription", new String[]{"id", "icon", "app_name", "price", "renewal_date"}, "Subscription Info"));
        html.append(getTableHTML(db, "ott_rank", new String[]{"rank", "app_name", "user_count"}, "OTT Rank"));
        html.append(getTableHTML(db, "streaming_rank", new String[]{"rank", "app_name", "user_count"}, "Streaming Rank"));
        html.append(getTableHTML(db, "cloud_rank", new String[]{"rank", "app_name", "user_count"}, "Cloud Rank"));

        html.append("</body></html>");
        db.close();
        return html.toString();
    }

    private String getTableHTML(SQLiteDatabase db, String tableName, String[] columns, String title) {
        StringBuilder tableHTML = new StringBuilder();
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName, null);

        tableHTML.append("<h2>").append(title).append("</h2>");
        tableHTML.append("<table><tr>");

        // 헤더
        for (String col : columns) {
            tableHTML.append("<th>").append(col).append("</th>");
        }
        tableHTML.append("</tr>");

        // 데이터
        while (cursor.moveToNext()) {
            tableHTML.append("<tr>");
            for (String col : columns) {
                int colIndex = cursor.getColumnIndex(col);
                String value = (colIndex != -1 && !cursor.isNull(colIndex)) ? cursor.getString(colIndex) : "";
                tableHTML.append("<td>").append(value).append("</td>");
            }
            tableHTML.append("</tr>");
        }

        tableHTML.append("</table>");
        cursor.close();
        return tableHTML.toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // 프로필 화면으로 이동
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish(); // MainActivity 종료해서 뒤로 가기 방지
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
