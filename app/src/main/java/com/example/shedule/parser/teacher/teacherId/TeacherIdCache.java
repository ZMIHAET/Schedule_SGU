package com.example.shedule.parser.teacher.teacherId;

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
                if (parts.length == 3) {
                    String lastName = parts[0];
                    String initials = parts[1].substring(0, 1) + "." + parts[2].substring(0, 1) + ".";
                    String key = lastName + " " + initials; // e.g. "Савин Д.В."

                    teacherIdMap.put(key, dataId);
                }
            }
            initialized = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized String getTeacherId(String shortName) {
        return teacherIdMap.get(shortName);
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
