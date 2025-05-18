package com.example.shedule.parser.teacher;

import android.app.Activity;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class ParseScheduleTeacherThread extends Thread {
    private final Activity activity;
    private final String scheduleUrl;
    private final String defaultUrl;
    private final List<ArrayList<String>> savedSchedules;
    private final int currentDayOfWeek;
    private final String[] daysOfWeek = {"", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"};
    private final TextView dayOfWeekText;
    private final TextView[] lessons;
    private final Calendar calendar = Calendar.getInstance();
    private final boolean isNumeratorWeek;

    public ParseScheduleTeacherThread(Activity activity, String scheduleUrl,
                                      String defaultUrl, List<ArrayList<String>> savedSchedules, int currentDayOfWeek,
                                      TextView dayOfWeekText, TextView[] lessons, boolean isNumeratorWeek) {
        this.activity = activity;
        this.scheduleUrl = scheduleUrl;
        this.defaultUrl = defaultUrl;
        this.savedSchedules = savedSchedules;
        this.currentDayOfWeek = currentDayOfWeek;
        this.dayOfWeekText = dayOfWeekText;
        this.lessons = lessons;
        this.isNumeratorWeek = isNumeratorWeek;
    }

    @Override
    public void run() {
        if (!Objects.equals(scheduleUrl, "https://www.sgu.runull")) {
            ArrayList<String> schedule;
            if (savedSchedules.get(currentDayOfWeek - 1).isEmpty() || !Objects.equals(scheduleUrl, defaultUrl)) {
                schedule = parseSchedule(scheduleUrl);
            } else
                schedule = savedSchedules.get(currentDayOfWeek - 1);

            ArrayList<String> finalSchedule = schedule;
            activity.runOnUiThread(() -> {
                dayOfWeekText.setText(daysOfWeek[currentDayOfWeek]);
                // Обновить текст в lessons
                for (int i = 0; i < lessons.length; i++) {
                    if (i < finalSchedule.size()) {
                        lessons[i].setText(HtmlCompat.fromHtml(finalSchedule.get(i), HtmlCompat.FROM_HTML_MODE_LEGACY));
                        lessons[i].setMovementMethod(LinkMovementMethod.getInstance());
                    } else {
                        lessons[i].setText("");
                    }
                }

                // Сохраняем список schedule после его первого получения
                if (savedSchedules.get(currentDayOfWeek - 1).isEmpty()) {
                    for (int i = 0; i < 8; i++) {
                        savedSchedules.get(currentDayOfWeek - 1).add(i, finalSchedule.get(i));
                    }
                }
            });
        }
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
                                ? lessonElement.selectFirst("div.schedule-table__lesson-props div").text() : "Не указан тип";
                        String subgr = lessonElement.selectFirst("div.schedule-table__lesson-uncertain") != null
                                ? lessonElement.selectFirst("div.schedule-table__lesson-uncertain").text() : "";
                        String lessonName = lessonElement.selectFirst("div.schedule-table__lesson-name").text();
                        String group = lessonElement.selectFirst("div.schedule-table__lesson-group span") != null
                                ? lessonElement.selectFirst("div.schedule-table__lesson-group span").text()
                                : "Не указана группа";
                        String room = lessonElement.selectFirst("div.schedule-table__lesson-room span").text();
                        String weekType = lessonElement.selectFirst("div.lesson-prop__num") != null ? "Ч" :
                                lessonElement.selectFirst("div.lesson-prop__denom") != null ? "З" : "";

                        String finalLesson = type + (subgr.isEmpty() ? "" : " (" + subgr + ")") +
                                ": " + lessonName + " (" + group + ", " + room + ")";

                        if (weekType.equals("Ч")) {
                            if (isNumeratorWeek) finalLesson = "<b>" + finalLesson + "</b>";
                            numeratorLesson = finalLesson;
                        } else if (weekType.equals("З")) {
                            if (!isNumeratorWeek) finalLesson = "<b>" + finalLesson + "</b>";
                            denominatorLesson = finalLesson;
                        } else {
                            selectedLesson = finalLesson;
                        }
                    }

                    if (!numeratorLesson.isEmpty() && denominatorLesson.isEmpty()) {
                        String label = isNumeratorWeek ? "<b>Ч: </b>" : "Ч: ";
                        selectedLesson = label + numeratorLesson;
                    }
                    else if (numeratorLesson.isEmpty() && !denominatorLesson.isEmpty()) {
                        String label = !isNumeratorWeek ? "<b>З: </b>" : "З: ";
                        selectedLesson = label + denominatorLesson;
                    }
                    else if (!numeratorLesson.isEmpty()) {
                        String labelNum = isNumeratorWeek ? "<b>Ч: </b>" : "Ч: ";
                        String labelDen = !isNumeratorWeek ? "<br><b>З: </b>" : "<br>З: ";
                        selectedLesson = labelNum + numeratorLesson + labelDen + denominatorLesson;
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
