package com.example.shedule.parser.student;

public class FacultySiteName {
    public String showFacultyName(String facultyRuName){
        String facultyCorrectName;
        switch (facultyRuName){
            case "Биологический факультет":
                facultyCorrectName = "bf";
                break;
            case "Биологический фак-т":
                facultyCorrectName = "bf";
                break;

            case "Географический факультет":
                facultyCorrectName = "gf";
                break;
            case "Географический фак-т":
                facultyCorrectName = "gf";
                break;

            case "Геологический факультет":
                facultyCorrectName = "gl";
                break;
            case "Геологический фак-т":
                facultyCorrectName = "gl";
                break;

            case "Институт искусств (ПИ)":
                facultyCorrectName = "piii";
                break;
            case "И-т искусств":
                facultyCorrectName = "piii";
                break;

            case "Институт истории и международных отношений":
                facultyCorrectName = "imo";
                break;
            case "ИИиМО":
                facultyCorrectName = "imo";
                break;

            case "Институт физики": //совпадает
                facultyCorrectName = "ff";
                break;


            case "Институт физической культуры и спорта (ПИ)":
                facultyCorrectName = "piifk";
                break;
            case "И-т физ. культуры":
                facultyCorrectName = "piifk";
                break;

            case "Институт филологии и журналистики":
                facultyCorrectName = "ifg";
                break;
            case "ИФиЖ":
                facultyCorrectName = "ifg";
                break;

            case "Институт химии": //совпадает
                facultyCorrectName = "ih";
                break;

            case "Институт дополнительного профессионального образования":
                facultyCorrectName = "idpo";
                break;
            case "ИДПО":
                facultyCorrectName = "idpo";
                break;

            case "Механико-математический факультет":
                facultyCorrectName = "mm";
                break;
            case "Мех-мат":
                facultyCorrectName = "mm";
                break;

            case "Социологический факультет":
                facultyCorrectName = "sf";
                break;
            case "Социологический фак-т":
                facultyCorrectName = "sf";
                break;

            case "Факультет гуманитарных дисциплин, русского и иностранных языков (ПИ)":
                facultyCorrectName = "gdrin";
                break;
            case "ГДРЯиИН":
                facultyCorrectName = "gdrin";
                break;

            case "Факультет компьютерных наук и информационных технологий":
                facultyCorrectName = "knt";
                break;
            case "КНиИТ":
                facultyCorrectName = "knt";
                break;

            case "Факультет психологии":
                facultyCorrectName = "fps";
                break;
            case "Фак-т психологии":
                facultyCorrectName = "fps";
                break;

            case "Факультет психолого-педагогического и специального образования (ПИ)":
                facultyCorrectName = "fppso";
                break;
            case "Факультет ППиСО":
                facultyCorrectName = "fppso";
                break;

            case "Факультет физико-математических и естественно-научных дисциплин (ПИ)":
                facultyCorrectName = "fmend";
                break;
            case "ФФМиЕНД":
                facultyCorrectName = "fmend";
                break;

            case "Факультет фундаментальной медицины и медицинских технологий":
                facultyCorrectName = "fmimt";
                break;
            case "ФФМиМТ":
                facultyCorrectName = "fmimt";
                break;

            case "Философский факультет":
                facultyCorrectName = "fp";
                break;
            case "Фак-т философии":
                facultyCorrectName = "fp";
                break;

            case "Экономический факультет":
                facultyCorrectName = "ef";
                break;
            case "Экономический фак-т":
                facultyCorrectName = "ef";
                break;

            case "Юридический факультет":
                facultyCorrectName = "uf";
                break;
            case "Юрфак":
                facultyCorrectName = "uf";
                break;

            case "Геологический колледж": // нет преподов
                facultyCorrectName = "kgl";
                break;
            case "Колледж радиоэлектроники им. П.Н. Яблочкова": // нет преподов
                facultyCorrectName = "cre";
                break;

            case "Психолого-педагогический факультет": // нет преподов
                facultyCorrectName = "bippf";
                break;

            case "Факультет математики и естественных наук": // нет преподов
                facultyCorrectName = "fmen";
                break;

            case "Филологический факультет": // нет преподов
                facultyCorrectName = "biff";
                break;

            default:
                facultyCorrectName = "bf";
                break;
        }
        return facultyCorrectName;
    }
}
