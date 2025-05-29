package com.example.shedule.activity.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shedule.DB.DatabaseHelper;
import com.example.shedule.activity.student.MainActivity;
import com.example.shedule.R;
import com.example.shedule.activity.student.OwnScheduleActivity;
import com.example.shedule.activity.teacher.TeacherActivity;
import com.example.shedule.parser.student.ParseFacultiesThread;
import com.example.shedule.parser.student.ParseGroupsThread;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextPatronymic, editTextPassword;
    private RadioGroup radioGroupRole;
    private Button btnLogin, btnRegister;
    private DatabaseHelper dbHelper;
    private Spinner facultySpinner, courseSpinner, groupSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        long expiry = prefs.getLong("expiryTime", 0);

        if (isLoggedIn && System.currentTimeMillis() < expiry) {
            String role = prefs.getString("role", "");
            String fullName = prefs.getString("fullName", "");

            Intent intent = role.equals("Студент") ? new Intent(this, MainActivity.class)
                    : new Intent(this, TeacherActivity.class);
            intent.putExtra("fullName", fullName);
            startActivity(intent);
            finish();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextPatronymic = findViewById(R.id.editTextPatronymic);
        editTextPassword = findViewById(R.id.editTextPassword);
        radioGroupRole = findViewById(R.id.radioGroupRole);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        facultySpinner = findViewById(R.id.faculty_spinner);
        courseSpinner = findViewById(R.id.course_spinner);
        groupSpinner = findViewById(R.id.group_spinner);

        new ParseFacultiesThread(this, facultySpinner).start();

        radioGroupRole.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadio = findViewById(checkedId);
            String role = selectedRadio.getText().toString();
            boolean isStudent = role.equals("Студент");

            int visibility = isStudent ? View.VISIBLE : View.GONE;
            facultySpinner.setVisibility(visibility);
            courseSpinner.setVisibility(visibility);
            groupSpinner.setVisibility(visibility);
        });

        facultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String faculty = facultySpinner.getSelectedItem().toString();
                new ParseGroupsThread(LoginActivity.this, faculty, courseSpinner, groupSpinner).start();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Нет действий при сбросе выбора
            }
        });

        dbHelper = new DatabaseHelper(this);

        btnLogin.setOnClickListener(v -> loginUser());
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view instanceof EditText) {
                Rect outRect = new Rect();
                view.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    view.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void loginUser() {
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String patronymic = editTextPatronymic.getText().toString().trim();
        String password = editTextPassword.getText().toString();

        int selectedRoleId = radioGroupRole.getCheckedRadioButtonId();
        if (selectedRoleId == -1) {
            Toast.makeText(this, "Выберите роль", Toast.LENGTH_SHORT).show();
            return;
        }
        String selectedRole = ((RadioButton) findViewById(selectedRoleId)).getText().toString();


        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query;
        String[] args;

        if (patronymic.isEmpty()) {
            query = "SELECT * FROM " + DatabaseHelper.TABLE_USERS + " WHERE " +
                    DatabaseHelper.COLUMN_FIRST_NAME + "=? AND " +
                    DatabaseHelper.COLUMN_LAST_NAME + "=? AND " +
                    "(" + DatabaseHelper.COLUMN_PATRONYMIC + " IS NULL OR " + DatabaseHelper.COLUMN_PATRONYMIC + "='')";
            args = new String[]{firstName, lastName};
        } else {
            query = "SELECT * FROM " + DatabaseHelper.TABLE_USERS + " WHERE " +
                    DatabaseHelper.COLUMN_FIRST_NAME + "=? AND " +
                    DatabaseHelper.COLUMN_LAST_NAME + "=? AND " +
                    DatabaseHelper.COLUMN_PATRONYMIC + "=?";
            args = new String[]{firstName, lastName, patronymic};
        }

        Cursor cursor = db.rawQuery(query, args);


        if (cursor.moveToFirst()) {
            String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD));
            String storedRole = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ROLE));

            if (PasswordHasher.checkPassword(password, storedPassword)) {
                if (selectedRole.equals(storedRole)) {
                    if (selectedRole.equals("Студент")) {
                        saveLoginSession("Студент", lastName + " " + firstName + " " + patronymic);

                        String faculty = facultySpinner.getSelectedItem().toString();
                        String course = courseSpinner.getSelectedItem().toString();
                        String group = groupSpinner.getSelectedItem().toString();

                        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();

                        if (!faculty.equals(prefs.getString("faculty", ""))) {
                            editor.putString("faculty", faculty);
                        }
                        if (!course.equals(prefs.getString("course", ""))) {
                            editor.putString("course", course);
                        }
                        if (!group.equals(prefs.getString("group", ""))) {
                            editor.putString("group", group);
                        }

                        Log.d("faculty", faculty);
                        Log.d("course", course);
                        Log.d("group", group);

                        editor.apply();

                        startActivity(new Intent(this, MainActivity.class));
                    } else {
                        saveLoginSession("Преподаватель", lastName + " " + firstName + " " + patronymic);
                        Intent intent = new Intent(this, TeacherActivity.class);
                        intent.putExtra("fullName", lastName + " " + firstName + " " + patronymic);
                        startActivity(intent);
                    }

                    finish();
                } else {
                    Toast.makeText(this, "Неверная роль", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, "Неверный пароль", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
        db.close();

    }

    private void saveLoginSession(String role, String fullName) {
        long oneWeekMillis = 7 * 24 * 60 * 60 * 1000L;
        long currentTime = System.currentTimeMillis();
        getSharedPreferences("auth", MODE_PRIVATE).edit()
                .putBoolean("isLoggedIn", true)
                .putString("role", role)
                .putString("fullName", fullName)
                .putLong("loginTime", currentTime)
                .putLong("expiryTime", currentTime + oneWeekMillis)
                .apply();
    }


}