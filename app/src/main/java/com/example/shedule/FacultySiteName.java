package com.example.shedule;

public class FacultySiteName {
    public String showFacultyName(String facultyRuName){
        String facultyCorrectName;
        switch (facultyRuName){
            case "Биологический факультет":
                facultyCorrectName = "bf";
                break;
            case "Географический факультет":
                facultyCorrectName = "gf";
                break;
            case "Геологический факультет":
                facultyCorrectName = "gl";
                break;
            case "Институт искусств":
                facultyCorrectName = "ii";
                break;
            case "Институт истории и международных отношений":
                facultyCorrectName = "imo";
                break;
            case "Институт физики":
                facultyCorrectName = "ff";
                break;
            case "Институт физической культуры и спорта":
                facultyCorrectName = "ifk";
                break;
            case "Институт филологии и журналистики":
                facultyCorrectName = "ifg";
                break;
            case "Институт химии":
                facultyCorrectName = "ih";
                break;
            case "Институт дополнительного профессионального образования":
                facultyCorrectName = "idpo";
                break;
            case "Механико-математический факультет":
                facultyCorrectName = "mm";
                break;
            case "Социологический факультет":
                facultyCorrectName = "sf";
                break;
            case "Факультет иностранных языков и лингводидактики":
                facultyCorrectName = "fi";
                break;
            case "Факультет компьютерных наук и информационных технологий":
                facultyCorrectName = "knt";
                break;
            case "Факультет психологии":
                facultyCorrectName = "fps";
                break;
            case "Факультет психолого-педагогического и специального образования":
                facultyCorrectName = "fppso";
                break;
            case "Факультет фундаментальной медицины и медицинских технологий":
                facultyCorrectName = "fmimt";
                break;
            case "Философский факультет":
                facultyCorrectName = "fp";
                break;
            case "Экономический факультет":
                facultyCorrectName = "ef";
                break;
            case "Юридический факультет":
                facultyCorrectName = "uf";
                break;
            default:
                facultyCorrectName = "bf";
                break;
        }
        return facultyCorrectName;
    }
}
