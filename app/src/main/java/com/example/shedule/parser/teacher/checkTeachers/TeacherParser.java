package com.example.shedule.parser.teacher.checkTeachers;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class TeacherParser {
    public static void parseTeachers(String url) {
        try {
            Document doc = Jsoup.connect(url).get();

            // Ищем все div с классом list-employee__info
            Elements teacherElements = doc.select("div.list-employee__info");

            for (Element teacherElement : teacherElements) {
                // Находим ФИО
                Element fioElement = teacherElement.selectFirst("div.list-employee__fio > a");
                if (fioElement == null) continue;

                String fullName = fioElement.text().trim();
                String[] parts = fullName.split(" ");

                if (parts.length >= 2) {
                    String lastName = parts[0];
                    String firstName = parts[1];
                    String patronymic = (parts.length > 2) ? parts[2] : "";

                    // Находим подразделение
                    Element deptElement = teacherElement.selectFirst("div.list-employee__subdivision");
                    String department = (deptElement != null) ? deptElement.text().trim() : "Неизвестная кафедра";

                    // Добавляем в список
                    TeacherList.addTeacher(lastName, firstName, patronymic, department);
                }
            }
        } catch (IOException e) {
            Log.e("TeacherParser", "Ошибка при парсинге: " + e.getMessage());
        }
    }
}
