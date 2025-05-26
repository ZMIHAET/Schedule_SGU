package com.example.shedule.parser.student;

import android.app.Activity;
import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.shedule.activity.teacher.TeacherActivity;
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
    private final boolean isNumeratorWeek;

    public ParseScheduleStudentThread(Activity activity, String scheduleUrl,
                                      List<ArrayList<String>> savedSchedules, int currentDayOfWeek,
                                      TextView dayOfWeekText, TextView[] lessons, boolean isNumeratorWeek) {
        this.activity = activity;
        this.scheduleUrl = scheduleUrl;
        this.savedSchedules = savedSchedules;
        this.currentDayOfWeek = currentDayOfWeek;
        this.dayOfWeekText = dayOfWeekText;
        this.lessons = lessons;
        this.isNumeratorWeek = isNumeratorWeek;
    }

    @Override
    public void run() {
        ArrayList<SpannableStringBuilder> schedule = parseSchedule(scheduleUrl);

        // Сохраняем расписание в savedSchedules
        savedSchedules.set(currentDayOfWeek - 1, new ArrayList<>());
        for (SpannableStringBuilder s : schedule) {
            savedSchedules.get(currentDayOfWeek - 1).add(s.toString()); // сохраняем как строку
        }

        activity.runOnUiThread(() -> {
            dayOfWeekText.setText(daysOfWeek[currentDayOfWeek]);
            for (int i = 0; i < lessons.length; i++) {
                if (i < schedule.size()) {
                    lessons[i].setText(schedule.get(i));
                    lessons[i].setMovementMethod(LinkMovementMethod.getInstance());
                } else {
                    lessons[i].setText("");
                }
            }
        });
    }


    private ArrayList<SpannableStringBuilder> parseSchedule(String scheduleUrl) {
        ArrayList<SpannableStringBuilder> schedule = new ArrayList<>();
        try {
            Document document = Jsoup.connect(scheduleUrl).get();
            Elements rows = document.select("tbody tr");
            int dayIndex = currentDayOfWeek - 1;

            for (Element row : rows) {
                Elements cols = row.select("td.schedule-table__col");
                if (dayIndex < cols.size()) {
                    Element lessonCell = cols.get(dayIndex);
                    Elements lessonElements = lessonCell.select("div.schedule-table__lesson");

                    SpannableStringBuilder selectedLessonBuilder = new SpannableStringBuilder();
                    SpannableStringBuilder numeratorBuilder = new SpannableStringBuilder();
                    SpannableStringBuilder denominatorBuilder = new SpannableStringBuilder();

                    for (Element lessonElement : lessonElements) {
                        String type = getTextSafe(lessonElement, "div.schedule-table__lesson-props div", "Не указан тип");
                        String subgr = getTextSafe(lessonElement, "div.schedule-table__lesson-uncertain", "");
                        String lessonName = getTextSafe(lessonElement, "div.schedule-table__lesson-name", "Без названия");
                        String teacherRaw = getTextSafe(lessonElement, "div.schedule-table__lesson-teacher span, div.schedule-table__lesson-teacher a", "Не указан преподаватель");
                        String room = getTextSafe(lessonElement, "div.schedule-table__lesson-room span", "—");

                        Spanned teacherSpan = makeTeacherSpannable(teacherRaw);

                        String prefix = type + (subgr.isEmpty() ? "" : " (" + subgr + ")") +
                                ": " + lessonName + " (";
                        String suffix = ", " + room + ")\n";

                        SpannableStringBuilder fullLesson = new SpannableStringBuilder();
                        fullLesson.append(prefix);
                        fullLesson.append(teacherSpan);
                        fullLesson.append(suffix);

                        String weekType = lessonElement.selectFirst("div.lesson-prop__num") != null ? "Ч" :
                                lessonElement.selectFirst("div.lesson-prop__denom") != null ? "З" : "";

                        switch (weekType) {
                            case "Ч":
                                if (isNumeratorWeek) fullLesson.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, fullLesson.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                numeratorBuilder.append(fullLesson);
                                break;
                            case "З":
                                if (!isNumeratorWeek) fullLesson.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, fullLesson.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                denominatorBuilder.append(fullLesson);
                                break;
                            default:
                                selectedLessonBuilder.append(fullLesson);
                        }
                    }

                    SpannableStringBuilder finalBuilder = new SpannableStringBuilder();
                    if (numeratorBuilder.length() > 0 || denominatorBuilder.length() > 0) {
                        int end = 0;
                        if (numeratorBuilder.length() > 0) {
                            finalBuilder.append("Ч: ");
                            if (isNumeratorWeek) {
                                finalBuilder.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                                        0, finalBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                            finalBuilder.append(numeratorBuilder);
                            end = finalBuilder.length();
                        }
                        if (denominatorBuilder.length() > 0) {
                            finalBuilder.append("З: " );
                            if (!isNumeratorWeek)
                                finalBuilder.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                                        end, finalBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            finalBuilder.append(denominatorBuilder);
                        }
                    } else {
                        finalBuilder = selectedLessonBuilder;
                    }

                    schedule.add(finalBuilder);
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

    private Spanned makeTeacherSpannable(String teacherName) {
        String id = TeacherIdCache.getTeacherId(teacherName);
        if (id == null) return new SpannableStringBuilder(teacherName);

        SpannableStringBuilder spannable = new SpannableStringBuilder(teacherName);
        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(activity, TeacherActivity.class);
                intent.putExtra("teacherUrl", "https://www.sgu.ru/schedule/teacher/" + id);
                activity.startActivity(intent);
            }
        }, 0, teacherName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannable;
    }
}
