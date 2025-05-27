package com.example.shedule.activity.student;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shedule.R;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        TextView infoTextView = findViewById(R.id.info_text_view);
        String infoText = getIntent().getStringExtra("infoText");

        infoTextView.setText(infoText != null ? infoText : "Информация не найдена.");
    }
}

