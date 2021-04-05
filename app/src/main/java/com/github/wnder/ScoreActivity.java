package com.github.wnder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class ScoreActivity extends AppCompatActivity {
    private Button guessNewPicture;
    private Button seeScoreBoard;
    private Button goToMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        goToMenu = findViewById(R.id.scoreToMenuButton);
        seeScoreBoard = findViewById(R.id.scoreToScoreBoardButton);
        guessNewPicture = findViewById(R.id.scoreToGuessPictureButton);

        goToMenu.setOnClickListener((view) -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish(); 
            startActivity(intent);
        });
        seeScoreBoard.setOnClickListener((view) -> {
            Intent intent = new Intent(this, ScoreBoardActivity.class);
            startActivity(intent);
        });
        guessNewPicture.setOnClickListener((view) -> {
            Intent intent = new Intent(this, GuessPreviewActivity.class);
            startActivity(intent);
            finish();
        });
    }
}