package com.example.shedule.parser.teacher.teacherId;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class TeacherIdCache {
    private static final String PREF_NAME = "teacher_cache_prefs";
    private static final String KEY_CACHE = "teacher_cache_json";
    private static final String KEY_TIMESTAMP = "teacher_cache_time";

    private static final HashMap<String, String> teacherIdMap = new HashMap<>();
    private static boolean initialized = false;

    public static synchronized void initializeCache(Context context) {
        if (initialized) return;

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long lastUpdateTime = prefs.getLong(KEY_TIMESTAMP, 0);
        long now = System.currentTimeMillis();

        long oneWeekMillis = 7L * 24 * 60 * 60 * 1000;

        if (now - lastUpdateTime < oneWeekMillis) {
            // Загружаем из SharedPreferences
            String jsonString = prefs.getString(KEY_CACHE, "");
            if (!jsonString.isEmpty()) {
                try {
                    JSONObject json = new JSONObject(jsonString);
                    Iterator<String> keys = json.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        teacherIdMap.put(key, json.getString(key));
                    }
                    initialized = true;
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                    // если ошибка — пробуем парсить заново
                }
            }
        }

        // Парсим сайт и сохраняем кэш
        try {
            Document doc = Jsoup.connect("https://www.sgu.ru/schedule").get();
            Elements teacherElements = doc.select("ul#search-results li");

            teacherIdMap.clear();

            for (Element el : teacherElements) {
                String fullName = el.text(); // "Савин Дмитрий Владимирович"
                String dataId = el.attr("data-id");

                String[] parts = fullName.split(" ");
                if (parts.length > 0) {
                    String lastName = parts[0];
                    teacherIdMap.put(lastName, dataId);
                }
            }

            // Сохраняем в SharedPreferences
            JSONObject jsonToSave = new JSONObject(teacherIdMap);
            prefs.edit()
                    .putString(KEY_CACHE, jsonToSave.toString())
                    .putLong(KEY_TIMESTAMP, now)
                    .apply();

            initialized = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized String getTeacherId(String shortName) {
        String[] parts = shortName.split(" ");
        return teacherIdMap.get(parts[0]);
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static String[] getAllTeachers() {
        return teacherIdMap.keySet().toArray(new String[0]);
    }

}
