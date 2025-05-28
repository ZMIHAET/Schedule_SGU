package com.example.shedule.activity.student;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shedule.R;
import com.example.shedule.parser.student.FacultySiteName;
import com.example.shedule.parser.student.LoadSessionStudentThread;
import com.example.shedule.parser.student.ParseScheduleStudentThread;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class OwnScheduleActivity extends AppCompatActivity {
    private TextView textViewFaculty, textViewCourse, textViewGroup;
    private LinearLayout checkboxContainer;
    private Button loadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_own_schedule);

        textViewFaculty = findViewById(R.id.textViewFaculty);
        textViewCourse = findViewById(R.id.textViewCourse);
        textViewGroup = findViewById(R.id.textViewGroup);
        checkboxContainer = findViewById(R.id.checkboxContainer);
        loadButton = findViewById(R.id.load_button);

        String faculty = getIntent().getStringExtra("faculty");
        String course = getIntent().getStringExtra("course");
        String group = getIntent().getStringExtra("group");

        textViewFaculty.setText("Факультет: " + faculty);
        textViewCourse.setText("Курс: " + course);
        textViewGroup.setText("Группа: " + group);

        assert group != null;
        String scheduleUrl = getString(faculty, group);

        Log.d("scheduleUrl", scheduleUrl);
        new Thread(() -> parseAndCreateCheckboxes(scheduleUrl)).start();

        loadButton.setOnClickListener(v -> {

            // Список отмеченных подгрупп
            ArrayList<String> selectedSubgroups = new ArrayList<>();
            for (int i = 0; i < checkboxContainer.getChildCount(); i++) {
                View view = checkboxContainer.getChildAt(i);
                if (view instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) view;
                    if (checkBox.isChecked()) {
                        selectedSubgroups.add(checkBox.getText().toString());
                    }
                }
            }

            // Проверка: выбрана хотя бы одна галка
            if (selectedSubgroups.isEmpty()) {
                Toast.makeText(OwnScheduleActivity.this, "Выберите хотя бы одну подгруппу", Toast.LENGTH_SHORT).show();
                return;
            }

            // Отправка результата в MainActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("scheduleUrl", scheduleUrl);
            resultIntent.putStringArrayListExtra("subgroups", new ArrayList<>(selectedSubgroups));
            setResult(RESULT_OK, resultIntent);
            finish();
        });

    }

    @NonNull
    private static String getString(String faculty, String group) {
        FacultySiteName facultySiteName = new FacultySiteName();
        String facultyUrl = facultySiteName.showFacultyName(faculty);
        String scheduleUrl;

        if (group.contains("(зо)")) {
            String[] parts = group.split(" ");
            scheduleUrl = "https://www.sgu.ru/schedule/" + facultyUrl + "/zo/" + parts[0];
        } else if (group.contains("(о-зо)")) {
            String[] parts = group.split(" ");
            scheduleUrl = "https://www.sgu.ru/schedule/" + facultyUrl + "/vo/" + parts[0];
        } else {
            scheduleUrl = "https://www.sgu.ru/schedule/" + facultyUrl + "/do/" + group;
        }
        return scheduleUrl;
    }

    private void parseAndCreateCheckboxes(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.select("div.schedule-table__lesson-uncertain");

            Set<String> subgroupLabels = new HashSet<>();
            for (Element el : elements) {
                String label = el.text().trim();
                if (!label.isEmpty()) {
                    subgroupLabels.add(label);
                }
            }

            runOnUiThread(() -> {
                for (String label : subgroupLabels) {
                    CheckBox checkBox = new CheckBox(this);
                    checkBox.setText(label);
                    checkboxContainer.addView(checkBox);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}