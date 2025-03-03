package com.example.shedule;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Spinner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class LoadSessionTask extends AsyncTask<Void, Void, Void> {
    private Spinner facultySpinner;
    private Spinner groupSpinner;
    private Document savedSessionDoc;
    private MainActivity mainActivity;
    private FacultySiteName facultySiteName;

    public LoadSessionTask(Spinner facultySpinner, Spinner groupSpinner, Document savedSessionDoc, MainActivity mainActivity) {
        this.facultySpinner = facultySpinner;
        this.groupSpinner = groupSpinner;
        this.savedSessionDoc = savedSessionDoc;
        this.mainActivity = mainActivity;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        // Выполняем сетевую операцию здесь
        String faculty = facultySpinner.getSelectedItem().toString();
        String group = groupSpinner.getSelectedItem().toString();
        String facultyUrl = facultySiteName.showFacultyName(faculty);
        String sessionUrl = "https://www.old.sgu.ru/schedule/" + facultyUrl + "/do/" + group + "#session";
        Document sessionDoc;
        try {
            if (savedSessionDoc == null || savedSessionDoc.text().length() == 0)
                sessionDoc = Jsoup.connect(sessionUrl).get();
            else
                sessionDoc = savedSessionDoc;
            if ((savedSessionDoc == null || savedSessionDoc.text().length() == 0) && sessionDoc != null)
                savedSessionDoc = sessionDoc;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //new MainActivity.ParseSessionThread(sessionDoc).start();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        // Обновляем пользовательский интерфейс здесь
        mainActivity.showSessionLayout();

    }
}

