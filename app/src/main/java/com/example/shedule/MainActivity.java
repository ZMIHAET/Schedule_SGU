package com.example.shedule;

import static com.example.shedule.R.*;

import android.graphics.Color;
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

import com.example.shedule.parser.ParseFacultiesThread;
import com.example.shedule.parser.ParseGroupsThread;
import com.example.shedule.parser.ParseScheduleThread;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity {

    private Button btnShowSchedule;
    private Button btnLoadSchedule;
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
    private LinearLayout buttonsLayout;
    private LinearLayout loadLayout;
    private LinearLayout scheduleLayout;
    private LinearLayout switchLayout;
    private LinearLayout loadsessionLayout;
    private TableLayout sessionTable;
    private LinearLayout sessionLayout;
    private TableLayout scheduleTable;
    private TextView[] lessons;
    private boolean firstParse = false;
    private List<ArrayList<String>> savedSchedules = new ArrayList<>();
    private List<String> savedSessionData = new ArrayList<>();
    private Document savedSessionDoc;
    private int originalColor;
    private int fadedColor;
    private FacultySiteName facultySiteName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonsLayout = findViewById(R.id.buttons_layout);
        btnShowSchedule = findViewById(R.id.btn_show_schedule);
        btnLoadSchedule = findViewById(R.id.btn_load_schedule);
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
        facultySiteName = new FacultySiteName();

        btnLoadSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Кнопка была нажата");
                buttonsLayout.setVisibility(View.GONE);
                loadLayout.setVisibility(View.VISIBLE);
            }
        });

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


        btnShowSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonsLayout.setVisibility(View.INVISIBLE);
                // делаем видимым schedule_layout, switch_layout и schedule_table
                scheduleLayout.setVisibility(View.VISIBLE);
                scheduleTable.setVisibility(View.VISIBLE);
                dayOfWeekText.setVisibility(View.VISIBLE);
                switchLayout.setVisibility(View.VISIBLE);
                loadsessionLayout.setVisibility(View.VISIBLE);
                numButton.setBackgroundResource(android.R.drawable.btn_default);
                znamButton.setBackgroundResource(android.R.drawable.btn_default);
                switchLek.setChecked(true);
                switchPr.setChecked(true);
                switchLab.setChecked(true);
                Log.d("MainActivity", "Расписание показано!");


            }
        });

        originalColor = Color.argb(255, 103, 80, 164);
        fadedColor = Color.argb(255, 105, 104, 104);

        final boolean[] isLessonVisibleZnam = {true};
        final boolean[] isLessonVisibleChis = {true};
        switchLek.setChecked(true);
        switchPr.setChecked(true);
        switchLab.setChecked(true);

        scheduleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());

        new ParseFacultiesThread(this, facultySpinner).start();


        facultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String faculty = facultySpinner.getSelectedItem().toString();
                new ParseGroupsThread(MainActivity.this, faculty, courseSpinner, groupSpinner).start();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Нет действий при сбросе выбора
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
                numButton.setBackgroundResource(android.R.drawable.btn_default);
                znamButton.setBackgroundResource(android.R.drawable.btn_default);
                switchLek.setChecked(true);
                switchPr.setChecked(true);
                switchLab.setChecked(true);

                // Генерируем расписание
                ScheduleGenerator();

            }
        });

        prevDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numButton.setBackgroundResource(android.R.drawable.btn_default);
                znamButton.setBackgroundResource(android.R.drawable.btn_default);
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
                numButton.setBackgroundResource(android.R.drawable.btn_default);
                znamButton.setBackgroundResource(android.R.drawable.btn_default);
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


        znamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLessonVisibleZnam[0] = !isLessonVisibleZnam[0];
                for (int i = 0; i < lessons.length; i++) {
                    String savedLesson = savedSchedules.get(currentDayOfWeek - 1).get(i);
                    if (savedLesson.contains("З:")) {
                        if (isLessonVisibleZnam[0] == false) {
                            znamButton.setBackgroundColor(fadedColor);
                            if(!savedLesson.contains("Ч:"))
                                lessons[i].setText("");
                            else {
                                if (!isLessonVisibleChis[0])
                                    lessons[i].setText("");
                                else {
                                    int indexOfZnam = savedLesson.indexOf("З:");
                                    if (checkSwitches(savedLesson.substring(0, indexOfZnam)))
                                        lessons[i].setText(savedLesson.substring(0, indexOfZnam));
                                }
                            }
                        } else {
                            znamButton.setBackgroundResource(android.R.drawable.btn_default);
                            if (savedLesson.contains("Ч:")) {
                                if (!isLessonVisibleChis[0]) {
                                    int indexOfZnam = savedLesson.indexOf("З:");
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
                    if (savedLesson.contains("Ч:")) {
                        if (!isLessonVisibleChis[0]) {
                            numButton.setBackgroundColor(fadedColor);
                            if (!savedLesson.contains("З:"))
                                lessons[i].setText("");
                            else {
                                if (!isLessonVisibleZnam[0])
                                    lessons[i].setText("");
                                else {
                                    int indexOfZnam = savedLesson.indexOf("З:");
                                    if (checkSwitches(savedLesson.substring(indexOfZnam)))
                                        lessons[i].setText(savedLesson.substring(indexOfZnam));
                                }
                            }
                        } else {
                            numButton.setBackgroundResource(android.R.drawable.btn_default);
                            if (savedLesson.contains("З:")) {
                                if (!isLessonVisibleZnam[0]) {
                                    int indexOfZnam = savedLesson.indexOf("З:");
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
                    if (savedLesson.contains("ЛЕКЦИЯ")) {
                        if (!isChecked) {
                            lessons[i].setText("");
                        } else {
                            if (!savedLesson.contains("З:") && !savedLesson.contains("Ч:") )
                                lessons[i].setText(savedLesson);
                            else {
                                if (isLessonVisibleZnam[0] && isLessonVisibleChis[0])
                                    lessons[i].setText(savedLesson);
                                else if (!isLessonVisibleZnam[0] && isLessonVisibleChis[0]) {
                                    int indexOfZnam = savedLesson.indexOf("З:");
                                    lessons[i].setText(savedLesson.substring(0, indexOfZnam));
                                }
                                else if (isLessonVisibleZnam[0] && !isLessonVisibleChis[0]) {
                                    int indexOfZnam = savedLesson.indexOf("З:");
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
                    if (savedLesson.contains("ПРАКТИКА")) {
                        if (!isChecked) {
                            lessons[i].setText("");
                        } else {
                            if (!savedLesson.contains("З:") && !savedLesson.contains("Ч:") )
                                lessons[i].setText(savedLesson);
                            else {
                                if (isLessonVisibleZnam[0] && isLessonVisibleChis[0])
                                    lessons[i].setText(savedLesson);
                                else if (!isLessonVisibleZnam[0] && isLessonVisibleChis[0]) {
                                    int indexOfZnam = savedLesson.indexOf("З:");
                                    lessons[i].setText(savedLesson.substring(0, indexOfZnam));
                                }
                                else if (isLessonVisibleZnam[0] && !isLessonVisibleChis[0]) {
                                    int indexOfZnam = savedLesson.indexOf("З:");
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
                    if (savedLesson.contains("ЛАБОРАТОРНАЯ")) {
                        if (!isChecked) {
                            lessons[i].setText("");
                        } else {
                            if (!savedLesson.contains("З:") && !savedLesson.contains("Ч:") )
                                lessons[i].setText(savedLesson);
                            else {
                                if (isLessonVisibleZnam[0] && isLessonVisibleChis[0])
                                    lessons[i].setText(savedLesson);
                                else if (!isLessonVisibleZnam[0] && isLessonVisibleChis[0]) {
                                    int indexOfZnam = savedLesson.indexOf("З:");
                                    lessons[i].setText(savedLesson.substring(0, indexOfZnam));
                                }
                                else if (isLessonVisibleZnam[0] && !isLessonVisibleChis[0]) {
                                    int indexOfZnam = savedLesson.indexOf("З:");
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
                new LoadSessionTask(facultySpinner, groupSpinner, savedSessionDoc, MainActivity.this).execute();
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
        return savedLesson.contains("ЛЕКЦИЯ") && switchLek.isChecked() || savedLesson.contains("ПРАКТИКА") && switchPr.isChecked() || savedLesson.contains("ЛАБОРАТОРНАЯ") && switchLab.isChecked();
    }

    private void ScheduleGenerator(){
        String faculty = facultySpinner.getSelectedItem().toString();
        String group = groupSpinner.getSelectedItem().toString();
        Log.d("faculty", faculty);
        String facultyUrl = facultySiteName.showFacultyName(faculty);
        String scheduleUrl = "https://www.sgu.ru/schedule/" + facultyUrl + "/do/" + group;
        new ParseScheduleThread(MainActivity.this, scheduleUrl,
                savedSchedules, currentDayOfWeek, dayOfWeekText,
                lessons).start();
        dayOfWeekText.setVisibility(View.VISIBLE);
        prevDayButton.setVisibility(View.VISIBLE);
        nextDayButton.setVisibility(View.VISIBLE);
    }

    public void showSessionLayout() {
        scheduleLayout.setVisibility(View.GONE);
        scheduleTable.setVisibility(View.GONE);
        switchLayout.setVisibility(View.GONE);
        loadsessionLayout.setVisibility(View.GONE);
        sessionLayout.setVisibility(View.VISIBLE);
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

    public class ParseSessionThread extends Thread {
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