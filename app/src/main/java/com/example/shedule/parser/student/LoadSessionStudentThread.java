package com.example.shedule.parser.student;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.shedule.activity.student.MainActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoadSessionStudentThread extends Thread {
    private String sessionUrl;
    private Document savedSessionDoc;
    private final MainActivity mainActivity;
    private final FacultySiteName facultySiteName;
    private final TableLayout sessionTable;
    private final LinearLayout sessionLayout;

    public LoadSessionStudentThread(String sessionUrl, Document savedSessionDoc,
                                    MainActivity mainActivity, TableLayout sessionTable, LinearLayout sessionLayout) {
        this.sessionUrl = sessionUrl;
        this.savedSessionDoc = savedSessionDoc;
        this.mainActivity = mainActivity;
        this.facultySiteName = new FacultySiteName();
        this.sessionTable = sessionTable;
        this.sessionLayout = sessionLayout;
    }


    @Override
    public void run() {

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

    private void createSessionRows(List<String> sessionData) {
        sessionTable.removeAllViews();

        // Поиск ближайшего экзамена
        int closestIndex = -1;
        long minDiff = Long.MAX_VALUE;

        for (int i = 0; i < sessionData.size(); i += 3) {
            try {
                String dateTimeStr = sessionData.get(i); // дата + \n + время
                String[] parts = dateTimeStr.split("\n");
                String yearRaw = parts[1]; // "2025 г."
                String year = yearRaw.replaceAll("\\D", ""); // "2025"
                String datePart = parts[0] + ' ' + year;// пример: "24 мая 2025"
                String timePart = sessionData.get(i + 1); // пример: "10:00"

                java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("dd MMMM yyyy HH:mm", java.util.Locale.forLanguageTag("ru"));
                java.util.Date parsedDate = format.parse(datePart + " " + timePart);

                long diff = parsedDate.getTime() - System.currentTimeMillis();
                if (diff > 0 && diff < minDiff) {
                    minDiff = diff;
                    closestIndex = i;
                }
            } catch (Exception e) {
                e.printStackTrace(); // на случай ошибки разбора даты
            }
        }

        // Создание строк таблицы
        for (int i = 0; i < sessionData.size(); i += 3) {
            TableRow row = new TableRow(mainActivity);
            row.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
            ));

            TextView dateTextView = new TextView(mainActivity);
            dateTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
            dateTextView.setText(sessionData.get(i));

            TextView timeTextView = new TextView(mainActivity);
            timeTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
            timeTextView.setText(sessionData.get(i + 1));

            TextView infoTextView = new TextView(mainActivity);
            infoTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2));
            infoTextView.setText(sessionData.get(i + 2));

            // Если это ближайший экзамен — выделяем жирным
            if (i == closestIndex) {
                dateTextView.setTypeface(null, android.graphics.Typeface.BOLD);
                timeTextView.setTypeface(null, android.graphics.Typeface.BOLD);
                infoTextView.setTypeface(null, android.graphics.Typeface.BOLD);
            }

            row.addView(dateTextView);
            row.addView(timeTextView);
            row.addView(infoTextView);

            sessionTable.addView(row);
        }

        sessionLayout.setVisibility(View.VISIBLE);
    }


}
