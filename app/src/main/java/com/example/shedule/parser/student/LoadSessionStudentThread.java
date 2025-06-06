package com.example.shedule.parser.student;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
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
    private final String sessionUrl;
    private Document savedSessionDoc;
    private final MainActivity mainActivity;
    private final TableLayout sessionTable;
    private final LinearLayout sessionLayout;
    private static final String PREFS_NAME = "session_prefs";
    private static final long CACHE_DURATION = 7L * 24 * 60 * 60 * 1000; // 7 дней

    public LoadSessionStudentThread(String sessionUrl, Document savedSessionDoc,
                                    MainActivity mainActivity, TableLayout sessionTable, LinearLayout sessionLayout) {
        this.sessionUrl = sessionUrl;
        this.savedSessionDoc = savedSessionDoc;
        this.mainActivity = mainActivity;
        this.sessionTable = sessionTable;
        this.sessionLayout = sessionLayout;
    }

    private String getCacheKeyData() {
        return "session_student_data_" + sessionUrl.hashCode();
    }

    private String getCacheKeyTimestamp() {
        return "session_student_timestamp_" + sessionUrl.hashCode();
    }

    @Override
    public void run() {
        String cachedData = mainActivity.getSharedPreferences(PREFS_NAME, 0).getString(getCacheKeyData(), null);
        long savedTime = mainActivity.getSharedPreferences(PREFS_NAME, 0).getLong(getCacheKeyTimestamp(), 0);
        long now = System.currentTimeMillis();

        if (cachedData != null && now - savedTime < CACHE_DURATION) {
            List<String> sessionData = deserializeSessionData(cachedData);
            updateUI(sessionData);
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
        Log.d("sessionData", sessionData.toString());

        saveSessionData(sessionData);
        updateUI(sessionData);
    }

    private void updateUI(List<String> sessionData) {
        new Handler(Looper.getMainLooper()).post(() -> {
            sessionLayout.setVisibility(View.VISIBLE);
            if (!sessionData.contains("Ошибка: не найден блок 'Расписание сессии'")) {
                createSessionRows(sessionData);
            }
            mainActivity.showSessionLayout();
        });
    }

    private void saveSessionData(List<String> data) {
        String serialized = serializeSessionData(data);
        mainActivity.getSharedPreferences(PREFS_NAME, 0).edit()
                .putString(getCacheKeyData(), serialized)
                .putLong(getCacheKeyTimestamp(), System.currentTimeMillis())
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

        Element sessionTable;
        if (sessionUrl.contains("/zo/"))
            sessionTable = doc.selectFirst(".schedule__wrap-lection table");
        else
            sessionTable = doc.selectFirst(".schedule__wrap-session table");

        if (sessionTable == null) {
            sessionData.add("Ошибка: не найден блок 'Расписание сессии'");
            return sessionData;
        }

        Elements rows = sessionTable.select("tbody tr");

        for (Element row : rows) {
            Elements cells = row.select("td");

            if (cells.size() < 4) continue;

            String[] dateTimeParts = cells.get(0).text().trim().split(" ");
            String date, time;
            if (sessionUrl.contains("/zo/")) {
                date = dateTimeParts[0] + " " + dateTimeParts[1] + "\n" + dateTimeParts[2];
                time = dateTimeParts[3];
            } else {
                date = dateTimeParts[0] + " " + dateTimeParts[1] + "\n" + dateTimeParts[2] + " " + dateTimeParts[3];
                time = dateTimeParts[4];
            }

            String examType = cells.get(1).selectFirst(".schedule-form") != null ? cells.get(1).selectFirst(".schedule-form").text() : "";
            String subject = cells.get(1).selectFirst(".schedule-discipline") != null ? cells.get(1).selectFirst(".schedule-discipline").text() : "";

            String teacher = cells.get(2).text().trim();
            String location = cells.get(3).text().trim();

            String info = examType + ": " + subject + "\nПреподаватель: " + teacher + "\nАудитория: " + location;

            sessionData.add(date);
            sessionData.add(time);
            sessionData.add(info);
        }

        return sessionData;
    }

    private void createSessionRows(List<String> sessionData) {
        sessionTable.removeAllViews();

        int closestIndex = -1;
        long minDiff = Long.MAX_VALUE;

        for (int i = 0; i < sessionData.size(); i += 3) {
            try {
                String dateTimeStr = sessionData.get(i);
                String[] parts = dateTimeStr.split("\n");
                String yearRaw = parts[1];
                String year = yearRaw.replaceAll("\\D", "");
                String datePart = parts[0] + ' ' + year;
                String timePart = sessionData.get(i + 1);

                java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("dd MMMM yyyy HH:mm", java.util.Locale.forLanguageTag("ru"));
                java.util.Date parsedDate = format.parse(datePart + " " + timePart);

                long diff = parsedDate.getTime() - System.currentTimeMillis();
                if (diff > 0 && diff < minDiff) {
                    minDiff = diff;
                    closestIndex = i;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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
    }
}
