package com.example.shedule.auth;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shedule.DB.DatabaseHelper;
import com.example.shedule.MainActivity;
import com.example.shedule.R;
import com.example.shedule.TeacherActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextPatronymic, editTextPassword;
    private RadioGroup radioGroupRole;
    private Button btnLogin, btnRegister;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextPatronymic = findViewById(R.id.editTextPatronymic);
        editTextPassword = findViewById(R.id.editTextPassword);
        radioGroupRole = findViewById(R.id.radioGroupRole);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        dbHelper = new DatabaseHelper(this);

        btnLogin.setOnClickListener(v -> loginUser());
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
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
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_USERS + " WHERE " +
                        DatabaseHelper.COLUMN_FIRST_NAME + "=? AND " +
                        DatabaseHelper.COLUMN_LAST_NAME + "=? AND " +
                        DatabaseHelper.COLUMN_PATRONYMIC + "=?",
                new String[]{firstName, lastName, patronymic});

        if (cursor.moveToFirst()) {
            String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD));
            String storedRole = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ROLE));

            if (PasswordHasher.checkPassword(password, storedPassword)) {
                if (selectedRole.equals(storedRole)) {
                    Intent intent = selectedRole.equals("Студент") ?
                            new Intent(this, MainActivity.class) :
                            new Intent(this, TeacherActivity.class);
                    startActivity(intent);
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
}