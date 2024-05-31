package com.example.shedule;

import static com.example.shedule.R.*;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
public class MainActivity extends AppCompatActivity {

    private Spinner facultySpinner;
    private Spinner groupSpinner;
    private Spinner courseSpinner;
    private Button loadButton;
    private ArrayAdapter<String> scheduleAdapter;
    private TextView dayOfWeekText;
    private Button prevDayButton;
    private Button nextDayButton;
    private Button znamButton;
    private Button numButton;
    private Button loadSession;
    private Button backButton;
    private Button returnButton;
    private SwitchCompat switchLek;
    private SwitchCompat switchPr;
    private SwitchCompat switchLab;
    private int currentDayOfWeek = 1;
    private LinearLayout loadLayout;
    private LinearLayout scheduleLayout;
    private LinearLayout switchLayout;
    private LinearLayout loadsessionLayout;
    private TableLayout sessionTable;
    private LinearLayout sessionLayout;
    private TableLayout scheduleTable;
    private TextView[] lessons;
    private String[] daysOfWeek = {"", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"};
    private boolean firstParse = false;
    private List<ArrayList<String>> savedSchedules = new ArrayList<>();
    private List<String> savedSessionData = new ArrayList<>();
    private Document savedSessionDoc;
    private int originalColor;
    private int fadedColor;
    private Calendar calendar = Calendar.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        facultySpinner = findViewById(R.id.faculty_spinner);
        groupSpinner = findViewById(R.id.group_spinner);
        courseSpinner = findViewById(R.id.course_spinner);
        loadButton = findViewById(R.id.load_button);
        dayOfWeekText = findViewById(R.id.day_of_week_text);
        prevDayButton = findViewById(R.id.prev_day_button);
        nextDayButton = findViewById(R.id.next_day_button);
        znamButton = findViewById(R.id.znam_button);
        numButton = findViewById(R.id.num_button);
        loadSession = findViewById(id.load_session);
        switchLek = findViewById(R.id.switch_lek);
        switchPr = findViewById(R.id.switch_pr);
        switchLab = findViewById(R.id.switch_lab);
        loadLayout = findViewById(R.id.load_layout);
        scheduleLayout = findViewById(R.id.schedule_layout);
        scheduleTable = findViewById(R.id.schedule_table);
        switchLayout = findViewById(R.id.switch_layout);
        loadsessionLayout = findViewById(R.id.load_session_layout);
        sessionLayout = findViewById(id.session_layout);
        sessionTable = findViewById(R.id.session_table);
        backButton = findViewById(R.id.back_button);
        returnButton = findViewById(id.return_button);

        // Инициализировать массив lessons

        lessons = new TextView[8];
        lessons[0] = findViewById(R.id.lesson1);
        lessons[1] = findViewById(R.id.lesson2);
        lessons[2] = findViewById(R.id.lesson3);
        lessons[3] = findViewById(R.id.lesson4);
        lessons[4] = findViewById(R.id.lesson5);
        lessons[5] = findViewById(R.id.lesson6);
        lessons[6] = findViewById(R.id.lesson7);
        lessons[7] = findViewById(R.id.lesson8);
        for (int i = 0; i < 7; i++) {
            savedSchedules.add(new ArrayList<String>());
        }

        originalColor = Color.argb(255, 103, 80, 164);
        fadedColor = Color.argb(255, 124, 65, 250);

        final boolean[] isLessonVisibleZnam = {true};
        final boolean[] isLessonVisibleChis = {true};
        switchLek.setChecked(true);
        switchPr.setChecked(true);
        switchLab.setChecked(true);

        scheduleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());

        new ParseFacultiesThread().start();
        facultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String faculty = facultySpinner.getSelectedItem().toString();
                new ParseGroupsThread(faculty).start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Нет действий при сбросе выбора
            }
        });
        prevDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numButton.setBackgroundColor(originalColor);
                znamButton.setBackgroundColor(originalColor);
                switchLek.setChecked(true);
                switchPr.setChecked(true);
                switchLab.setChecked(true);
                currentDayOfWeek--;
                if (currentDayOfWeek < 1) {
                    currentDayOfWeek = 6;
                }
                // выводим расписание
                ScheduleGenerator();
            }
        });

        nextDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numButton.setBackgroundColor(originalColor);
                znamButton.setBackgroundColor(originalColor);
                switchLek.setChecked(true);
                switchPr.setChecked(true);
                switchLab.setChecked(true);
                currentDayOfWeek++;
                if (currentDayOfWeek > 6) {
                    currentDayOfWeek = 1;
                }
                // выводим расписание
                ScheduleGenerator();
            }
        });

        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // скрываем load_layout
                loadLayout.setVisibility(View.GONE);

                // делаем видимым schedule_layout, switch_layout и schedule_table
                scheduleLayout.setVisibility(View.VISIBLE);
                scheduleTable.setVisibility(View.VISIBLE);
                switchLayout.setVisibility(View.VISIBLE);
                loadsessionLayout.setVisibility(View.VISIBLE);
                numButton.setBackgroundColor(originalColor);
                znamButton.setBackgroundColor(originalColor);
                switchLek.setChecked(true);
                switchPr.setChecked(true);
                switchLab.setChecked(true);

                // выводим расписание
                ScheduleGenerator();
            }
        });
        znamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLessonVisibleZnam[0] = !isLessonVisibleZnam[0];
                for (int i = 0; i < lessons.length; i++) {
                    String savedLesson = savedSchedules.get(currentDayOfWeek - 1).get(i);
                    if (savedLesson.contains("знам.")) {
                        if (isLessonVisibleZnam[0] == false) {
                            znamButton.setBackgroundColor(fadedColor);
                            if(!savedLesson.contains("чис."))
                                lessons[i].setText("");
                            else {
                                if (!isLessonVisibleChis[0])
                                    lessons[i].setText("");
                                else {
                                    int indexOfZnam = savedLesson.indexOf("знам.");
                                    if (checkSwitches(savedLesson.substring(0, indexOfZnam)))
                                        lessons[i].setText(savedLesson.substring(0, indexOfZnam));
                                }
                            }
                        } else {
                            znamButton.setBackgroundColor(originalColor);
                            if (savedLesson.contains("чис.")) {
                                if (!isLessonVisibleChis[0]) {
                                    int indexOfZnam = savedLesson.indexOf("знам.");
                                    lessons[i].setText(savedLesson.substring(indexOfZnam));
                                } else
                                    lessons[i].setText(savedLesson);
                            }
                            else
                                lessons[i].setText(savedLesson);
                        }
                    }
                }
            }
        });


        numButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLessonVisibleChis[0] = !isLessonVisibleChis[0];

                for (int i = 0; i < lessons.length; i++) {
                    String savedLesson = savedSchedules.get(currentDayOfWeek - 1).get(i);
                    if (savedLesson.contains("чис.")) {
                        if (!isLessonVisibleChis[0]) {
                            numButton.setBackgroundColor(fadedColor);
                            if (!savedLesson.contains("знам."))
                                lessons[i].setText("");
                            else {
                                if (!isLessonVisibleZnam[0])
                                    lessons[i].setText("");
                                else {
                                    int indexOfZnam = savedLesson.indexOf("знам.");
                                    if (checkSwitches(savedLesson.substring(indexOfZnam)))
                                        lessons[i].setText(savedLesson.substring(indexOfZnam));
                                }
                            }
                        } else {
                            numButton.setBackgroundColor(originalColor);
                            if (savedLesson.contains("знам.")) {
                                if (!isLessonVisibleZnam[0]) {
                                    int indexOfZnam = savedLesson.indexOf("знам.");
                                    lessons[i].setText(savedLesson.substring(0, indexOfZnam));
                                } else
                                    lessons[i].setText(savedLesson);
                            }
                            else
                                lessons[i].setText(savedLesson);
                        }
                    }
                }
            }
        });


        switchLek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = switchLek.isChecked();
                for (int i = 0; i < lessons.length; i++) {
                    String savedLesson = savedSchedules.get(currentDayOfWeek - 1).get(i);
                    if (savedLesson.contains("лек.")) {
                        if (!isChecked) {
                            lessons[i].setText("");
                        } else {
                            if (!savedLesson.contains("знам.") && !savedLesson.contains("чис.") )
                                lessons[i].setText(savedLesson);
                            else {
                                if (isLessonVisibleZnam[0] && isLessonVisibleChis[0])
                                    lessons[i].setText(savedLesson);
                                else if (!isLessonVisibleZnam[0] && isLessonVisibleChis[0]) {
                                    int indexOfZnam = savedLesson.indexOf("знам.");
                                    lessons[i].setText(savedLesson.substring(0, indexOfZnam));
                                }
                                else if (isLessonVisibleZnam[0] && !isLessonVisibleChis[0]) {
                                    int indexOfZnam = savedLesson.indexOf("знам.");
                                    lessons[i].setText(savedLesson.substring(indexOfZnam));
                                }
                            }
                        }
                    }
                }
            }
        });

        switchPr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = switchPr.isChecked();
                for (int i = 0; i < lessons.length; i++) {
                    String savedLesson = savedSchedules.get(currentDayOfWeek - 1).get(i);
                    if (savedLesson.contains("пр.")) {
                        if (!isChecked) {
                            lessons[i].setText("");
                        } else {
                            if (!savedLesson.contains("знам.") && !savedLesson.contains("чис.") )
                                lessons[i].setText(savedLesson);
                            else {
                                if (isLessonVisibleZnam[0] && isLessonVisibleChis[0])
                                    lessons[i].setText(savedLesson);
                                else if (!isLessonVisibleZnam[0] && isLessonVisibleChis[0]) {
                                    int indexOfZnam = savedLesson.indexOf("знам.");
                                    lessons[i].setText(savedLesson.substring(0, indexOfZnam));
                                }
                                else if (isLessonVisibleZnam[0] && !isLessonVisibleChis[0]) {
                                    int indexOfZnam = savedLesson.indexOf("знам.");
                                    lessons[i].setText(savedLesson.substring(indexOfZnam));
                                }
                            }
                        }
                    }
                }
            }
        });


        switchLab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = switchLab.isChecked();
                for (int i = 0; i < lessons.length; i++) {
                    String savedLesson = savedSchedules.get(currentDayOfWeek - 1).get(i);
                    if (savedLesson.contains("лаб.")) {
                        if (!isChecked) {
                            lessons[i].setText("");
                        } else {
                            if (!savedLesson.contains("знам.") && !savedLesson.contains("чис.") )
                                lessons[i].setText(savedLesson);
                            else {
                                if (isLessonVisibleZnam[0] && isLessonVisibleChis[0])
                                    lessons[i].setText(savedLesson);
                                else if (!isLessonVisibleZnam[0] && isLessonVisibleChis[0]) {
                                    int indexOfZnam = savedLesson.indexOf("знам.");
                                    lessons[i].setText(savedLesson.substring(0, indexOfZnam));
                                }
                                else if (isLessonVisibleZnam[0] && !isLessonVisibleChis[0]) {
                                    int indexOfZnam = savedLesson.indexOf("знам.");
                                    lessons[i].setText(savedLesson.substring(indexOfZnam));
                                }
                            }
                        }
                    }
                }
            }
        });

        loadSession.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                new LoadSessionTask().execute();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayOfWeekText.setVisibility(View.VISIBLE);
                scheduleLayout.setVisibility(View.VISIBLE);
                scheduleTable.setVisibility(View.VISIBLE);
                switchLayout.setVisibility(View.VISIBLE);
                loadsessionLayout.setVisibility(View.VISIBLE);
                sessionLayout.setVisibility(View.GONE);
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savedSchedules.clear();
                for (int i = 0; i < 7; i++) {
                    savedSchedules.add(new ArrayList<String>());
                }
                savedSessionDoc = new Document("");
                savedSessionData.clear();
                firstParse = false;
                dayOfWeekText.setVisibility(View.GONE);
                scheduleLayout.setVisibility(View.GONE);
                scheduleTable.setVisibility(View.GONE);
                switchLayout.setVisibility(View.GONE);
                loadsessionLayout.setVisibility(View.GONE);
                loadLayout.setVisibility(View.VISIBLE);
            }
        });
    }
    private boolean checkSwitches(String savedLesson){
        if (savedLesson.contains("лек.") && switchLek.isChecked() || savedLesson.contains("пр.") && switchPr.isChecked() || savedLesson.contains("лаб.") && switchLab.isChecked())
            return true;
        else
            return false;
    }
    private class LoadSessionTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            // Выполняем сетевую операцию здесь
            String faculty = facultySpinner.getSelectedItem().toString();
            String group = groupSpinner.getSelectedItem().toString();
            String facultyUrl = facultySiteName(faculty);
            String sessionUrl = "https://www.old.sgu.ru/schedule/" + facultyUrl + "/do/" + group + "#session";
            Document sessionDoc = null;
            try {
                if (savedSessionDoc == null || savedSessionDoc.text().length() == 0)
                    sessionDoc = Jsoup.connect(sessionUrl).get();
                else
                    sessionDoc = savedSessionDoc;
                if ((savedSessionDoc == null || savedSessionDoc.text().length() == 0) && sessionDoc != null)
                    savedSessionDoc = sessionDoc;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            new ParseSessionThread(sessionDoc).start();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Обновляем пользовательский интерфейс здесь
            scheduleLayout.setVisibility(View.GONE);
            scheduleTable.setVisibility(View.GONE);
            switchLayout.setVisibility(View.GONE);
            loadsessionLayout.setVisibility(View.GONE);
            sessionLayout.setVisibility(View.VISIBLE);
        }
    }

    private void ScheduleGenerator(){
        String faculty = facultySpinner.getSelectedItem().toString();
        String group = groupSpinner.getSelectedItem().toString();
        String facultyUrl = facultySiteName(faculty);
        String scheduleUrl = "https://www.old.sgu.ru/schedule/" + facultyUrl + "/do/" + group;
        new ParseScheduleThread(scheduleUrl).start();
        dayOfWeekText.setVisibility(View.VISIBLE);
        prevDayButton.setVisibility(View.VISIBLE);
        nextDayButton.setVisibility(View.VISIBLE);
    }
    private List<String> parseFaculties() {
        List<String> faculties = new ArrayList<>();
        try {
            Document document = Jsoup.connect("https://www.old.sgu.ru/schedule").get();
            Element elements = document.select("#schedule_page > div > div.panes_item.panes_item__type_group > ul:nth-child(3)").first();
            for (Element li : elements.select("li")) {
                faculties.add(li.text());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return faculties;
    }

    private String facultySiteName(String facultyRuName){
        String facultyCorrectName;
        switch (facultyRuName){
            case "Биологический факультет":
                facultyCorrectName = "bf";
                break;
            case "Географический факультет":
                facultyCorrectName = "gf";
                break;
            case "Геологический факультет":
                facultyCorrectName = "gl";
                break;
            case "Институт искусств":
                facultyCorrectName = "ii";
                break;
            case "Институт истории и международных отношений":
                facultyCorrectName = "imo";
                break;
            case "Институт физики":
                facultyCorrectName = "ff";
                break;
            case "Институт физической культуры и спорта":
                facultyCorrectName = "ifk";
                break;
            case "Институт филологии и журналистики":
                facultyCorrectName = "ifg";
                break;
            case "Институт химии":
                facultyCorrectName = "ih";
                break;
            case "Институт дополнительного профессионального образования":
                facultyCorrectName = "idpo";
                break;
            case "Механико-математический факультет":
                facultyCorrectName = "mm";
                break;
            case "Социологический факультет":
                facultyCorrectName = "sf";
                break;
            case "Факультет иностранных языков и лингводидактики":
                facultyCorrectName = "fi";
                break;
            case "Факультет компьютерных наук и информационных технологий":
                facultyCorrectName = "knt";
                break;
            case "Факультет психологии":
                facultyCorrectName = "fps";
                break;
            case "Факультет психолого-педагогического и специального образования":
                facultyCorrectName = "fppso";
                break;
            case "Факультет фундаментальной медицины и медицинских технологий":
                facultyCorrectName = "fmimt";
                break;
            case "Философский факультет":
                facultyCorrectName = "fp";
                break;
            case "Экономический факультет":
                facultyCorrectName = "ef";
                break;
            case "Юридический факультет":
                facultyCorrectName = "uf";
                break;
            default:
                facultyCorrectName = "bf";
                break;
        }
        return facultyCorrectName;
    }

    private List<String> parseGroups(String facultyName) {
        List<String> groups = new ArrayList<>();
        try {
            String facultyUrl = "https://www.old.sgu.ru/schedule/" + facultySiteName(facultyName);
            Document document = Jsoup.connect(facultyUrl).get();
            Elements formElements = document.select("#schedule_page > fieldset.do.form_education.form-wrapper > div > fieldset, #schedule_page > fieldset.zo.form_education.form-wrapper > div > fieldset");
            for (Element formElement : formElements) {
                Elements courseElements = formElement.select("> div > fieldset");
                for (Element courseElement : courseElements) {
                    Elements groupElements = courseElement.select("> div > a");
                    for (int i = 0; i < groupElements.size(); i++) {
                        String groupName = groupElements.get(i).text();
                        if (formElement.cssSelector().contains(".zo")) {
                            groupName += " (заочная)";
                        }
                        groups.add(groupName);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(groups);
        return groups;
    }

    private ArrayList<String> parseSchedule(String scheduleUrl) {
        ArrayList<String> schedule = new ArrayList<>();
        try {
            Document document = Jsoup.connect(scheduleUrl).get();
            int currentDayOfWeekIndex;
            if (firstParse) {
                currentDayOfWeekIndex = currentDayOfWeek == 1 ? 0 : currentDayOfWeek - 1;
            } else {
                firstParse = true;
                currentDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) - 1 + 7) % 7;
                currentDayOfWeekIndex = (calendar.get(Calendar.DAY_OF_WEEK) - 1 + 7) % 7 == 1 ? 0 :
                        (calendar.get(Calendar.DAY_OF_WEEK) - 1 + 7) % 7 - 1;
            }
            if (currentDayOfWeekIndex == -1 || currentDayOfWeek == 0) {
                currentDayOfWeekIndex++;
                currentDayOfWeek++;
            }
            for (int i = 1; i <= 8; i++) { // Парсим все 8 пар
                String elementId = i + "_" + (currentDayOfWeekIndex + 1); // Формируем id элемента таблицы для нужной пары и нужного дня недели
                Element element = document.getElementById(elementId);
                if (element != null) {
                    schedule.add(element.text());
                } else {
                    schedule.add(""); // Добавляем пустую строку, если пара отсутствует
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return schedule;
    }

    private TableRow createSessionRow(String date, String time, String info) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        TextView dateTextView = new TextView(this);
        dateTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        dateTextView.setText(date);
        row.addView(dateTextView);

        TextView timeTextView = new TextView(this);
        timeTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        timeTextView.setText(time);
        row.addView(timeTextView);

        TextView infoTextView = new TextView(this);
        infoTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2));
        infoTextView.setText(info);
        row.addView(infoTextView);

        return row;
    }


    private List<String> parseSession(Document doc) {
        List<String> sessionData = new ArrayList<>();

        // Первая строка
        sessionData.add(doc.select("#session > tbody > tr:nth-child(1) > td:nth-child(1)").text());
        sessionData.add(doc.select("#session > tbody > tr:nth-child(1) > td:nth-child(2)").text());
        sessionData.add(doc.select("#session > tbody > tr:nth-child(1) > td:nth-child(3)").text() + "\n" +
                doc.select("#session > tbody > tr:nth-child(1) > td:nth-child(4)").text() + "\n" +
                doc.select("#session > tbody > tr:nth-child(2) > td:nth-child(1)").text() + "\n" +
                doc.select("#session > tbody > tr:nth-child(2) > td:nth-child(2)").text() + "\n" +
                doc.select("#session > tbody > tr:nth-child(3) > td:nth-child(1)").text() + "\n" +
                doc.select("#session > tbody > tr:nth-child(3) > td:nth-child(2)").text());

        // Последующие строки
        for (int i = 4; i <= doc.select("#session > tbody > tr").size(); i++) {
            sessionData.add(doc.select("#session > tbody > tr:nth-child(" + i + ") > td:nth-child(1)").text());
            sessionData.add(doc.select("#session > tbody > tr:nth-child(" + i + ") > td:nth-child(2)").text());
            sessionData.add(doc.select("#session > tbody > tr:nth-child(" + i + ") > td:nth-child(3)").text() + "\n" +
                    doc.select("#session > tbody > tr:nth-child(" + i + ") > td:nth-child(4)").text() + "\n" +
                    doc.select("#session > tbody > tr:nth-child(" + (i+1) + ") > td:nth-child(1)").text() + "\n" +
                    doc.select("#session > tbody > tr:nth-child(" + (i+1) + ") > td:nth-child(2)").text() + "\n" +
                    doc.select("#session > tbody > tr:nth-child(" + (i+2) + ") > td:nth-child(1)").text() + "\n" +
                    doc.select("#session > tbody > tr:nth-child(" + (i+2) + ") > td:nth-child(2)").text());
            i += 2; // пропускаем две следующие строки, которые уже обработаны
        }

        return sessionData;
    }



    private void createSessionRows(List<String> sessionData) {
        sessionTable.removeAllViews();

        for (int i = 0; i < sessionData.size(); i += 3) {
            TableRow row = createSessionRow(sessionData.get(i), sessionData.get(i + 1), sessionData.get(i + 2));
            sessionTable.addView(row);
        }

        sessionLayout.setVisibility(View.VISIBLE);
    }

    private class ParseFacultiesThread extends Thread {
        @Override
        public void run() {
            final List<String> faculties = parseFaculties();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, faculties);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    facultySpinner.setAdapter(adapter);
                }
            });
        }
    }

    private class ParseGroupsThread extends Thread {
        private String faculty;

        public ParseGroupsThread(String faculty) {
            this.faculty = faculty;
        }

        @Override
        public void run() {
            final List<String> groups = parseGroups(faculty);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    List<String> courses = new ArrayList<>();
                    char lastCourse = (groups.get(groups.size() - 1)).charAt(0);
                    for (int i = 0; i < Character.getNumericValue(lastCourse); i++){
                        int k = i + 1;
                        String numberOfCourse = String.valueOf(k);
                        String course = numberOfCourse + " курс";
                        courses.add(i, course);
                    }
                    ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(MainActivity.this,
                            android.R.layout.simple_spinner_item, courses);
                    courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    courseSpinner.setAdapter(courseAdapter);

                    courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            List<String> currentGroups = new ArrayList<>();
                            for (int i = 0; i < groups.size(); i++){
                                if (groups.get(i).charAt(0) == courseSpinner.getSelectedItem().toString().charAt(0)) {
                                    currentGroups.add(groups.get(i));
                                }
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                                    android.R.layout.simple_spinner_item, currentGroups);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            groupSpinner.setAdapter(adapter);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            // Нет действий при сбросе выбора
                        }
                    });
                }
            });
        }
    }

    private class ParseScheduleThread extends Thread {
        private String scheduleUrl;

        public ParseScheduleThread(String scheduleUrl) {
            this.scheduleUrl = scheduleUrl;
        }

        @Override
        public void run() {
            ArrayList<String> schedule = new ArrayList<>();
            if (savedSchedules.get(currentDayOfWeek - 1).isEmpty())
                schedule = parseSchedule(scheduleUrl);
            else
                schedule = savedSchedules.get(currentDayOfWeek - 1);

            ArrayList<String> finalSchedule = schedule;
            runOnUiThread(new Runnable() {
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
    }

    private class ParseSessionThread extends Thread {
        private Document sessionDoc;

        public ParseSessionThread(Document sessionDoc) {
            this.sessionDoc = sessionDoc;
        }
        @Override
        public void run() {
            List<String> sessionData = new ArrayList<>();
            if (savedSessionData.isEmpty())
                sessionData = parseSession(sessionDoc);
            else
                sessionData = savedSessionData;
            List<String> finalSessionData = sessionData;
            if (savedSessionData.isEmpty())
                savedSessionData = sessionData;
            runOnUiThread(() -> createSessionRows(finalSessionData));

        }
    }
}