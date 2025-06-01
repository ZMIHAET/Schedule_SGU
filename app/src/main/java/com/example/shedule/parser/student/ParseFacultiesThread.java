package com.example.shedule.parser.student;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParseFacultiesThread extends Thread {
    private final Activity activity;
    private final Spinner facultySpinner;
    private final SharedPreferences prefs;
    private static final String PREFS_NAME = "schedule_prefs";
    private static final String KEY_FACULTIES = "faculties";
    private static final String KEY_LAST_PARSE_TIME = "faculties_last_parse";

    public ParseFacultiesThread(Activity activity, Spinner facultySpinner) {
        this.activity = activity;
        this.facultySpinner = facultySpinner;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void run() {
        List<String> faculties;

        long lastParseTime = prefs.getLong(KEY_LAST_PARSE_TIME, 0);
        long now = System.currentTimeMillis();
        long sevenDaysMillis = 7L * 24 * 60 * 60 * 1000;

        if (now - lastParseTime > sevenDaysMillis) {
            // Нужно парсить заново
            faculties = parseFaculties();

            if (!faculties.isEmpty()) {
                // Сохраняем в SharedPreferences
                String serialized = serializeList(faculties);
                prefs.edit()
                        .putString(KEY_FACULTIES, serialized)
                        .putLong(KEY_LAST_PARSE_TIME, now)
                        .apply();
            }
        } else {
            // Берем из SharedPreferences
            String savedFaculties = prefs.getString(KEY_FACULTIES, "");
            faculties = deserializeList(savedFaculties);
        }

        final List<String> finalFaculties = faculties;
        activity.runOnUiThread(() -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                    android.R.layout.simple_spinner_item, finalFaculties);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            facultySpinner.setAdapter(adapter);
        });
    }

    private List<String> parseFaculties() {
        List<String> faculties = new ArrayList<>();
        try {
            Document document = Jsoup.connect("https://www.sgu.ru/schedule")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .get();

            Elements facultyElements = document.select("div.accordion-container h3.accordion__header");

            for (Element faculty : facultyElements) {
                faculties.add(faculty.text().trim());
            }
            Log.d("Parser", faculties.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return faculties;
    }

    private String serializeList(List<String> list) {
        // Просто соединяем через |
        return String.join("|", list);
    }

    private List<String> deserializeList(String str) {
        if (str.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(str.split("\\|")));
    }
}
