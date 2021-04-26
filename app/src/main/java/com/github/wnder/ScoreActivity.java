package com.github.wnder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

/**
 * Activity that displays scores for a given picture
 */
public class ScoreActivity extends AppCompatActivity {
    //Prepare the different buttons
    private Button guessNewPicture;
    private Button seeScoreBoard;
    private Button goToMenu;

    /**
     * Executes on activity creation
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set layout
        setContentView(R.layout.activity_score);

        //set buttons
        goToMenu = findViewById(R.id.scoreToMenuButton);
        seeScoreBoard = findViewById(R.id.scoreToScoreBoardButton);
        guessNewPicture = findViewById(R.id.scoreToGuessPictureButton);

        //What happens when you click on the menu button
        goToMenu.setOnClickListener((view) -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish(); 
            startActivity(intent);
        });

        //What happens when you click on the see scoreboard button
        seeScoreBoard.setOnClickListener((view) -> {
            Intent intent = new Intent(this, ScoreboardActivity.class);
            startActivity(intent);
        });

        //What happens when you click on the guess new picture button
        guessNewPicture.setOnClickListener((view) -> {
            Intent intent = new Intent(this, GuessPreviewActivity.class);
            startActivity(intent);
            finish();
        });
    }
}