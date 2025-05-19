package com.example.shedule.parser.teacher.checkTeachers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TeacherParser {
    private static final String PREF_NAME = "teacher_cache";
    private static final String KEY_TEACHERS_JSON = "teachers_json";
    private static final String KEY_TIMESTAMP = "timestamp";

    public static void parseTeachers(Context context, String url) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long lastFetch = prefs.getLong(KEY_TIMESTAMP, 0);
        long now = System.currentTimeMillis();

        // Если прошло менее 7 дней — читаем из кэша
        if (now - lastFetch < TimeUnit.DAYS.toMillis(7)) {
            String json = prefs.getString(KEY_TEACHERS_JSON, null);
            if (json != null) {
                loadFromJson(json);
                return;
            }
        }

        // Иначе парсим с сайта
        try {
            Document doc = Jsoup.connect(url).get();
            Elements teacherElements = doc.select("div.list-employee__info");

            JSONArray jsonArray = new JSONArray();

            for (Element teacherElement : teacherElements) {
                Element fioElement = teacherElement.selectFirst("div.list-employee__fio > a");
                if (fioElement == null) continue;

                String fullName = fioElement.text().trim();
                String[] parts = fullName.split(" ");

                if (parts.length >= 2) {
                    String lastName = parts[0];
                    String firstName = parts[1];
                    String patronymic = (parts.length > 2) ? parts[2] : "";

                    Element deptElement = teacherElement.selectFirst("div.list-employee__subdivision");
                    String department = (deptElement != null) ? deptElement.text().trim() : "Неизвестная кафедра";

                    TeacherList.addTeacher(lastName, firstName, patronymic, department);

                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("lastName", lastName);
                    jsonObj.put("firstName", firstName);
                    jsonObj.put("patronymic", patronymic);
                    jsonObj.put("department", department);
                    jsonArray.put(jsonObj);
                }
            }

            // Сохраняем в SharedPreferences
            prefs.edit()
                    .putString(KEY_TEACHERS_JSON, jsonArray.toString())
                    .putLong(KEY_TIMESTAMP, now)
                    .apply();

        } catch (IOException e) {
            Log.e("TeacherParser", "Ошибка при парсинге: " + e.getMessage());
        } catch (Exception e) {
            Log.e("TeacherParser", "Ошибка сериализации: " + e.getMessage());
        }
    }

    private static void loadFromJson(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String lastName = obj.getString("lastName");
                String firstName = obj.getString("firstName");
                String patronymic = obj.getString("patronymic");
                String department = obj.getString("department");

                TeacherList.addTeacher(lastName, firstName, patronymic, department);
            }
        } catch (Exception e) {
            Log.e("TeacherParser", "Ошибка при чтении JSON: " + e.getMessage());
        }
    }
}
