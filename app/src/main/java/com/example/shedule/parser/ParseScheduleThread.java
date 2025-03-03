package com.example.shedule.parser;

import android.app.Activity;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ParseScheduleThread extends Thread {
    private final Activity activity;
    private final String scheduleUrl;
    private final List<ArrayList<String>> savedSchedules;
    private int currentDayOfWeek;
    private final String[] daysOfWeek = {"", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"};
    private final TextView dayOfWeekText;
    private final TextView[] lessons;
    private final Calendar calendar = Calendar.getInstance();

    public ParseScheduleThread(Activity activity, String scheduleUrl,
                               List<ArrayList<String>> savedSchedules, int currentDayOfWeek,
                               TextView dayOfWeekText, TextView[] lessons) {
        this.activity = activity;
        this.scheduleUrl = scheduleUrl;
        this.savedSchedules = savedSchedules;
        this.currentDayOfWeek = currentDayOfWeek;
        this.dayOfWeekText = dayOfWeekText;
        this.lessons = lessons;
    }

    @Override
    public void run() {
        ArrayList<String> schedule;
        if (savedSchedules.get(currentDayOfWeek - 1).isEmpty())
            schedule = parseSchedule(scheduleUrl);
        else
            schedule = savedSchedules.get(currentDayOfWeek - 1);

        ArrayList<String> finalSchedule = schedule;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dayOfWeekText.setText(daysOfWeek[currentDayOfWeek]);
                // Обновить текст в lessons
                for (int i = 0; i < lessons.length; i++) {
                    if (i < finalSchedule.size()) {
                        lessons[i].setText(finalSchedule.get(i));
                    } else {
                        lessons[i].setText("");
                    }
                }
                // Сохраняем список schedule после его первого получения
                if (savedSchedules.get(currentDayOfWeek - 1).isEmpty()) {
                    for (int i = 0; i < 8; i++){
                        savedSchedules.get(currentDayOfWeek - 1).add(i, finalSchedule.get(i));
                    }
                }
            }
        });
    }

    private ArrayList<String> parseSchedule(String scheduleUrl) {
        ArrayList<String> schedule = new ArrayList<>();
        try {
            Document document = Jsoup.connect(scheduleUrl).get(); // Загружаем HTML-страницу с сайта
            Elements rows = document.select("tbody tr"); // Выбираем строки с парами

            int dayIndex = currentDayOfWeek - 1; // Индекс текущего дня недели в таблице

            for (Element row : rows) {
                Elements cols = row.select("td.schedule-table__col"); // Получаем ячейки
                if (dayIndex < cols.size()) {
                    Element lessonCell = cols.get(dayIndex);
                    Elements lessonElements = lessonCell.select("div.schedule-table__lesson");

                    String selectedLesson = "";
                    String numeratorLesson = "", denominatorLesson = "";

                    for (Element lessonElement : lessonElements) {
                        String type = lessonElement.selectFirst("div.schedule-table__lesson-props div") != null
                                ? lessonElement.selectFirst("div.schedule-table__lesson-props div").text() : "Не указано";
                        String lessonName = lessonElement.selectFirst("div.schedule-table__lesson-name").text();
                        String teacher = lessonElement.selectFirst("div.schedule-table__lesson-teacher span, div.schedule-table__lesson-teacher a").text();
                        String room = lessonElement.selectFirst("div.schedule-table__lesson-room span").text();
                        String weekType = lessonElement.selectFirst("div.lesson-prop__num") != null ? "Ч" :
                                lessonElement.selectFirst("div.lesson-prop__denom") != null ? "З" : "";

                        if (weekType.equals("Ч")) {
                            numeratorLesson = type + ": " + lessonName + " (" + teacher + ", " + room + ")";
                        } else if (weekType.equals("З")) {
                            denominatorLesson = type + ": " + lessonName + " (" + teacher + ", " + room + ")";
                        } else {
                            selectedLesson = type + ": " + lessonName + " (" + teacher + ", " + room + ")";
                        }
                    }

                    if (!numeratorLesson.isEmpty() || !denominatorLesson.isEmpty()) {
                        selectedLesson = "Ч: " + numeratorLesson + "\nЗ: " + denominatorLesson;
                    }

                    schedule.add(selectedLesson);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return schedule;
    }

}

