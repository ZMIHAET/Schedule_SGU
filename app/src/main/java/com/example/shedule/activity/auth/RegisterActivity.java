package com.example.shedule.activity.auth;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shedule.DB.DatabaseHelper;
import com.example.shedule.R;
import com.example.shedule.parser.teacher.checkTeachers.TeacherList;
import com.example.shedule.parser.teacher.checkTeachers.TeacherParser;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextPatronymic, editTextPassword;
    private RadioGroup radioGroupRole;
    private Button btnRegister;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextPatronymic = findViewById(R.id.editTextPatronymic);
        editTextPassword = findViewById(R.id.editTextPassword);
        radioGroupRole = findViewById(R.id.radioGroupRole);
        btnRegister = findViewById(R.id.btnRegister);

        dbHelper = new DatabaseHelper(this);
        btnRegister.setEnabled(false); // Заблокировать кнопку

        new Thread(() -> {
            TeacherParser.parseTeachers(getApplicationContext(), "https://www.sgu.ru/person");

            // После парсинга включаем кнопку в UI-потоке
            runOnUiThread(() -> btnRegister.setEnabled(true));
        }).start();
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String patronymic = editTextPatronymic.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        int selectedId = radioGroupRole.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Выберите роль", Toast.LENGTH_SHORT).show();
            return;
        }
        String role = ((RadioButton) findViewById(selectedId)).getText().toString();

        if (firstName.isEmpty() || lastName.isEmpty() || patronymic.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        // Если роль "Преподаватель", парсим сайт и проверяем наличие
        if (role.equalsIgnoreCase("Преподаватель")) {
            String fullName = lastName + " " + firstName + " " + patronymic;
            boolean isTeacherExists = TeacherList.containsTeacher(fullName);
            if (!isTeacherExists) {
                Toast.makeText(this, "Преподаватель не найден на сайте", Toast.LENGTH_LONG).show();
                return;
            }
            saveUserToDatabase(firstName, lastName, patronymic, password, role);
        } else {
            saveUserToDatabase(firstName, lastName, patronymic, password, role);
        }
    }

    // Отдельный метод для сохранения пользователя в БД
    private void saveUserToDatabase(String firstName, String lastName, String patronymic, String password, String role) {
        String hashedPassword = PasswordHasher.hashPassword(password);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_FIRST_NAME, firstName);
        values.put(DatabaseHelper.COLUMN_LAST_NAME, lastName);
        values.put(DatabaseHelper.COLUMN_PATRONYMIC, patronymic);
        values.put(DatabaseHelper.COLUMN_PASSWORD, hashedPassword);
        values.put(DatabaseHelper.COLUMN_ROLE, role);

        long result = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        db.close();

        if (result == -1) {
            Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
