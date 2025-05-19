package com.example.shedule.parser.teacher.teacherId;

import android.content.Context;

public class TeacherIdCacheLoader extends Thread {
    private final Context context;

    public TeacherIdCacheLoader(Context context) {
        this.context = context.getApplicationContext(); // предотвращаем утечку Activity
    }

    @Override
    public void run() {
        if (!TeacherIdCache.isInitialized()) {
            TeacherIdCache.initializeCache(context);
        }
    }
}
