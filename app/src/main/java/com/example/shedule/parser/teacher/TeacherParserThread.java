package com.example.shedule.parser.teacher;

import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TeacherParserThread extends Thread {
    private final Activity activity;
    private final String baseUrl;
    private final Spinner facSpinner, teacherSpinner;
    private List<String> facultyList = new ArrayList<>();
    private HashMap<String, List<String>> facultyTeachersMap = new HashMap<>();

    public TeacherParserThread(Activity activity, String baseUrl, Spinner facSpinner, Spinner teacherSpinner) {
        this.activity = activity;
        this.baseUrl = baseUrl;
        this.facSpinner = facSpinner;
        this.teacherSpinner = teacherSpinner;
    }

    @Override
    public void run() {
        facultyList = parseFaculties();
        activity.runOnUiThread(() -> {
            if (facultyList.isEmpty()) {
                Toast.makeText(activity, "Ошибка загрузки факультетов", Toast.LENGTH_SHORT).show();
            } else {
                ArrayAdapter<String> facAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, facultyList);
                facSpinner.setAdapter(facAdapter);

                facSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                        String selectedFaculty = facultyList.get(position);
                        List<String> teachers = facultyTeachersMap.getOrDefault(selectedFaculty, new ArrayList<>());
                        activity.runOnUiThread(() -> {
                            if (teachers.isEmpty()) {
                                Toast.makeText(activity, "Преподаватели не найдены", Toast.LENGTH_SHORT).show();
                            } else {
                                Collections.sort(teachers);
                                ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, teachers);
                                teacherSpinner.setAdapter(teacherAdapter);
                            }
                        });
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    }
                });
            }
        });
    }

    private List<String> parseFaculties() {
        List<String> faculties = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(baseUrl).get();
            Elements facultyElements = doc.select("select#select-education option");

            for (Element faculty : facultyElements) {
                String facultyName = faculty.text();
                if (!facultyName.equals("Выберите подразделение")) {
                    faculties.add(facultyName);
                }
            }
            parseTeachers(); // Загружаем преподавателей после получения факультетов
        } catch (IOException e) {
            e.printStackTrace();
        }
        return faculties;
    }

    private void parseTeachers() {
        try {
            Document doc = Jsoup.connect(baseUrl).get();
            Elements teacherElements = doc.select("li.schedule__fio_item");

            for (Element teacherElement : teacherElements) {
                Element teacherNameElement = teacherElement.selectFirst("a.schedule__fio_item-link");
                Element facultyNameElement = teacherElement.selectFirst("a.schedule__faculty_item-link");

                if (teacherNameElement != null && facultyNameElement != null) {
                    String teacherName = teacherNameElement.text();
                    String facultyName = facultyNameElement.text();
                    facultyTeachersMap.computeIfAbsent(facultyName, k -> new ArrayList<>()).add(teacherName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
