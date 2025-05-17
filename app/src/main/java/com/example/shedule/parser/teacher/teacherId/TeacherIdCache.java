package com.example.shedule.parser.teacher.teacherId;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;

public class TeacherIdCache {
    private static final HashMap<String, String> teacherIdMap = new HashMap<>();
    private static boolean initialized = false;

    public static synchronized void initializeCache() {
        if (initialized) return;

        try {
            Document doc = Jsoup.connect("https://www.sgu.ru/schedule").get();
            Elements teacherElements = doc.select("ul#search-results li");

            for (Element el : teacherElements) {
                String fullName = el.text(); // "Савин Дмитрий Владимирович"
                String dataId = el.attr("data-id");

                String[] parts = fullName.split(" ");
                String lastName = parts[0];

                teacherIdMap.put(lastName, dataId);
            }
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
}
