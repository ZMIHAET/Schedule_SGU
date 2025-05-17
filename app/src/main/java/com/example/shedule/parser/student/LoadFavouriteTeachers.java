package com.example.shedule.parser.student;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.*;

import com.example.shedule.activity.teacher.TeacherActivity;
import com.example.shedule.parser.teacher.checkTeachers.TeacherList;
import com.example.shedule.parser.teacher.checkTeachers.TeacherParser;
import com.example.shedule.parser.teacher.teacherId.TeacherIdCache;

import java.util.ArrayList;
import java.util.List;

public class LoadFavouriteTeachers {

    private final Activity activity;
    private final LinearLayout loadLayout, favouritesLayout;
    private final EditText addTeacherInput;
    private final Button addTeacherButton, deleteTeacherButton;
    private final ListView favTeachersList;

    private final List<String> favouritesList;
    private ArrayAdapter<String> favouritesAdapter;
    private String selectedTeacher;

    public LoadFavouriteTeachers(Activity activity,
                                 LinearLayout loadLayout,
                                 LinearLayout favouritesLayout,
                                 EditText addTeacherInput,
                                 Button addTeacherButton,
                                 Button deleteTeacherButton,
                                 ListView favTeachersList) {
        this.activity = activity;
        this.loadLayout = loadLayout;
        this.favouritesLayout = favouritesLayout;
        this.addTeacherInput = addTeacherInput;
        this.addTeacherButton = addTeacherButton;
        this.deleteTeacherButton = deleteTeacherButton;
        this.favTeachersList = favTeachersList;

        this.favouritesList = loadFavourites(activity);
        setup();
    }

    private void setup() {
        favouritesAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, favouritesList);
        favTeachersList.setAdapter(favouritesAdapter);

        new Thread(() -> {
            TeacherParser.parseTeachers("https://www.sgu.ru/person");
            activity.runOnUiThread(() -> {
                addTeacherInput.setEnabled(true);
                addTeacherButton.setEnabled(true);
            });
        }).start();

        addTeacherButton.setOnClickListener(v -> {
            String fullName = addTeacherInput.getText().toString().trim();

            if (fullName.isEmpty()) {
                Toast.makeText(activity, "Введите ФИО преподавателя", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TeacherList.containsTeacher(fullName)) {
                favouritesList.add(fullName);
                favouritesAdapter.notifyDataSetChanged();
                saveFavourites(activity, favouritesList);
                addTeacherInput.setText("");
            } else {
                Toast.makeText(activity, "Преподаватель не найден", Toast.LENGTH_SHORT).show();
            }
        });

        favTeachersList.setOnItemClickListener((parent, view, position, id) -> {
            selectedTeacher = favouritesList.get(position);
            String teacherId = TeacherIdCache.getTeacherId(selectedTeacher);

            if (teacherId != null) {
                String teacherUrl = "https://www.sgu.ru/schedule/teacher/" + teacherId;
                Intent intent = new Intent(activity, TeacherActivity.class);
                intent.putExtra("teacherUrl", teacherUrl);
                activity.startActivity(intent);
            } else {
                Toast.makeText(activity, "ID преподавателя не найден", Toast.LENGTH_SHORT).show();
            }
        });

        favTeachersList.setOnItemLongClickListener((parent, view, position, id) -> {
            selectedTeacher = favouritesList.get(position);
            Toast.makeText(activity, "Выбран для удаления: " + selectedTeacher, Toast.LENGTH_SHORT).show();
            return true;
        });

        deleteTeacherButton.setOnClickListener(v -> {
            if (selectedTeacher != null && favouritesList.contains(selectedTeacher)) {
                favouritesList.remove(selectedTeacher);
                favouritesAdapter.notifyDataSetChanged();
                saveFavourites(activity, favouritesList);
                Toast.makeText(activity, "Удалён: " + selectedTeacher, Toast.LENGTH_SHORT).show();
                selectedTeacher = null;
            } else {
                Toast.makeText(activity, "Выберите преподавателя для удаления", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveFavourites(Context context, List<String> favourites) {
        SharedPreferences prefs = context.getSharedPreferences("fav_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        StringBuilder joined = new StringBuilder();
        for (String name : favourites) {
            joined.append(name).append(";");
        }
        editor.putString("fav_list", joined.toString());
        editor.apply();
    }

    public static List<String> loadFavourites(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("fav_prefs", Context.MODE_PRIVATE);
        String joined = prefs.getString("fav_list", "");
        List<String> result = new ArrayList<>();
        if (!joined.isEmpty()) {
            String[] names = joined.split(";");
            for (String name : names) {
                if (!name.trim().isEmpty()) result.add(name.trim());
            }
        }
        return result;
    }
}
