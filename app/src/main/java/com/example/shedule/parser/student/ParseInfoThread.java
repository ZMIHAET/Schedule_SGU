package com.example.shedule.parser.student;

import android.app.Activity;
import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.example.shedule.activity.student.InfoActivity;
import com.example.shedule.activity.student.MainActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class ParseInfoThread extends Thread {
    private final String faculty;
    private final Activity activity;
    private static final String PREFS_NAME = "info_prefs";
    private static final String KEY_INFO_TEXT = "info_text";
    private static final String KEY_INFO_TIMESTAMP = "info_timestamp";
    private static final long CACHE_DURATION = 7L * 24 * 60 * 60 * 1000; // 7 дней


    public ParseInfoThread(String faculty, Activity activity) {
        this.faculty = faculty;
        this.activity = activity;
    }

    @Override
    public void run() {
        try {
            android.content.SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, 0);
            long savedTime = prefs.getLong(KEY_INFO_TIMESTAMP, 0);
            long now = System.currentTimeMillis();
            String cachedInfo = prefs.getString(KEY_INFO_TEXT, null);

            if (cachedInfo != null && now - savedTime < CACHE_DURATION) {
                showInfo(cachedInfo);
                return;
            }

            Document doc = Jsoup.connect("https://www.sgu.ru/schedule").get();

            Elements containers = doc.select(".accordion-container");

            for (Element container : containers) {
                Element title = container.selectFirst("h3.accordion__header");
                if (title != null && title.text().trim().equalsIgnoreCase(faculty)) {
                    Element info = container.selectFirst("div.schedule__info span");
                    if (info != null) {
                        String infoText = info.text().trim();

                        // Сохраняем
                        prefs.edit()
                                .putString(KEY_INFO_TEXT, infoText)
                                .putLong(KEY_INFO_TIMESTAMP, System.currentTimeMillis())
                                .apply();

                        showInfo(infoText);
                    }
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showInfo(String infoText) {
        activity.runOnUiThread(() -> {
            Intent intent = new Intent(activity, InfoActivity.class);
            intent.putExtra("infoText", infoText);
            activity.startActivity(intent);
        });
    }

}
