package com.example.shedule.parser.teacher.teacherId;

public class TeacherIdCacheLoader extends Thread {
    @Override
    public void run() {
        if (!TeacherIdCache.isInitialized()) {
            TeacherIdCache.initializeCache();
        }
    }
}
