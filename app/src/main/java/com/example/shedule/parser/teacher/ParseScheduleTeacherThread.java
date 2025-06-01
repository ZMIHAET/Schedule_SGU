package com.example.shedule.parser.teacher;

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
import com.example.shedule.activity.student.MainActivity;
import com.example.shedule.parser.student.FacultySiteName;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final boolean isNumeratorWeek;

    public ParseScheduleTeacherThread(Activity activity, String scheduleUrl,
                                      String defaultUrl, List<ArrayList<String>> savedSchedules,
                                      int currentDayOfWeek, TextView dayOfWeekText, TextView[] lessons,
                                      boolean isNumeratorWeek) {
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
            DatabaseHelper dbHelper = new DatabaseHelper(activity);
/*            SQLiteDatabase db1 = dbHelper.getWritableDatabase();
            dbHelper.clearScheduleCache(db1);
            db1.close();*/
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String cachedData = dbHelper.getCachedSchedule(db, scheduleUrl, currentDayOfWeek);
            if (cachedData != null) {
                activity.runOnUiThread(() -> applyScheduleFromCache(cachedData));
                return;
            }

            ArrayList<SpannableStringBuilder> schedule = parseSchedule(scheduleUrl);

            savedSchedules.set(currentDayOfWeek - 1, new ArrayList<>());
            StringBuilder stringBuilder = new StringBuilder();
            for (SpannableStringBuilder s : schedule) {
                savedSchedules.get(currentDayOfWeek - 1).add(s.toString());
                stringBuilder.append(s.toString()).append("\n⏎⏎\n");
            }

            SQLiteDatabase writableDb = dbHelper.getWritableDatabase();
            dbHelper.saveScheduleToCache(writableDb, scheduleUrl, currentDayOfWeek, stringBuilder.toString());

            activity.runOnUiThread(() -> {
                SpannableStringBuilder builder = new SpannableStringBuilder(daysOfWeek[currentDayOfWeek] + " (");
                String weekLetter = isNumeratorWeek ? "числ" : "знам";
                builder.append(weekLetter).append(")");
                int start = builder.length() - 5;
                int end = builder.length() - 1;
                builder.setSpan(new StyleSpan(Typeface.BOLD), end, end + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new ForegroundColorSpan(Color.parseColor("#800080")), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                dayOfWeekText.setText(builder);

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
    }

    private void applyScheduleFromCache(String data) {
        String[] lessonsArray = data.split("\n⏎⏎\n");

        if (savedSchedules.get(currentDayOfWeek - 1).isEmpty()) {
            for (String raw : lessonsArray) {
                savedSchedules.get(currentDayOfWeek - 1).add(raw);
            }
        }

        for (int i = 0; i < lessons.length; i++) {
            if (i < lessonsArray.length) {
                SpannableStringBuilder spannable = new SpannableStringBuilder(lessonsArray[i]);
                String fullText = spannable.toString();

                // Ищем все возможные скобки и обрабатываем только те, что содержат "гр."
                int searchIndex = 0;
                while (searchIndex < fullText.length()) {
                    int open = fullText.indexOf("(", searchIndex);
                    if (open == -1) break;
                    int close = fullText.indexOf(")", open);
                    if (close == -1) break;

                    String inside = fullText.substring(open + 1, close);
                    if (inside.contains("гр.")) {
                        String[] parts = inside.split("гр\\.");
                        if (parts.length == 2) {
                            String groupNumber = parts[0].trim();
                            String facultyShort = parts[1].trim().split(",")[0].replace(" ", "");
                            Log.d("facultyShort", facultyShort);
                            String facultyCode = new FacultySiteName().showFacultyName(facultyShort);
                            String groupUrl = "https://www.sgu.ru/schedule/" + facultyCode + "/do/" + groupNumber;

                            final int spanStart = open + 1;
                            final int spanEnd = close;

                            spannable.setSpan(new ClickableSpan() {
                                @Override
                                public void onClick(View widget) {
                                    Intent intent = new Intent(activity, MainActivity.class);
                                    intent.putExtra("groupUrl", groupUrl);
                                    activity.startActivity(intent);
                                }
                            }, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }

                    searchIndex = close + 1;
                }

                lessons[i].setText(spannable);
                lessons[i].setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                lessons[i].setText("");
            }
        }

        // Обновление дня недели (с жирной Ч/З)
        SpannableStringBuilder builder = new SpannableStringBuilder(daysOfWeek[currentDayOfWeek] + " (");
        String weekLetter = isNumeratorWeek ? "числ" : "знам";
        builder.append(weekLetter).append(")");
        int start = builder.length() - 5;
        int end = builder.length() - 1;
        builder.setSpan(new StyleSpan(Typeface.BOLD), end, end + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new ForegroundColorSpan(Color.parseColor("#800080")), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        dayOfWeekText.setText(builder);
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

                    SpannableStringBuilder selectedLesson = new SpannableStringBuilder();
                    SpannableStringBuilder numeratorLesson = new SpannableStringBuilder();
                    SpannableStringBuilder denominatorLesson = new SpannableStringBuilder();

                    for (Element lessonElement : lessonElements) {
                        String type = getSafeText(lessonElement, "div.schedule-table__lesson-props div", "Не указан тип");
                        String subgr = getSafeText(lessonElement, "div.schedule-table__lesson-uncertain", "");
                        String lessonName = getSafeText(lessonElement, "div.schedule-table__lesson-name", "Без названия");
                        String group = getSafeText(lessonElement, "div.schedule-table__lesson-group span", "Не указана группа");
                        String room = getSafeText(lessonElement, "div.schedule-table__lesson-room span", "—");
                        String weekType = lessonElement.selectFirst("div.lesson-prop__num") != null ? "Ч" :
                                lessonElement.selectFirst("div.lesson-prop__denom") != null ? "З" : "";

                        SpannableStringBuilder lessonBuilder = new SpannableStringBuilder();
                        lessonBuilder.append(type);
                        if (!subgr.isEmpty()) lessonBuilder.append(" (").append(subgr).append(")");
                        lessonBuilder.append(": ").append(lessonName).append(" (");

                        if (group.contains("гр.")) {
                            String[] parts = group.split("гр\\.");
                            String groupNumber = parts[0].trim();
                            String facultyShort = parts.length > 1 ? parts[1].trim() : "";
                            String facultyCode = new FacultySiteName().showFacultyName(facultyShort);

                            String displayGroup = group.trim();
                            String groupUrl = "https://www.sgu.ru/schedule/" + facultyCode + "/do/" + groupNumber;

                            SpannableStringBuilder clickableGroup = new SpannableStringBuilder(displayGroup);
                            clickableGroup.setSpan(new ClickableSpan() {
                                @Override
                                public void onClick(View widget) {
                                    Intent intent = new Intent(activity, MainActivity.class);
                                    intent.putExtra("groupUrl", groupUrl);
                                    activity.startActivity(intent);
                                }
                            }, 0, displayGroup.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                            lessonBuilder.append(clickableGroup);
                        } else {
                            lessonBuilder.append(group);
                        }

                        lessonBuilder.append(", ").append(room).append(")\n");

                        switch (weekType) {
                            case "Ч":
                                numeratorLesson.append(lessonBuilder);
                                break;
                            case "З":
                                denominatorLesson.append(lessonBuilder);
                                break;
                            default:
                                selectedLesson.append(lessonBuilder);
                                break;
                        }
                    }

                    SpannableStringBuilder finalBuilder = new SpannableStringBuilder();
                    if (numeratorLesson.length() > 0) {
                        finalBuilder.append("Ч: ").append(numeratorLesson);
                    }
                    if (denominatorLesson.length() > 0) {
                        finalBuilder.append("З: ").append(denominatorLesson);
                    }
                    if (finalBuilder.length() == 0) {
                        finalBuilder = selectedLesson;
                    }

                    schedule.add(finalBuilder);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return schedule;
    }

    private String getSafeText(Element parent, String selector, String fallback) {
        Element el = parent.selectFirst(selector);
        return el != null ? el.text() : fallback;
    }
}
