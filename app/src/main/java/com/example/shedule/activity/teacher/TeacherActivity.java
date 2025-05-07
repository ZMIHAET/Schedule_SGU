package com.example.shedule.activity.teacher;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.text.HtmlCompat;

import com.example.shedule.R;
import com.example.shedule.activity.auth.LoginActivity;
import com.example.shedule.parser.teacher.LoadSessionTeacherThread;
import com.example.shedule.parser.teacher.ParseScheduleTeacherThread;
import com.example.shedule.parser.teacher.TeacherParserThread;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TeacherActivity extends AppCompatActivity {

    private Spinner facSpinner, teacherSpinner;
    private LinearLayout loadLayout, scheduleLayout, switchLayout,
    loadSessionLayout, sessionLayout;
    private TableLayout sessionTable, scheduleTable;
    private Button loadButton, loadOwnButton, prevDayButton, nextDayButton,
            znamButton, numButton, loadSession, returnButton, backButton, loadTeachersButton;
    private SwitchCompat switchLek, switchPr, switchLab;
    private TextView dayOfWeekText;
    private int currentDayOfWeek = 1, fadedColor;

    private boolean isOwnSchedule = false;

    private TextView[] lessons;
    private List<ArrayList<String>> savedSchedules = new ArrayList<>();
    private List<String> savedSessionData = new ArrayList<>();

    private Document savedSessionDoc;
    boolean isNumeratorWeek;



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

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        currentDayOfWeek = (day == Calendar.SUNDAY) ? 1 : day - 1;
        Log.d("currentDayOfWeek", String.valueOf(currentDayOfWeek));

        // Определяем тип текущей недели: числитель (Ч) или знаменатель (З)
        int weekNumber = calendar.get(Calendar.WEEK_OF_YEAR);
        isNumeratorWeek = (weekNumber % 2 == 1); // нечетные недели — числитель


        loadLayout = findViewById(R.id.load_layout);

        //loadOwnButton = findViewById(R.id.load_own_button);
        //loadOwnButton.setVisibility(View.VISIBLE);

        scheduleLayout = findViewById(R.id.schedule_layout);
        switchLayout = findViewById(R.id.switch_layout);
        loadSessionLayout = findViewById(R.id.load_session_layout);

        sessionLayout = findViewById(R.id.session_layout);
        sessionTable = findViewById(R.id.session_table);
        loadSession = findViewById(R.id.load_session);
        loadTeachersButton = findViewById(R.id.load_teachers);

        scheduleTable = findViewById(R.id.schedule_table);
        facSpinner = findViewById(R.id.fac_spinner);
        teacherSpinner = findViewById(R.id.teacher_spinner);
        loadButton = findViewById(R.id.load_teacher_button);
        backButton = findViewById(R.id.back_button);

        znamButton = findViewById(R.id.znam_button);
        numButton = findViewById(R.id.num_button);

        prevDayButton = findViewById(R.id.prev_day_button);
        nextDayButton = findViewById(R.id.next_day_button);
        returnButton = findViewById(R.id.return_button);
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
        Thread thread = new Thread(() -> {
            teacherParserThread = new TeacherParserThread(this, baseUrl + "/schedule", facSpinner, teacherSpinner);
            teacherParserThread.start();
            try {
                teacherParserThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> loadButton.setEnabled(true));
        });
        thread.start();

        try {
            thread.join(); // Ждем завершения потока перед выполнением следующих строк
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // загружаем расписание авторизованного препода
        isOwnSchedule = true;
        scheduleGenerator();


        loadTeachersButton.setOnClickListener(v -> {
            isOwnSchedule = false;

            loadLayout.setVisibility(View.VISIBLE);

            scheduleLayout.setVisibility(View.GONE);
            scheduleTable.setVisibility(View.GONE);
            switchLayout.setVisibility(View.GONE);
            loadSessionLayout.setVisibility(View.GONE);
        });

        loadButton.setOnClickListener(v -> {
            switchToSchedule();

            scheduleGenerator();
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
            scheduleGenerator();
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
            scheduleGenerator();
        });


        znamButton.setOnClickListener(v -> {
            isLessonVisibleZnam[0] = !isLessonVisibleZnam[0];
            for (int i = 0; i < lessons.length; i++) {
                String rawText = HtmlCompat.fromHtml(savedSchedules.get(currentDayOfWeek - 1).get(i), HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
                if (rawText.contains("З:")) {
                    String savedLesson = savedSchedules.get(currentDayOfWeek - 1).get(i);
                    if (!isLessonVisibleZnam[0]) {
                        znamButton.setBackgroundColor(fadedColor);
                        if(!rawText.contains("Ч:"))
                            lessons[i].setText("");
                        else {
                            if (!isLessonVisibleChis[0])
                                lessons[i].setText("");
                            else {
                                int indexOfZnam = savedLesson.indexOf("З:");
                                if (checkSwitches(savedLesson.substring(0, indexOfZnam)))
                                    lessons[i].setText(HtmlCompat.fromHtml(savedLesson.substring(0, indexOfZnam), HtmlCompat.FROM_HTML_MODE_LEGACY));
                            }
                        }
                    } else {
                        znamButton.setBackgroundResource(android.R.drawable.btn_default);
                        if (savedLesson.contains("Ч:")) {
                            if (!isLessonVisibleChis[0]) {
                                int indexOfZnam = savedLesson.indexOf("З:");
                                lessons[i].setText(HtmlCompat.fromHtml(savedLesson.substring(indexOfZnam), HtmlCompat.FROM_HTML_MODE_LEGACY));
                            } else
                                lessons[i].setText(HtmlCompat.fromHtml(savedLesson, HtmlCompat.FROM_HTML_MODE_LEGACY));
                        }
                        else
                            lessons[i].setText(HtmlCompat.fromHtml(savedLesson, HtmlCompat.FROM_HTML_MODE_LEGACY));
                    }
                }
            }
        });


        numButton.setOnClickListener(v -> {
            isLessonVisibleChis[0] = !isLessonVisibleChis[0];

            for (int i = 0; i < lessons.length; i++) {
                String rawText = HtmlCompat.fromHtml(savedSchedules.get(currentDayOfWeek - 1).get(i), HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
                if (rawText.contains("Ч:")) {
                    String savedLesson = savedSchedules.get(currentDayOfWeek - 1).get(i);
                    if (!isLessonVisibleChis[0]) {
                        numButton.setBackgroundColor(fadedColor);
                        if (!rawText.contains("З:"))
                            lessons[i].setText("");
                        else {
                            if (!isLessonVisibleZnam[0])
                                lessons[i].setText("");
                            else {
                                int indexOfZnam = savedLesson.indexOf("З:");
                                if (checkSwitches(savedLesson.substring(indexOfZnam)))
                                    lessons[i].setText(HtmlCompat.fromHtml(savedLesson.substring(indexOfZnam), HtmlCompat.FROM_HTML_MODE_LEGACY));
                            }
                        }
                    } else {
                        numButton.setBackgroundResource(android.R.drawable.btn_default);
                        if (savedLesson.contains("З:")) {
                            if (!isLessonVisibleZnam[0]) {
                                int indexOfZnam = savedLesson.indexOf("З:");
                                lessons[i].setText(HtmlCompat.fromHtml(savedLesson.substring(0, indexOfZnam), HtmlCompat.FROM_HTML_MODE_LEGACY));
                            } else
                                lessons[i].setText(HtmlCompat.fromHtml(savedLesson, HtmlCompat.FROM_HTML_MODE_LEGACY));
                        }
                        else
                            lessons[i].setText(HtmlCompat.fromHtml(savedLesson, HtmlCompat.FROM_HTML_MODE_LEGACY));
                    }
                }
            }
        });


        switchLek.setOnClickListener(v -> {
            boolean isChecked = switchLek.isChecked();
            for (int i = 0; i < lessons.length; i++) {
                String rawText = HtmlCompat.fromHtml(savedSchedules.get(currentDayOfWeek - 1).get(i), HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
                if (rawText.contains("ЛЕКЦИЯ")) {
                    if (!isChecked) {
                        lessons[i].setText("");
                    } else {
                        String savedLesson = savedSchedules.get(currentDayOfWeek - 1).get(i);
                        if (!rawText.contains("З:") && !rawText.contains("Ч:") )
                            lessons[i].setText(HtmlCompat.fromHtml(savedLesson, HtmlCompat.FROM_HTML_MODE_LEGACY));
                        else {
                            if (isLessonVisibleZnam[0] && isLessonVisibleChis[0])
                                lessons[i].setText(HtmlCompat.fromHtml(savedLesson, HtmlCompat.FROM_HTML_MODE_LEGACY));
                            else if (!isLessonVisibleZnam[0] && isLessonVisibleChis[0]) {
                                int indexOfZnam = savedLesson.indexOf("З:");
                                String part = savedLesson.substring(0, indexOfZnam);
                                lessons[i].setText(HtmlCompat.fromHtml(part, HtmlCompat.FROM_HTML_MODE_LEGACY));
                            }
                            else if (isLessonVisibleZnam[0] && !isLessonVisibleChis[0]) {
                                int indexOfZnam = savedLesson.indexOf("З:");
                                String part = savedLesson.substring(indexOfZnam);
                                lessons[i].setText(HtmlCompat.fromHtml(part, HtmlCompat.FROM_HTML_MODE_LEGACY));
                            }
                        }
                    }
                }
            }
        });

        switchPr.setOnClickListener(v -> {
            boolean isChecked = switchPr.isChecked();
            for (int i = 0; i < lessons.length; i++) {
                // Получаем текст без HTML-тегов для проверки содержимого
                String rawText = HtmlCompat.fromHtml(savedSchedules.get(currentDayOfWeek - 1).get(i), HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
                if (rawText.contains("ПРАКТИКА")) {
                    if (!isChecked) {
                        lessons[i].setText("");
                    } else {
                        String savedLesson = savedSchedules.get(currentDayOfWeek - 1).get(i);
                        if (!rawText.contains("З:") && !rawText.contains("Ч:")) {
                            lessons[i].setText(HtmlCompat.fromHtml(savedLesson, HtmlCompat.FROM_HTML_MODE_LEGACY));
                        } else {
                            if (isLessonVisibleZnam[0] && isLessonVisibleChis[0]) {
                                lessons[i].setText(HtmlCompat.fromHtml(savedLesson, HtmlCompat.FROM_HTML_MODE_LEGACY));
                            } else if (!isLessonVisibleZnam[0] && isLessonVisibleChis[0]) {
                                int indexOfZnam = rawText.indexOf("З:");
                                String part = savedLesson.substring(0, indexOfZnam);
                                lessons[i].setText(HtmlCompat.fromHtml(part, HtmlCompat.FROM_HTML_MODE_LEGACY));
                            } else if (isLessonVisibleZnam[0]) {
                                int indexOfZnam = rawText.indexOf("З:");
                                String part = savedLesson.substring(indexOfZnam);
                                lessons[i].setText(HtmlCompat.fromHtml(part, HtmlCompat.FROM_HTML_MODE_LEGACY));
                            }
                        }
                    }
                }
            }
        });


        switchLab.setOnClickListener(v -> {
            boolean isChecked = switchLab.isChecked();
            for (int i = 0; i < lessons.length; i++) {
                String rawText = HtmlCompat.fromHtml(savedSchedules.get(currentDayOfWeek - 1).get(i), HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
                if (rawText.contains("ЛАБОРАТОРНАЯ")) {
                    if (!isChecked) {
                        lessons[i].setText("");
                    } else {
                        String savedLesson = savedSchedules.get(currentDayOfWeek - 1).get(i);
                        if (!savedLesson.contains("З:") && !savedLesson.contains("Ч:") )
                            lessons[i].setText(HtmlCompat.fromHtml(savedLesson, HtmlCompat.FROM_HTML_MODE_LEGACY));
                        else {
                            if (isLessonVisibleZnam[0] && isLessonVisibleChis[0])
                                lessons[i].setText(HtmlCompat.fromHtml(savedLesson, HtmlCompat.FROM_HTML_MODE_LEGACY));
                            else if (!isLessonVisibleZnam[0] && isLessonVisibleChis[0]) {
                                int indexOfZnam = savedLesson.indexOf("З:");
                                String part = savedLesson.substring(0, indexOfZnam);
                                lessons[i].setText(HtmlCompat.fromHtml(part, HtmlCompat.FROM_HTML_MODE_LEGACY));
                            }
                            else if (isLessonVisibleZnam[0]) {
                                int indexOfZnam = savedLesson.indexOf("З:");
                                String part = savedLesson.substring(indexOfZnam);
                                lessons[i].setText(HtmlCompat.fromHtml(part, HtmlCompat.FROM_HTML_MODE_LEGACY));
                            }
                        }
                    }
                }
            }
        });

        loadSession.setOnClickListener(v -> {
            String teacher = !isOwnSchedule ? teacherSpinner.getSelectedItem().toString() :
                    getIntent().getStringExtra("fullName");
            String Url = baseUrl + teacherParserThread.getTeacherHref(teacher);
            //Log.d("Url", Url);

            new LoadSessionTeacherThread(TeacherActivity.this, sessionTable, sessionLayout, Url).start();

        });

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
            isOwnSchedule = false;
            savedSessionDoc = new Document("");
            savedSessionData.clear();
            dayOfWeekText.setVisibility(View.GONE);
            scheduleLayout.setVisibility(View.GONE);
            scheduleTable.setVisibility(View.GONE);
            switchLayout.setVisibility(View.GONE);
            loadSessionLayout.setVisibility(View.GONE);
            loadLayout.setVisibility(View.VISIBLE);
        });

        boolean shouldShowLoadLayout = getIntent().getBooleanExtra("showLoadLayout", false);
        if (shouldShowLoadLayout) {
            isOwnSchedule = false;
            scheduleLayout.setVisibility(View.GONE);
            scheduleTable.setVisibility(View.GONE);
            switchLayout.setVisibility(View.GONE);
            loadSessionLayout.setVisibility(View.GONE);
            loadLayout.setVisibility(View.VISIBLE);
        }
    }


    private boolean checkSwitches(String savedLesson){
        return savedLesson.contains("ЛЕКЦИЯ") && switchLek.isChecked() || savedLesson.contains("ПРАКТИКА") && switchPr.isChecked() || savedLesson.contains("ЛАБОРАТОРНАЯ") && switchLab.isChecked();
    }

    private void switchToSchedule(){
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
    }


    private void scheduleGenerator() {
        // Проверяем инициализацию savedSchedules
/*        if (savedSchedules == null || savedSchedules.size() != 7) {
            initSavedSchedules();
        }*/

        // Проверяем currentDayOfWeek
        if (currentDayOfWeek < 1 || currentDayOfWeek > 7) {
            currentDayOfWeek = 1;
        }

        String teacher = !isOwnSchedule ? teacherSpinner.getSelectedItem().toString() :
                getIntent().getStringExtra("fullName");
        String Url = baseUrl + teacherParserThread.getTeacherHref(teacher);

        new ParseScheduleTeacherThread(TeacherActivity.this, Url,
                savedSchedules, currentDayOfWeek, dayOfWeekText,
                lessons, isNumeratorWeek).start();

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
