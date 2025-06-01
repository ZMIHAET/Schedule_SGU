package com.example.shedule.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TABLE_SCHEDULE = "schedule_cache";
    public static final String COLUMN_SCHEDULE_DAY = "day_of_week";
    public static final String COLUMN_SCHEDULE_URL = "url";
    public static final String COLUMN_SCHEDULE_DATA = "data";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String DATABASE_NAME = "users.db";
    private static final int DATABASE_VERSION = 2;
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FIRST_NAME = "firstName";
    public static final String COLUMN_LAST_NAME = "lastName";
    public static final String COLUMN_PATRONYMIC = "patronymic";
    public static final String COLUMN_PASSWORD = "password"; // В реальном приложении храни хэш
    public static final String COLUMN_ROLE = "role"; // "Студент" или "Преподаватель"

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_FIRST_NAME + " TEXT, " +
                    COLUMN_LAST_NAME + " TEXT, " +
                    COLUMN_PATRONYMIC + " TEXT, " +
                    COLUMN_PASSWORD + " TEXT, " +
                    COLUMN_ROLE + " TEXT);";

    private static final String CREATE_SCHEDULE_TABLE =
            "CREATE TABLE " + TABLE_SCHEDULE + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_SCHEDULE_URL + " TEXT, " +
                    COLUMN_SCHEDULE_DAY + " INTEGER, " +
                    COLUMN_SCHEDULE_DATA + " TEXT, " +
                    COLUMN_TIMESTAMP + " INTEGER)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void clearScheduleCache(SQLiteDatabase db) {
        db.delete(TABLE_SCHEDULE, null, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SCHEDULE_TABLE);

        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(CREATE_SCHEDULE_TABLE);
        }
    }


    public String getCachedSchedule(SQLiteDatabase db, String url, int dayOfWeek) {
        Cursor cursor = db.rawQuery("SELECT data, timestamp FROM " + TABLE_SCHEDULE + " WHERE " +
                "url = ? AND day_of_week = ?", new String[]{url, String.valueOf(dayOfWeek)});

        if (cursor.moveToFirst()) {
            long timestamp = cursor.getLong(1);
            long now = System.currentTimeMillis();
            long sevenDaysMillis = 7 * 24 * 60 * 60 * 1000L;

            if (now - timestamp <= sevenDaysMillis) {
                String data = cursor.getString(0);
                cursor.close();
                return data;
            }
        }
        cursor.close();
        return null;
    }
    public void saveScheduleToCache(SQLiteDatabase db, String url, int dayOfWeek, String data) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SCHEDULE_URL, url);
        values.put(COLUMN_SCHEDULE_DAY, dayOfWeek);
        values.put(COLUMN_SCHEDULE_DATA, data);
        values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());

        db.delete(TABLE_SCHEDULE, "url=? AND day_of_week=?", new String[]{url, String.valueOf(dayOfWeek)});
        db.insert(TABLE_SCHEDULE, null, values);
    }


}

