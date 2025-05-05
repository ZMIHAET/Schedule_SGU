package com.example.shedule.parser.student;

import android.app.Activity;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import com.example.shedule.parser.teacher.teacherId.TeacherIdCache;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ParseScheduleStudentThread extends Thread {
    private final Activity activity;
    private final String scheduleUrl;
    private final List<ArrayList<String>> savedSchedules;
    private final int currentDayOfWeek;
    private final String[] daysOfWeek = {"", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"};
    private final TextView dayOfWeekText;
    private final TextView[] lessons;
    private final Calendar calendar = Calendar.getInstance();

    public ParseScheduleStudentThread(Activity activity, String scheduleUrl,
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
        activity.runOnUiThread(() -> {
            dayOfWeekText.setText(daysOfWeek[currentDayOfWeek]);
            // Обновить текст в lessons
            for (int i = 0; i < lessons.length; i++) {
                if (i < finalSchedule.size()) {
                    String htmlText = finalSchedule.get(i);
                    lessons[i].setText(HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY));
                    lessons[i].setMovementMethod(LinkMovementMethod.getInstance());
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
        });
    }

    private ArrayList<String> parseSchedule(String scheduleUrl) {
        ArrayList<String> schedule = new ArrayList<>();
        try {
            Document document = Jsoup.connect(scheduleUrl).get();
            Elements rows = document.select("tbody tr");
            int dayIndex = currentDayOfWeek - 1;

            for (Element row : rows) {
                Elements cols = row.select("td.schedule-table__col");
                if (dayIndex < cols.size()) {
                    Element lessonCell = cols.get(dayIndex);
                    Elements lessonElements = lessonCell.select("div.schedule-table__lesson");

                    String selectedLesson = "";
                    String numeratorLesson = "", denominatorLesson = "";

                    for (Element lessonElement : lessonElements) {
                        String type = getTextSafe(lessonElement, "div.schedule-table__lesson-props div", "Не указан тип");
                        String lessonName = getTextSafe(lessonElement, "div.schedule-table__lesson-name", "Без названия");
                        String teacherRaw = getTextSafe(lessonElement, "div.schedule-table__lesson-teacher span, div.schedule-table__lesson-teacher a", "Не указан преподаватель");
                        String room = getTextSafe(lessonElement, "div.schedule-table__lesson-room span", "—");

                        // Преобразуем teacherRaw к формату Фамилия И.О.
                        String teacherLinked = formatTeacherAsLink(teacherRaw);

                        String weekType = lessonElement.selectFirst("div.lesson-prop__num") != null ? "Ч" :
                                lessonElement.selectFirst("div.lesson-prop__denom") != null ? "З" : "";

                        String lessonText = type + ": " + lessonName + " (" + teacherLinked + ", " + room + ")";

                        if (weekType.equals("Ч")) {
                            numeratorLesson = lessonText;
                        } else if (weekType.equals("З")) {
                            denominatorLesson = lessonText;
                        } else {
                            selectedLesson = lessonText;
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

    private String getTextSafe(Element root, String selector, String fallback) {
        Element el = root.selectFirst(selector);
        return el != null ? el.text() : fallback;
    }

    private String formatTeacherAsLink(String teacherName) {
        if (teacherName.equals("Не указан преподаватель")) return teacherName;

        // Преобразуем Фамилия И.О. → ссылку, если ID есть
        String id = TeacherIdCache.getTeacherId(teacherName);
        if (id != null) {
            return "<a href=\"https://www.sgu.ru/schedule/teacher/" + id + "\">" + teacherName + "</a>";
        } else {
            return teacherName; // без ссылки, если ID не найден
        }
    }



}