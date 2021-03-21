package com.github.wnder;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Map;
import java.util.TreeMap;

public class ScoreboardActivity extends AppCompatActivity {

    // Tree map should allow to order scores if I remember correctly
    private Map<String, Integer> scoreboard = new TreeMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);

        // Finish activity on click (=> go back to previous activity)
        findViewById(R.id.leaveScoreboardButton).setOnClickListener(clicked -> {
            finish();
        });

        //Somehow get the scoreboard from the image:
        // scoreboard = image.getScoreboard();

        TableLayout scoreTable = findViewById(R.id.scoreTable);

        for(Map.Entry e : scoreboard.entrySet()){
            TableRow newRow = new TableRow(this);
            TextView userName = new TextView(this);
            userName.setText((String)e.getKey());
            TextView score = new TextView(this);
            score.setText((Integer) e.getValue());
        }
    }
}