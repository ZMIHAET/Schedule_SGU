package com.example.shedule.parser.teacher;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.shedule.activity.teacher.TeacherActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoadSessionTeacherThread extends Thread{
    private Document savedSessionDoc;
    private final TeacherActivity teacherActivity;
    private final TableLayout sessionTable;
    private final LinearLayout sessionLayout;
    private final String sessionUrl;
    private static final String PREFS_NAME = "session_prefs";
    private static final String KEY_SESSION_DATA = "session_teacher_data";
    private static final String KEY_SESSION_TIMESTAMP = "session_teacher_timestamp";
    private static final long CACHE_DURATION = 7L * 24 * 60 * 60 * 1000; // 7 дней


    public LoadSessionTeacherThread(TeacherActivity teacherActivity, TableLayout sessionTable, LinearLayout sessionLayout, String sessionUrl) {
        this.teacherActivity = teacherActivity;
        this.sessionTable = sessionTable;
        this.sessionLayout = sessionLayout;
        this.sessionUrl = sessionUrl;
    }


    @Override
    public void run() {
        String cachedData = teacherActivity.getSharedPreferences(PREFS_NAME, 0).getString(KEY_SESSION_DATA, null);
        long savedTime = teacherActivity.getSharedPreferences(PREFS_NAME, 0).getLong(KEY_SESSION_TIMESTAMP, 0);
        long now = System.currentTimeMillis();

        if (cachedData != null && now - savedTime < CACHE_DURATION) {
            List<String> sessionData = deserializeSessionData(cachedData);
            new Handler(Looper.getMainLooper()).post(() -> {
                createSessionRows(sessionData);
                teacherActivity.showSessionLayout();
            });
            return;
        }

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

        List<String> sessionData = parseSession(sessionDoc);

        saveSessionData(sessionData);

        new Handler(Looper.getMainLooper()).post(() -> {
            createSessionRows(sessionData);
            teacherActivity.showSessionLayout();
        });
    }

    private void saveSessionData(List<String> data) {
        String serialized = serializeSessionData(data);
        teacherActivity.getSharedPreferences(PREFS_NAME, 0).edit()
                .putString(KEY_SESSION_DATA, serialized)
                .putLong(KEY_SESSION_TIMESTAMP, System.currentTimeMillis())
                .apply();
    }

    private String serializeSessionData(List<String> data) {
        return android.text.TextUtils.join("||", data);
    }

    private List<String> deserializeSessionData(String serialized) {
        String[] parts = serialized.split("\\|\\|");
        List<String> list = new ArrayList<>();
        for (String s : parts) {
            list.add(s);
        }
        return list;
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

            // Извлекаем группу и подразделение
            String groupAndFac = cells.get(2).text().trim();

            // Извлекаем аудиторию
            String location = cells.get(3).text().trim();

            // Формируем строку с информацией о предмете
            String info = examType + ": " + subject + "\nГруппа/Подразделение: " + groupAndFac + "\nАудитория: " + location;

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
            TableRow row = new TableRow(teacherActivity);
            row.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
            ));

            TextView dateTextView = new TextView(teacherActivity);
            dateTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
            dateTextView.setText(sessionData.get(i));

            TextView timeTextView = new TextView(teacherActivity);
            timeTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
            timeTextView.setText(sessionData.get(i + 1));

            TextView infoTextView = new TextView(teacherActivity);
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
