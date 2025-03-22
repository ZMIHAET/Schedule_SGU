package com.example.shedule.activity.student;

import static com.example.shedule.R.*;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.shedule.R;
import com.example.shedule.parser.student.FacultySiteName;
import com.example.shedule.parser.student.LoadSessionThread;
import com.example.shedule.parser.student.ParseFacultiesThread;
import com.example.shedule.parser.student.ParseGroupsThread;
import com.example.shedule.parser.student.ParseScheduleStudentThread;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity {

    private Button loadButton, prevDayButton, nextDayButton,
            znamButton, numButton, loadSession, backButton, returnButton;
    private Spinner facultySpinner, groupSpinner, courseSpinner;
    private TextView dayOfWeekText;
    private SwitchCompat switchLek, switchPr, switchLab;
    private int currentDayOfWeek = 1, fadedColor;
    private LinearLayout loadLayout, scheduleLayout, switchLayout,
            loadSessionLayout, sessionLayout;
    private TableLayout sessionTable, scheduleTable;
    private TextView[] lessons;
    private List<ArrayList<String>> savedSchedules = new ArrayList<>();
    private List<String> savedSessionData = new ArrayList<>();
    private Document savedSessionDoc;
    private FacultySiteName facultySiteName;

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
        loadSessionLayout = findViewById(R.id.load_session_layout);
        sessionLayout = findViewById(id.session_layout);
        sessionTable = findViewById(R.id.session_table);
        backButton = findViewById(R.id.back_button);
        returnButton = findViewById(id.return_button);
        facultySiteName = new FacultySiteName();


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
            savedSchedules.add(new ArrayList<>());
        }



        //int originalColor = Color.argb(255, 103, 80, 164);
        fadedColor = Color.argb(255, 105, 104, 104);

        final boolean[] isLessonVisibleZnam = {true};
        final boolean[] isLessonVisibleChis = {true};
        switchLek.setChecked(true);
        switchPr.setChecked(true);
        switchLab.setChecked(true);

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

        loadButton.setOnClickListener(v -> {

            // скрываем load_layout
            loadLayout.setVisibility(View.GONE);

            // делаем видимым schedule_layout, switch_layout и schedule_table
            scheduleLayout.setVisibility(View.VISIBLE);
            scheduleTable.setVisibility(View.VISIBLE);
            switchLayout.setVisibility(View.VISIBLE);
            loadSessionLayout.setVisibility(View.VISIBLE);
            numButton.setBackgroundResource(android.R.drawable.btn_default);
            znamButton.setBackgroundResource(android.R.drawable.btn_default);
            switchLek.setChecked(true);
            switchPr.setChecked(true);
            switchLab.setChecked(true);

            // Генерируем расписание
            ScheduleGenerator();

        });

        prevDayButton.setOnClickListener(v -> {
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
        });

        nextDayButton.setOnClickListener(v -> {
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
        });


        znamButton.setOnClickListener(v -> {
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
        });


        numButton.setOnClickListener(v -> {
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
        });


        switchLek.setOnClickListener(v -> {
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
        });

        switchPr.setOnClickListener(v -> {
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
        });


        switchLab.setOnClickListener(v -> {
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
        });

        loadSession.setOnClickListener(v ->
                new LoadSessionThread(facultySpinner, groupSpinner, savedSessionDoc, MainActivity.this, sessionTable, sessionLayout).start()
        );


        backButton.setOnClickListener(v -> {
            dayOfWeekText.setVisibility(View.VISIBLE);
            scheduleLayout.setVisibility(View.VISIBLE);
            scheduleTable.setVisibility(View.VISIBLE);
            switchLayout.setVisibility(View.VISIBLE);
            loadSessionLayout.setVisibility(View.VISIBLE);
            sessionLayout.setVisibility(View.GONE);
        });

        returnButton.setOnClickListener(v -> {
            savedSchedules.clear();
            for (int i = 0; i < 7; i++) {
                savedSchedules.add(new ArrayList<>());
            }
            savedSessionDoc = new Document("");
            savedSessionData.clear();
            dayOfWeekText.setVisibility(View.GONE);
            scheduleLayout.setVisibility(View.GONE);
            scheduleTable.setVisibility(View.GONE);
            switchLayout.setVisibility(View.GONE);
            loadSessionLayout.setVisibility(View.GONE);
            loadLayout.setVisibility(View.VISIBLE);
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
        new ParseScheduleStudentThread(MainActivity.this, scheduleUrl,
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
        loadSessionLayout.setVisibility(View.GONE);
        sessionLayout.setVisibility(View.VISIBLE);
    }
}