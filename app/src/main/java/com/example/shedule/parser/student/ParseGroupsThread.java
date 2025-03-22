package com.example.shedule.parser.student;

import android.app.Activity;
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

    public ParseGroupsThread(Activity activity, String faculty, Spinner courseSpinner, Spinner groupSpinner) {
        this.activity = activity;
        this.faculty = faculty;
        this.courseSpinner = courseSpinner;
        this.groupSpinner = groupSpinner;
    }

    @Override
    public void run() {
        final List<String> groups = parseGroups(faculty);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<String> courses = new ArrayList<>();
                char lastCourse = (groups.get(groups.size() - 1)).charAt(0);
                int courseQuantity = 2;
                while (!Character.isDigit(lastCourse)) {
                    lastCourse = (groups.get(groups.size() - courseQuantity)).charAt(0);
                    courseQuantity++;
                }
                for (int i = 0; i < Character.getNumericValue(lastCourse); i++){
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
                        for (int i = 0; i < groups.size(); i++){
                            if (groups.get(i).charAt(0) == courseSpinner.getSelectedItem().toString().charAt(0)) {
                                currentGroups.add(groups.get(i));
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                                android.R.layout.simple_spinner_item, currentGroups);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        groupSpinner.setAdapter(adapter);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Нет действий при сбросе выбора
                    }
                });
            }
        });
    }
    private List<String> parseGroups(String facultyName) {
        Log.d("FacultyName", facultyName);

        List<String> groups = new ArrayList<>();
        try {
            // Загружаем HTML-страницу
            Document document = Jsoup.connect("https://www.sgu.ru/schedule").userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36").get();

            // Ищем контейнер с нужным факультетом
            Elements facultyElements = document.select("div.accordion-container:has(h3:contains(" + facultyName + "))");

            if (!facultyElements.isEmpty()) {
                Element facultyElement = facultyElements.first(); // Берем первый найденный элемент

                // Ищем все группы внутри этого факультета
                Elements groupElements = facultyElement.select("li.schedule-number__item a");

                for (Element group : groupElements) {
                    groups.add(group.text().trim());
                }
            } else {
                System.out.println("Факультет не найден: " + facultyName);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Сортируем группы перед возвратом
        Collections.sort(groups);
        return groups;
    }
}
