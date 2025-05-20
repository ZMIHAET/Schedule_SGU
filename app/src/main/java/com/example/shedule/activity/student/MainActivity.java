package com.example.shedule.activity.student;

import static com.example.shedule.R.*;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.text.HtmlCompat;
import androidx.core.widget.NestedScrollView;

import com.example.shedule.R;
import com.example.shedule.activity.teacher.TeacherActivity;
import com.example.shedule.parser.student.FacultySiteName;
import com.example.shedule.parser.student.LoadFavouriteTeachers;
import com.example.shedule.parser.student.LoadSessionStudentThread;
import com.example.shedule.parser.student.ParseFacultiesThread;
import com.example.shedule.parser.student.ParseGroupsThread;
import com.example.shedule.parser.student.ParseScheduleStudentThread;
import com.example.shedule.parser.teacher.checkTeachers.TeacherList;
import com.example.shedule.parser.teacher.checkTeachers.TeacherParser;
import com.example.shedule.parser.teacher.teacherId.TeacherIdCache;
import com.example.shedule.parser.teacher.teacherId.TeacherIdCacheLoader;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
public class MainActivity extends AppCompatActivity {

    private Button loadButton, prevDayButton, nextDayButton,
            znamButton, numButton, loadSession, backButton, returnButton,
    loadTeacherSchedule, favouritesTeachers, addTeacherButton, deleteTeacherButton, backFromFavs;
    private NestedScrollView scheduleScrollView;
    private EditText addTeacherInput;
    private ListView favTeachersList;
    private Spinner facultySpinner, groupSpinner, courseSpinner;
    private TextView dayOfWeekText;
    private SwitchCompat switchLek, switchPr, switchLab;
    private int currentDayOfWeek = 1, fadedColor;
    private LinearLayout loadLayout, scheduleLayout, switchLayout,
            loadSessionLayout, sessionLayout, favouritesLayout;
    private TableLayout sessionTable, scheduleTable;
    private TextView[] lessons;
    private List<ArrayList<String>> savedSchedules = new ArrayList<>();
    private List<String> savedSessionData = new ArrayList<>();
    private Document savedSessionDoc;
    private FacultySiteName facultySiteName;
    boolean isNumeratorWeek;
    private String selectedTeacher = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        currentDayOfWeek = (day == Calendar.SUNDAY) ? 1 : day - 1;

        // Определяем тип текущей недели: числитель (Ч) или знаменатель (З)
        int weekNumber = calendar.get(Calendar.WEEK_OF_YEAR);
        isNumeratorWeek = (weekNumber % 2 == 1); // нечетные недели — числитель


        // Запуск фоновой загрузки ID преподавателей
        new TeacherIdCacheLoader(getApplicationContext()).start();

        facultySpinner = findViewById(R.id.faculty_spinner);
        groupSpinner = findViewById(R.id.group_spinner);
        courseSpinner = findViewById(R.id.course_spinner);
        loadButton = findViewById(R.id.load_button);
        dayOfWeekText = findViewById(R.id.day_of_week_text);
        prevDayButton = findViewById(R.id.prev_day_button);
        nextDayButton = findViewById(R.id.next_day_button);
        znamButton = findViewById(R.id.znam_button);
        numButton = findViewById(R.id.num_button);

        addTeacherButton = findViewById(id.add_teacher_button);
        addTeacherButton.setEnabled(false);
        addTeacherInput = findViewById(id.add_teacher_input);
        addTeacherInput.setEnabled(false);
        deleteTeacherButton = findViewById(id.delete_teacher_button);

        favTeachersList = findViewById(R.id.fav_teachers_list);
        List<String> favouritesList = LoadFavouriteTeachers.loadFavourites(this);
        ArrayAdapter<String> favouritesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, favouritesList);
        favTeachersList.setAdapter(favouritesAdapter);
        backFromFavs = findViewById(R.id.back_from_favs);




        favouritesTeachers = findViewById(id.favourites_teachers);
        loadSession = findViewById(id.load_session);
        switchLek = findViewById(R.id.switch_lek);
        switchPr = findViewById(R.id.switch_pr);
        switchLab = findViewById(R.id.switch_lab);
        loadLayout = findViewById(R.id.load_layout);
        scheduleLayout = findViewById(R.id.schedule_layout);
        favouritesLayout = findViewById(R.id.favourites_layout);

        scheduleTable = findViewById(R.id.schedule_table);
        scheduleScrollView = findViewById(id.schedule_scroll_view);
    
        switchLayout = findViewById(R.id.switch_layout);
        loadSessionLayout = findViewById(R.id.load_session_layout);
        sessionLayout = findViewById(id.session_layout);
        sessionTable = findViewById(R.id.session_table);
        backButton = findViewById(R.id.back_button);
        returnButton = findViewById(id.return_button);
        loadTeacherSchedule = findViewById(R.id.load_teacher_schedule);
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
            scheduleScrollView.setVisibility(View.VISIBLE);
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

        loadTeacherSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TeacherActivity.class);
            intent.putExtra("showLoadLayout", true); // передаем флаг
            startActivity(intent);
        });

        favouritesTeachers.setOnClickListener(v ->{
            loadLayout.setVisibility(View.GONE);
            favouritesLayout.setVisibility(View.VISIBLE);

            new LoadFavouriteTeachers(this, loadLayout, favouritesLayout, addTeacherInput,
                    addTeacherButton, deleteTeacherButton, favTeachersList);
        });

        backFromFavs.setOnClickListener(v ->{
            loadLayout.setVisibility(View.VISIBLE);
            favouritesLayout.setVisibility(View.GONE);
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

        loadSession.setOnClickListener(v ->
                new LoadSessionStudentThread(facultySpinner, groupSpinner, savedSessionDoc, MainActivity.this, sessionTable, sessionLayout).start()
        );


        backButton.setOnClickListener(v -> {
            dayOfWeekText.setVisibility(View.VISIBLE);
            scheduleLayout.setVisibility(View.VISIBLE);
            scheduleTable.setVisibility(View.VISIBLE);
            scheduleScrollView.setVisibility(View.VISIBLE);
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
            scheduleScrollView.setVisibility(View.GONE);
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
                lessons, isNumeratorWeek).start();
        dayOfWeekText.setVisibility(View.VISIBLE);
        prevDayButton.setVisibility(View.VISIBLE);
        nextDayButton.setVisibility(View.VISIBLE);
    }

    public void showSessionLayout() {
        scheduleLayout.setVisibility(View.GONE);
        scheduleTable.setVisibility(View.GONE);
        scheduleScrollView.setVisibility(View.GONE);
        switchLayout.setVisibility(View.GONE);
        loadSessionLayout.setVisibility(View.GONE);
        sessionLayout.setVisibility(View.VISIBLE);
    }


}