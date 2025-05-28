package com.example.shedule.parser.student;

import android.app.Activity;
import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.example.shedule.activity.student.InfoActivity;
import com.example.shedule.activity.student.MainActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class ParseInfoThread extends Thread {
    private final String faculty;
    private final Activity activity;

    public ParseInfoThread(String faculty, Activity activity) {
        this.faculty = faculty;
        this.activity = activity;
    }

    @Override
    public void run() {
        try {
            Document doc = Jsoup.connect("https://www.sgu.ru/schedule").get();

            Elements containers = doc.select(".accordion-container");

            for (Element container : containers) {
                Element title = container.selectFirst("h3.accordion__header");
                if (title != null && title.text().trim().equalsIgnoreCase(faculty)) {
                    Element info = container.selectFirst("div.schedule__info span");
                    if (info != null) {
                        String infoText = info.text().trim();

                        activity.runOnUiThread(() -> {
                            Intent intent = new Intent(activity, InfoActivity.class);
                            intent.putExtra("infoText", infoText);
                            activity.startActivity(intent);
                        });
                    }
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
