package com.example.shedule.parser.student;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParseGroupsThread extends Thread {
    private final String faculty;
    private final Activity activity;
    private final Spinner courseSpinner;
    private final Spinner groupSpinner;

    private final SharedPreferences prefs;
    private static final String PREFS_NAME = "schedule_prefs";
    private static final String KEY_GROUPS_PREFIX = "groups_";  // + faculty
    private static final String KEY_LAST_PARSE_TIME_PREFIX = "groups_last_parse_"; // + faculty

    public ParseGroupsThread(Activity activity, String faculty, Spinner courseSpinner, Spinner groupSpinner) {
        this.activity = activity;
        this.faculty = faculty;
        this.courseSpinner = courseSpinner;
        this.groupSpinner = groupSpinner;
        this.prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void run() {
        List<String> groups;

        long lastParseTime = prefs.getLong(KEY_LAST_PARSE_TIME_PREFIX + faculty, 0);
        long now = System.currentTimeMillis();
        long sevenDaysMillis = 7L * 24 * 60 * 60 * 1000;

        if (now - lastParseTime > sevenDaysMillis) {
            groups = parseGroups(faculty);

            if (!groups.isEmpty()) {
                String serialized = serializeList(groups);
                prefs.edit()
                        .putString(KEY_GROUPS_PREFIX + faculty, serialized)
                        .putLong(KEY_LAST_PARSE_TIME_PREFIX + faculty, now)
                        .apply();
            }
        } else {
            String savedGroups = prefs.getString(KEY_GROUPS_PREFIX + faculty, "");
            groups = deserializeList(savedGroups);
        }

        final List<String> finalGroups = groups;
        activity.runOnUiThread(() -> {
            List<String> courses = new ArrayList<>();
            if (finalGroups.isEmpty()) {
                // Пусто, поставим пустой адаптер
                courseSpinner.setAdapter(null);
                groupSpinner.setAdapter(null);
                return;
            }

            char lastCourse = finalGroups.get(finalGroups.size() - 1).charAt(0);
            int courseQuantity = 2;
            while (!Character.isDigit(lastCourse)) {
                lastCourse = finalGroups.get(finalGroups.size() - courseQuantity).charAt(0);
                courseQuantity++;
            }
            for (int i = 0; i < Character.getNumericValue(lastCourse); i++) {
                int k = i + 1;
                String numberOfCourse = String.valueOf(k);
                String course = numberOfCourse + " курс";
                courses.add(i, course);
            }

            ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(activity,
                    android.R.layout.simple_spinner_item, courses);
            courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            courseSpinner.setAdapter(courseAdapter);

            courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    List<String> currentGroups = new ArrayList<>();
                    for (String group : finalGroups) {
                        if (group.charAt(0) == courseSpinner.getSelectedItem().toString().charAt(0)) {
                            currentGroups.add(group);
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                            android.R.layout.simple_spinner_item, currentGroups);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    groupSpinner.setAdapter(adapter);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            // Чтобы триггернуть выбор, выставим позицию 0
            if (!courses.isEmpty()) {
                courseSpinner.setSelection(0);
            }
        });
    }

    private List<String> parseGroups(String facultyName) {
        List<String> groups = new ArrayList<>();
        try {
            Document document = Jsoup.connect("https://www.sgu.ru/schedule")
                    .userAgent("Mozilla/5.0").get();

            Elements facultyElements = document.select("div.accordion-container:has(h3:contains(" + facultyName + "))");

            if (!facultyElements.isEmpty()) {
                Element facultyElement = facultyElements.first();

                Elements allForms = facultyElement.select("div.schedule__form-education");

                for (Element form : allForms) {
                    boolean isZaoch = form.classNames().contains("schedule__form-education_zo");
                    boolean isOchZaoch = form.classNames().contains("schedule__form-education_vo");

                    Elements groupElements = form.select("a[href^=/schedule/]");

                    for (Element group : groupElements) {
                        String groupName = group.text().trim();
                        if (!groupName.isEmpty()) {
                            if (isZaoch) {
                                groupName += " (зо)";
                            } else if (isOchZaoch)
                                groupName += " (о-зо)";
                            groups.add(groupName);
                        }
                    }
                }
            } else {
                Log.w("Parser", "Факультет не найден: " + facultyName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(groups);
        return groups;
    }

    private String serializeList(List<String> list) {
        return String.join("|", list);
    }

    private List<String> deserializeList(String str) {
        if (str.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(java.util.Arrays.asList(str.split("\\|")));
    }
}
