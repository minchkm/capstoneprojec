package com.project.gudasi;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "gudasi.db";
    private static final int DATABASE_VERSION = 3;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. user 테이블
        String CREATE_USER_TABLE = "CREATE TABLE user (" +
                "name TEXT NOT NULL, " +
                "email TEXT NOT NULL" +
                ")";
        db.execSQL(CREATE_USER_TABLE);

        // 2. subscription 테이블
        String CREATE_SUBSCRIPTION_TABLE = "CREATE TABLE subscription (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "date TEXT NOT NULL, " +
                "service_name TEXT NOT NULL, " +
                "purchase_date TEXT NOT NULL, " +
                "price TEXT NOT NULL" +
                ")";
        db.execSQL(CREATE_SUBSCRIPTION_TABLE);

        // 기타 랭크 테이블 (필요시 추가)
        String CREATE_OTT_RANK_TABLE = "CREATE TABLE ott_rank (" +
                "rank INTEGER PRIMARY KEY, " +
                "app_name TEXT NOT NULL, " +
                "user_count INTEGER NOT NULL)";
        db.execSQL(CREATE_OTT_RANK_TABLE);

        String CREATE_STREAMING_RANK_TABLE = "CREATE TABLE streaming_rank (" +
                "rank INTEGER PRIMARY KEY, " +
                "app_name TEXT NOT NULL, " +
                "user_count INTEGER NOT NULL)";
        db.execSQL(CREATE_STREAMING_RANK_TABLE);

        String CREATE_CLOUD_RANK_TABLE = "CREATE TABLE cloud_rank (" +
                "rank INTEGER PRIMARY KEY, " +
                "app_name TEXT NOT NULL, " +
                "user_count INTEGER NOT NULL)";
        db.execSQL(CREATE_CLOUD_RANK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS user");
        db.execSQL("DROP TABLE IF EXISTS subscription");
        db.execSQL("DROP TABLE IF EXISTS ott_rank");
        db.execSQL("DROP TABLE IF EXISTS streaming_rank");
        db.execSQL("DROP TABLE IF EXISTS cloud_rank");
        onCreate(db);
    }

    public void insertUser(String name, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM user");
        db.execSQL("INSERT INTO user (name, email) VALUES (?, ?)",
                new Object[]{name, email});
        db.close();
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name, email FROM user", null);

        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            String email = cursor.getString(1);
            userList.add(new User(name, email));
        }

        cursor.close();
        db.close();
        return userList;
    }

    public void insertSubscription(String title, String date, String serviceName, String purchaseDate, String price) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO subscription (title, date, service_name, purchase_date, price) VALUES (?, ?, ?, ?, ?)",
                new Object[]{title, date, serviceName, purchaseDate, price});
        db.close();
    }

    public List<Subscription> getAllSubscriptions() {
        List<Subscription> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, title, date, service_name, purchase_date, price FROM subscription", null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String title = cursor.getString(1);
            String date = cursor.getString(2);
            String serviceName = cursor.getString(3);
            String purchaseDate = cursor.getString(4);
            String price = cursor.getString(5);

            list.add(new Subscription(id, title, date, serviceName, purchaseDate, price));

        }

        cursor.close();
        db.close();
        return list;
    }
}
