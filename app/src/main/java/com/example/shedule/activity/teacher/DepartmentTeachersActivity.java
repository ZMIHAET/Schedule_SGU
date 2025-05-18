package com.example.shedule.activity.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shedule.R;
import com.example.shedule.parser.teacher.teacherId.TeacherIdCache;

import java.util.ArrayList;

public class DepartmentTeachersActivity extends AppCompatActivity {

    private TextView departmentNameText;
    private ListView departmentTeachersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department_teachers);

        departmentNameText = findViewById(R.id.department_name);
        departmentTeachersList = findViewById(R.id.department_teachers_list);

        // Получаем данные из Intent
        ArrayList<String> teacherNames = getIntent().getStringArrayListExtra("teacherNames");
        String departmentName = getIntent().getStringExtra("departmentName");

        if (departmentName != null) {
            departmentNameText.setText("Кафедра: " + departmentName);
        }

        if (teacherNames == null || teacherNames.isEmpty()) {
            Toast.makeText(this, "Список преподавателей пуст", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Показываем список преподавателей
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, teacherNames);
        departmentTeachersList.setAdapter(adapter);

        // Обработка нажатия на преподавателя
        departmentTeachersList.setOnItemClickListener((parent, view, position, id) -> {
            String fullName = teacherNames.get(position);
            Log.d("fullName", fullName);
            String teacherId = TeacherIdCache.getTeacherId(fullName);

            if (teacherId != null) {
                String teacherUrl = "https://www.sgu.ru/schedule/teacher/" + teacherId;
                Intent intent = new Intent(DepartmentTeachersActivity.this, TeacherActivity.class);
                intent.putExtra("teacherUrl", teacherUrl);
                intent.putExtra("fullName", fullName);
                startActivity(intent);
            } else {
                Toast.makeText(this, "ID преподавателя не найден", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
