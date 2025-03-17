package com.example.shedule;

import android.os.Bundle;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import com.example.shedule.parser.teacher.TeacherParserThread;

import java.util.ArrayList;
import java.util.List;

public class TeacherActivity extends AppCompatActivity {

    private Spinner facSpinner, teacherSpinner;
    private List<String> facultyList = new ArrayList<>();
    private List<String> teacherList = new ArrayList<>();
    private String baseUrl = "https://www.sgu.ru/schedule";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        facSpinner = findViewById(R.id.fac_spinner);
        teacherSpinner = findViewById(R.id.teacher_spinner);

        // Загружаем факультеты
        new TeacherParserThread(this, baseUrl, facSpinner, teacherSpinner).start();
    }
}
