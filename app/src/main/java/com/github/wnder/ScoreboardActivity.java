package com.github.wnder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity that displays a scoreboard
 */
@AndroidEntryPoint
public class ScoreboardActivity extends AppCompatActivity {

    public static final String EXTRA_PICTURE_ID = "picture_id";

    private ScoreboardActivityViewModel viewModel;

    /**
     * Executes on activity creation
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Placeholder for now
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);

        findViewById(R.id.leaveScoreboardButton).setOnClickListener(id -> finish());

        viewModel = new ViewModelProvider(this).get(ScoreboardActivityViewModel.class);
        viewModel.getScoreboard().observe(this, this::updateScoreboard);
    }

    /**
     * Update the scoreboard display
     * @param scoreboard the new scoreboard to display
     */
    private void updateScoreboard(Map<String, Double> scoreboard){

        TableLayout scoreTable = findViewById(R.id.scoreTable);
        List<Map.Entry<String, Double>> scoreList = new ArrayList<>(scoreboard.entrySet());
        scoreList.sort(Map.Entry.comparingByValue());
        Collections.reverse(scoreList);
        for(Map.Entry<String, Double> e : scoreList){
            if(e.getValue() > 0){

                TableRow newRow = new TableRow(this);
                TextView userName = new TextView(this);
                userName.setText((String)e.getKey());
                TextView score = new TextView(this);
                Double value = (Double) e.getValue();
                score.setText(value.toString());
                newRow.addView(userName);
                newRow.addView(score);
                scoreTable.addView(newRow);
            }
        }
    }
}