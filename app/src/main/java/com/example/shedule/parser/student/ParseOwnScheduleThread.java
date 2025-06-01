package com.example.shedule.parser.student;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.shedule.DB.DatabaseHelper;
import com.example.shedule.activity.teacher.TeacherActivity;
import com.example.shedule.parser.teacher.teacherId.TeacherIdCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParseOwnScheduleThread extends Thread {
    private final Activity activity;
    private final String scheduleUrl;
    private final List<ArrayList<String>> savedSchedules;
    private final int currentDayOfWeek;
    private final TextView dayOfWeekText;
    private final String[] daysOfWeek = {"", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"};
    private final TextView[] lessons;
    private final boolean isNumeratorWeek;
    private final ArrayList<String> selectedSubgroups;

    public ParseOwnScheduleThread(Activity activity, String scheduleUrl,
                                  List<ArrayList<String>> savedSchedules, int currentDayOfWeek,
                                  TextView dayOfWeekText, TextView[] lessons,
                                  boolean isNumeratorWeek, ArrayList<String> selectedSubgroups) {
        this.activity = activity;
        this.scheduleUrl = scheduleUrl;
        this.savedSchedules = savedSchedules;
        this.currentDayOfWeek = currentDayOfWeek;
        this.dayOfWeekText = dayOfWeekText;
        this.lessons = lessons;
        this.isNumeratorWeek = isNumeratorWeek;
        this.selectedSubgroups = selectedSubgroups;
    }

    @Override
    public void run() {
        DatabaseHelper dbHelper = new DatabaseHelper(activity);
/*                    SQLiteDatabase db1 = dbHelper.getWritableDatabase();
            dbHelper.clearScheduleCache(db1);
            db1.close();*/
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String cachedJson = dbHelper.getCachedSchedule(db, scheduleUrl, currentDayOfWeek);
        ArrayList<SpannableStringBuilder> schedule;

        if (cachedJson != null) {
            schedule = deserializeSchedule(cachedJson);
        } else {
            schedule = parseSchedule(scheduleUrl);
            String jsonToSave = serializeSchedule(schedule);
            dbHelper.saveScheduleToCache(db, scheduleUrl, currentDayOfWeek, jsonToSave);
        }

        savedSchedules.set(currentDayOfWeek - 1, new ArrayList<>());
        for (SpannableStringBuilder s : schedule) {
            savedSchedules.get(currentDayOfWeek - 1).add(s.toString());
        }

        db.close();

        activity.runOnUiThread(() -> {
            SpannableStringBuilder header = new SpannableStringBuilder(
                    daysOfWeek[currentDayOfWeek] + " (" + (isNumeratorWeek ? "числ" : "знам") + ")");
            header.setSpan(new StyleSpan(Typeface.BOLD), header.length() - 1, header.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            header.setSpan(new ForegroundColorSpan(Color.parseColor("#800080")),
                    header.length() - 5, header.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            dayOfWeekText.setText(header);

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
        Log.d("scheduleUrl", scheduleUrl);
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
                        // Пропуск, если есть подгруппа, но она не выбрана
                        if (!subgr.isEmpty() && !selectedSubgroups.contains(subgr)) {
                            continue;
                        }

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

                        // Определение числителя / знаменателя
                        boolean isNumerator = lessonElement.selectFirst("div.lesson-prop__num") != null;
                        boolean isDenominator = lessonElement.selectFirst("div.lesson-prop__denom") != null;

                        if (isNumerator) {
                            numeratorBuilder.append(fullLesson);
                        } else if (isDenominator) {
                            denominatorBuilder.append(fullLesson);
                        } else {
                            selectedLessonBuilder.append(fullLesson);
                        }
                    }

                    SpannableStringBuilder finalBuilder = new SpannableStringBuilder();
                    if (numeratorBuilder.length() > 0 || denominatorBuilder.length() > 0) {
                        if (numeratorBuilder.length() > 0) {
                            finalBuilder.append("Ч: ");
                            finalBuilder.append(numeratorBuilder);
                        }
                        if (denominatorBuilder.length() > 0) {
                            finalBuilder.append("З: ");
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

    private String serializeSchedule(ArrayList<SpannableStringBuilder> list) {
        JSONArray array = new JSONArray();
        for (SpannableStringBuilder builder : list) {
            array.put(builder.toString());
        }
        return array.toString();
    }

    private ArrayList<SpannableStringBuilder> deserializeSchedule(String json) {
        ArrayList<SpannableStringBuilder> result = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                String text = array.getString(i);
                SpannableStringBuilder builder = new SpannableStringBuilder(text);
                for (String name : TeacherIdCache.getAllTeachers()) {
                    int start = text.indexOf(name);
                    if (start != -1) {
                        int end = start + name.length();
                        String id = TeacherIdCache.getTeacherId(name);
                        if (id != null) {
                            builder.setSpan(new ClickableSpan() {
                                @Override
                                public void onClick(View widget) {
                                    Intent intent = new Intent(activity, TeacherActivity.class);
                                    intent.putExtra("teacherUrl", "https://www.sgu.ru/schedule/teacher/" + id);
                                    activity.startActivity(intent);
                                }
                            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }
                result.add(builder);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getTextSafe(Element parent, String selector, String fallback) {
        Element el = parent.selectFirst(selector);
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
