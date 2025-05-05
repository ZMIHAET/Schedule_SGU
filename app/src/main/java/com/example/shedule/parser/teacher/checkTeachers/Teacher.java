package com.example.shedule.parser.teacher.checkTeachers;

public class Teacher {
    private String lastName;
    private String firstName;
    private String patronymic;
    private String department;

    public Teacher(String lastName, String firstName, String patronymic, String department) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.patronymic = patronymic;
        this.department = department;
    }

    public String getFullName() {
        return lastName + " " + firstName + " " + patronymic;
    }

    public String getDepartment() {
        return department;
    }
}

