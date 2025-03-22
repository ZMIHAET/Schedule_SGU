package com.example.shedule.parser.student;

import android.app.Activity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParseFacultiesThread extends Thread {
    private final Activity activity;
    private final Spinner facultySpinner;
    public ParseFacultiesThread(Activity activity, Spinner facultySpinner) {
        this.activity = activity;
        this.facultySpinner = facultySpinner;
    }

    @Override
    public void run() {
        final List<String> faculties = parseFaculties();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, faculties);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                facultySpinner.setAdapter(adapter);
            }
        });
    }

    private List<String> parseFaculties() {
        List<String> faculties = new ArrayList<>();
        try {
            // Загружаем HTML-страницу
            Document document = Jsoup.connect("https://www.sgu.ru/schedule").userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36").get();

            // Ищем все заголовки факультетов
            Elements facultyElements = document.select("div.accordion-container h3.accordion__header");

            // Добавляем текст заголовков в список
            for (Element faculty : facultyElements) {
                faculties.add(faculty.text().trim());
            }
            Log.d("Parser", faculties.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return faculties;
    }
}

