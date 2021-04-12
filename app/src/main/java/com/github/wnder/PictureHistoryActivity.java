package com.github.wnder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class PictureHistoryActivity extends AppCompatActivity {
    private Button seeScoreBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_history);
        seeScoreBoard = findViewById(R.id.pictureHistoryToScoreboardButton);
        seeScoreBoard.setOnClickListener((view) -> {
            Intent intent = new Intent(this, ScoreBoardActivity.class);
            startActivity(intent);
        });
    }
}