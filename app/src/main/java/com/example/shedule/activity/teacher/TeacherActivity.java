package com.example.shedule.activity.teacher;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.text.HtmlCompat;

import com.example.shedule.R;
import com.example.shedule.activity.auth.LoginActivity;
import com.example.shedule.parser.teacher.LoadSessionTeacherThread;
import com.example.shedule.parser.teacher.ParseScheduleTeacherThread;
import com.example.shedule.parser.teacher.TeacherParserThread;
import com.example.shedule.parser.teacher.checkTeachers.Teacher;
import com.example.shedule.parser.teacher.checkTeachers.TeacherList;
import com.example.shedule.parser.teacher.checkTeachers.TeacherParser;
import com.example.shedule.parser.teacher.teacherId.TeacherIdCacheLoader;

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
            znamButton, numButton, loadSession, returnButton, backButton, loadTeachersButton, loadDepartment, logoutButton;
    private SwitchCompat switchLek, switchPr, switchLab;
    private TextView dayOfWeekText;
    private int currentDayOfWeek = 1, fadedColor;

    private boolean isOwnSchedule = false;

    private TextView[] lessons;
    private List<ArrayList<String>> savedSchedules = new ArrayList<>();
    private List<String> savedSessionData = new ArrayList<>();

    private Document savedSessionDoc;
    boolean isNumeratorWeek;
    boolean isEnemy = false;
    String defaultUrl = "";


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
        logoutButton = findViewById(R.id.logout_button);

        loadDepartment = findViewById(R.id.load_department);
        loadDepartment.setEnabled(false);

        if (TeacherList.getTeachers().isEmpty()) {
            new Thread(() -> {
                Log.d("Loaded", "Loading teachers...");
                TeacherParser.parseTeachers(getApplicationContext(), "https://www.sgu.ru/person");
                runOnUiThread(() -> loadDepartment.setEnabled(true));
            }).start();
        } else {
            // Обязательно включаем кнопку, если данные уже были загружены
            loadDepartment.setEnabled(true);
        }

        // Запуск фоновой загрузки ID преподавателей
        new TeacherIdCacheLoader(getApplicationContext()).start();



        backButton = findViewById(R.id.back_button);

        znamButton = findViewById(R.id.znam_button);
        numButton = findViewById(R.id.num_button);
        numButton.setBackgroundResource(android.R.drawable.btn_default);
        znamButton.setBackgroundResource(android.R.drawable.btn_default);


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

        //обработчик учителя из расписания студента
        String enemyUrl = getIntent().getStringExtra("teacherUrl");
        if (enemyUrl != null) {
            scheduleGenerator(enemyUrl);
            loadDepartment.setVisibility(View.GONE);
            logoutButton.setVisibility(View.GONE);
            isOwnSchedule = false;
        }

        //обработчик избранных преподавателей
        String teacherUrl = getIntent().getStringExtra("teacherUrl");
        if (teacherUrl != null) {
            scheduleGenerator(teacherUrl);
            loadDepartment.setVisibility(View.GONE);
            logoutButton.setVisibility(View.GONE);
            loadTeachersButton.setVisibility(View.GONE);
            isOwnSchedule = false;
        }


        loadTeachersButton.setOnClickListener(v -> {
            isOwnSchedule = false;

            loadLayout.setVisibility(View.VISIBLE);


            loadDepartment.setVisibility(View.GONE);
            logoutButton.setVisibility(View.GONE);
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
            if (!isEnemy)
                scheduleGenerator();
            else
                scheduleGenerator(enemyUrl);
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
            if (!isEnemy)
                scheduleGenerator();
            else
                scheduleGenerator(enemyUrl);
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
            // Скрываем лишние элементы
            scheduleLayout.setVisibility(View.GONE);
            scheduleTable.setVisibility(View.GONE);
            switchLayout.setVisibility(View.GONE);
            loadSessionLayout.setVisibility(View.GONE);
            loadDepartment.setVisibility(View.GONE);
            logoutButton.setVisibility(View.GONE);

            String finalUrl;

            if (teacherUrl != null && !teacherUrl.isEmpty()) {
                finalUrl = teacherUrl;
            } else if (enemyUrl != null && !enemyUrl.isEmpty()) {
                finalUrl = enemyUrl;
            } else {
                String teacher = !isOwnSchedule
                        ? teacherSpinner.getSelectedItem().toString()
                        : getIntent().getStringExtra("fullName");
                finalUrl = baseUrl + teacherParserThread.getTeacherHref(teacher);
            }

            new LoadSessionTeacherThread(TeacherActivity.this, sessionTable, sessionLayout, finalUrl).start();
        });


        loadDepartment.setOnClickListener(v -> {
            String fullName = getIntent().getStringExtra("fullName");
            if (fullName == null || fullName.isEmpty()) {
                Toast.makeText(this, "ФИО преподавателя не передано", Toast.LENGTH_SHORT).show();
                return;
            }

            String department = TeacherList.getTeacherDepartment(fullName);
            if (department.equals("Кафедра не найдена")) {
                Toast.makeText(this, "Кафедра не найдена", Toast.LENGTH_SHORT).show();
                return;
            }

            // Получаем всех преподавателей с той же кафедры
            List<Teacher> sameDeptTeachers = new ArrayList<>();
            for (Teacher t : TeacherList.getTeachers()) {
                if (department.equalsIgnoreCase(t.getDepartment()) && !t.getFullName().equalsIgnoreCase(fullName)) {
                    sameDeptTeachers.add(t);
                }
            }

            if (sameDeptTeachers.isEmpty()) {
                Toast.makeText(this, "Нет других преподавателей с этой кафедры", Toast.LENGTH_SHORT).show();
                return;
            }

            // Передаём список ФИО в новое Activity
            Intent intent = new Intent(TeacherActivity.this, DepartmentTeachersActivity.class);
            ArrayList<String> names = new ArrayList<>();
            for (Teacher t : sameDeptTeachers) names.add(t.getFullName());
            intent.putStringArrayListExtra("teacherNames", names);
            intent.putExtra("departmentName", department);
            startActivity(intent);
        });


        backButton.setOnClickListener(v -> {
            dayOfWeekText.setVisibility(View.VISIBLE);
            scheduleLayout.setVisibility(View.VISIBLE);
            scheduleTable.setVisibility(View.VISIBLE);
            switchLayout.setVisibility(View.VISIBLE);
            loadSessionLayout.setVisibility(View.VISIBLE);
            sessionLayout.setVisibility(View.GONE);
            loadDepartment.setVisibility(View.GONE);
            logoutButton.setVisibility(View.GONE);
            Log.d("isOwnSchedule", String.valueOf(isOwnSchedule));
            if (isOwnSchedule) {
                loadDepartment.setVisibility(View.VISIBLE);
                logoutButton.setVisibility(View.VISIBLE);
            }
        });

        returnButton.setOnClickListener(v -> {
            isOwnSchedule = true;
            dayOfWeekText.setVisibility(View.VISIBLE);
            scheduleLayout.setVisibility(View.VISIBLE);
            scheduleTable.setVisibility(View.VISIBLE);
            switchLayout.setVisibility(View.VISIBLE);
            loadSessionLayout.setVisibility(View.VISIBLE);
            loadLayout.setVisibility(View.GONE);
            Log.d("isOwnSchedule", String.valueOf(isOwnSchedule));
            if (isOwnSchedule) {
                loadDepartment.setVisibility(View.VISIBLE);
                logoutButton.setVisibility(View.VISIBLE);
            }

        });
        logoutButton.setOnClickListener(v -> logout());



        //обработчик расписания преподов из расписания студента
        boolean shouldShowLoadLayout = getIntent().getBooleanExtra("showLoadLayout", false);
        if (shouldShowLoadLayout) {
            isOwnSchedule = false;
            scheduleLayout.setVisibility(View.GONE);
            scheduleTable.setVisibility(View.GONE);
            switchLayout.setVisibility(View.GONE);
            loadSessionLayout.setVisibility(View.GONE);
            loadLayout.setVisibility(View.VISIBLE);
            returnButton.setVisibility(View.GONE);
            loadDepartment.setVisibility(View.GONE);
            logoutButton.setVisibility(View.GONE);
            loadTeachersButton.setVisibility(View.GONE);
        }
    }

    private boolean checkSwitches(String savedLesson){
        return savedLesson.contains("ЛЕКЦИЯ") && switchLek.isChecked() || savedLesson.contains("ПРАКТИКА") && switchPr.isChecked() || savedLesson.contains("ЛАБОРАТОРНАЯ") && switchLab.isChecked();
    }

    private void switchToSchedule(){
        savedSchedules.clear();
        for (int i = 0; i < 7; i++) {
            savedSchedules.add(new ArrayList<>());
        }
        savedSessionDoc = new Document("");
        savedSessionData.clear();
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

        // Проверяем currentDayOfWeek
        if (currentDayOfWeek < 1 || currentDayOfWeek > 7) {
            currentDayOfWeek = 1;
        }
        String teacher;
        String Url;
        if (!isOwnSchedule) {
            teacher = teacherSpinner.getSelectedItem().toString();
            Url = baseUrl + teacherParserThread.getTeacherHref(teacher);
        }
        else {
            teacher = getIntent().getStringExtra("fullName");
            Url = baseUrl + teacherParserThread.getTeacherHref(teacher);
            defaultUrl = Url;
        }

        Log.d("Url", Url);

        new ParseScheduleTeacherThread(TeacherActivity.this, Url, defaultUrl,
                savedSchedules, currentDayOfWeek, dayOfWeekText,
                lessons, isNumeratorWeek).start();

        dayOfWeekText.setVisibility(View.VISIBLE);
        prevDayButton.setVisibility(View.VISIBLE);
        nextDayButton.setVisibility(View.VISIBLE);
    }

    private void scheduleGenerator(String url) {
        isEnemy = true;
        // Проверяем currentDayOfWeek
        if (currentDayOfWeek < 1 || currentDayOfWeek > 7) {
            currentDayOfWeek = 1;
        }

        defaultUrl = url;
        new ParseScheduleTeacherThread(TeacherActivity.this, url,
                defaultUrl, savedSchedules, currentDayOfWeek, dayOfWeekText,
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

    private void logout() {
        getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}