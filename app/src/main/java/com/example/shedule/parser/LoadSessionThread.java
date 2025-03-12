package com.example.shedule.parser;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.shedule.FacultySiteName;
import com.example.shedule.MainActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoadSessionThread extends Thread {
    private final Spinner facultySpinner;
    private final Spinner groupSpinner;
    private Document savedSessionDoc;
    private final MainActivity mainActivity;
    private final FacultySiteName facultySiteName;
    private final TableLayout sessionTable;
    private final LinearLayout sessionLayout;

    public LoadSessionThread(Spinner facultySpinner, Spinner groupSpinner, Document savedSessionDoc,
                             MainActivity mainActivity, TableLayout sessionTable, LinearLayout sessionLayout) {
        this.facultySpinner = facultySpinner;
        this.groupSpinner = groupSpinner;
        this.savedSessionDoc = savedSessionDoc;
        this.mainActivity = mainActivity;
        this.facultySiteName = new FacultySiteName();
        this.sessionTable = sessionTable;
        this.sessionLayout = sessionLayout;
    }


    @Override
    public void run() {
        String faculty = facultySpinner.getSelectedItem().toString();
        String group = groupSpinner.getSelectedItem().toString();
        String facultyUrl = facultySiteName.showFacultyName(faculty);
        String sessionUrl = "https://www.sgu.ru/schedule/" + facultyUrl + "/do/" + group + "#session";

        Document sessionDoc;
        try {
            if (savedSessionDoc == null || savedSessionDoc.text().isEmpty())
                sessionDoc = Jsoup.connect(sessionUrl).get();
            else
                sessionDoc = savedSessionDoc;

            if ((savedSessionDoc == null || savedSessionDoc.text().isEmpty()) && sessionDoc != null)
                savedSessionDoc = sessionDoc;

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Парсим данные
        List<String> sessionData = parseSession(sessionDoc);

        // Обновляем UI в главном потоке
        new Handler(Looper.getMainLooper()).post(() -> {
            createSessionRows(sessionData);
            mainActivity.showSessionLayout();
        });
    }

    private List<String> parseSession(Document doc) {
        List<String> sessionData = new ArrayList<>();

        // Находим секцию "Расписание сессии"
        Element sessionTable = doc.selectFirst(".schedule__wrap-session table");

        if (sessionTable == null) {
            sessionData.add("Ошибка: не найден блок 'Расписание сессии'");
            return sessionData;
        }

        Elements rows = sessionTable.select("tbody tr");

        for (Element row : rows) {
            Elements cells = row.select("td");

            if (cells.size() < 4) continue; // Пропускаем пустые строки

            // Извлекаем дату и время
            String[] dateTimeParts = cells.get(0).text().trim().split(" ");
            String date = dateTimeParts[0] + " " + dateTimeParts[1] + "\n" + dateTimeParts[2] + " " + dateTimeParts[3];
            String time = dateTimeParts[4];

            // Извлекаем тип экзамена и дисциплину
            String examType = cells.get(1).selectFirst(".schedule-form") != null ? cells.get(1).selectFirst(".schedule-form").text() : "";
            String subject = cells.get(1).selectFirst(".schedule-discipline") != null ? cells.get(1).selectFirst(".schedule-discipline").text() : "";

            // Извлекаем преподавателя
            String teacher = cells.get(2).text().trim();

            // Извлекаем аудиторию
            String location = cells.get(3).text().trim();

            // Формируем строку с информацией о предмете
            String info = examType + ": " + subject + "\nПреподаватель: " + teacher + "\nАудитория: " + location;

            // Добавляем в список
            sessionData.add(date);
            sessionData.add(time);
            sessionData.add(info);
        }

        return sessionData;
    }

    private TableRow createSessionRow(String date, String time, String info) {
        TableRow row = new TableRow(mainActivity);
        row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        TextView dateTextView = new TextView(mainActivity);
        dateTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        dateTextView.setText(date);
        row.addView(dateTextView);

        TextView timeTextView = new TextView(mainActivity);
        timeTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        timeTextView.setText(time);
        row.addView(timeTextView);

        TextView infoTextView = new TextView(mainActivity);
        infoTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2));
        infoTextView.setText(info);
        row.addView(infoTextView);

        return row;
    }

    private void createSessionRows(List<String> sessionData) {
        sessionTable.removeAllViews();

        for (int i = 0; i < sessionData.size(); i += 3) {
            TableRow row = createSessionRow(sessionData.get(i), sessionData.get(i + 1), sessionData.get(i + 2));
            sessionTable.addView(row);
        }

        sessionLayout.setVisibility(View.VISIBLE);
    }

}
