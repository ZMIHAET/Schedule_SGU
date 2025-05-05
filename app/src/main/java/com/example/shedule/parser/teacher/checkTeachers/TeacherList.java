package com.example.shedule.parser.teacher.checkTeachers;

import java.util.ArrayList;
import java.util.List;

public class TeacherList {
    private static final List<Teacher> teachers = new ArrayList<>();

    public static void addTeacher(String lastName, String firstName, String patronymic, String department) {
        teachers.add(new Teacher(lastName, firstName, patronymic, department));
    }

    public static boolean containsTeacher(String fullName) {
        return teachers.stream().anyMatch(t -> t.getFullName().equalsIgnoreCase(fullName));
    }

    public static String getTeacherDepartment(String fullName) {
        return teachers.stream()
                .filter(t -> t.getFullName().equalsIgnoreCase(fullName))
                .map(Teacher::getDepartment)
                .findFirst()
                .orElse("Кафедра не найдена");
    }

    public static List<Teacher> getTeachers() {
        return teachers;
    }
}

