package com.example.shedule.activity.teacher;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.shedule.R;
import com.example.shedule.parser.student.ParseScheduleStudentThread;
import com.example.shedule.parser.teacher.ParseScheduleTeacherThread;
import com.example.shedule.parser.teacher.TeacherParserThread;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

public class TeacherActivity extends AppCompatActivity {

    private Spinner facSpinner, teacherSpinner;
    private LinearLayout loadLayout, scheduleLayout, switchLayout,
    loadSessionLayout;
    private TableLayout sessionTable, scheduleTable;
    private Button loadButton, prevDayButton, nextDayButton,
            znamButton, numButton, loadSession, returnButton;
    private SwitchCompat switchLek, switchPr, switchLab;
    private TextView dayOfWeekText;
    private int currentDayOfWeek = 1, fadedColor;

    private TextView[] lessons;
    private List<ArrayList<String>> savedSchedules = new ArrayList<>();
    private List<String> savedSessionData = new ArrayList<>();

    private Document savedSessionDoc;


    /*
    private List<String> facultyList = new ArrayList<>();
    private List<String> teacherList = new ArrayList<>();
*/
    private String baseUrl = "https://www.sgu.ru";
    private TeacherParserThread teacherParserThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);



        loadLayout = findViewById(R.id.load_layout);
        scheduleLayout = findViewById(R.id.schedule_layout);
        switchLayout = findViewById(R.id.switch_layout);
        loadSessionLayout = findViewById(R.id.load_session_layout);
        scheduleTable = findViewById(R.id.schedule_table);
        facSpinner = findViewById(R.id.fac_spinner);
        teacherSpinner = findViewById(R.id.teacher_spinner);
        loadButton = findViewById(R.id.load_teacher_button);

        znamButton = findViewById(R.id.znam_button);
        numButton = findViewById(R.id.num_button);

        prevDayButton = findViewById(R.id.prev_day_button);
        nextDayButton = findViewById(R.id.next_day_button);
        returnButton = findViewById(R.id.return_button);
        loadSession = findViewById(R.id.load_session);
        dayOfWeekText = findViewById(R.id.day_of_week_text);


        switchLab = findViewById(R.id.switch_lab);
        switchLek = findViewById(R.id.switch_lek);
        switchPr = findViewById(R.id.switch_pr);


        // Инициализация массива lessons
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


        fadedColor = Color.argb(255, 105, 104, 104);

        final boolean[] isLessonVisibleZnam = {true};
        final boolean[] isLessonVisibleChis = {true};
        switchLek.setChecked(true);
        switchPr.setChecked(true);
        switchLab.setChecked(true);

        // Загружаем данные в новом потоке
        new Thread(() -> {
            teacherParserThread = new TeacherParserThread(this, baseUrl + "/schedule", facSpinner, teacherSpinner);
            teacherParserThread.start(); // Запускаем в том же потоке
            runOnUiThread(() -> loadButton.setEnabled(true)); // Включаем кнопку после загрузки
        }).start();

        loadButton.setOnClickListener(v -> {
            loadLayout.setVisibility(View.GONE);

            scheduleLayout.setVisibility(View.VISIBLE);
            scheduleTable.setVisibility(View.VISIBLE);
            switchLayout.setVisibility(View.VISIBLE);
            loadSessionLayout.setVisibility(View.VISIBLE);
            numButton.setBackgroundResource(android.R.drawable.btn_default);
            znamButton.setBackgroundResource(android.R.drawable.btn_default);
            switchLek.setChecked(true);
            switchPr.setChecked(true);
            switchLab.setChecked(true);


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
        String teacher = teacherSpinner.getSelectedItem().toString();
        String Url = baseUrl + teacherParserThread.getTeacherHref(teacher);


        new ParseScheduleTeacherThread(TeacherActivity.this, Url,
                savedSchedules, currentDayOfWeek, dayOfWeekText,
                lessons).start();
        dayOfWeekText.setVisibility(View.VISIBLE);
        prevDayButton.setVisibility(View.VISIBLE);
        nextDayButton.setVisibility(View.VISIBLE);
    }
}
