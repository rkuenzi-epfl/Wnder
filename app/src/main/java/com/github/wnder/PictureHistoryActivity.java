package com.github.wnder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

/**
 * Detailed history for one specific history picture
 */
public class PictureHistoryActivity extends AppCompatActivity {
    private Button seeScoreBoard;

    /**
     * Executes on activity creation
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //For now, placeholder
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_history);
        seeScoreBoard = findViewById(R.id.pictureHistoryToScoreboardButton);
        seeScoreBoard.setOnClickListener((view) -> {
            Intent intent = new Intent(this, ScoreBoardActivity.class);
            startActivity(intent);
        });
    }
}