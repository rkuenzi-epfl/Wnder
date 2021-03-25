package com.github.wnder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ScoreboardActivity extends AppCompatActivity {

    public static final String EXTRA_PICTURE_ID = "pictureUID";
    public static final String LOADING_FAILED = "Could not load image";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);

        // Finish activity on click (=> go back to previous activity)
        findViewById(R.id.leaveScoreboardButton).setOnClickListener(clicked -> {
            finish();
        });

        Intent intent = getIntent();
        String pictureUID = intent.getStringExtra(EXTRA_PICTURE_ID);

        try {
            // When picture is loaded, fill scoreboard
            fillScoreboard(ExistingPicture.loadExistingPicture(pictureUID).get().getScoreboard());
        } catch (Exception e) {
            // Display small error message on failure
            Snackbar.make(findViewById(R.id.scoreTable).getRootView(), LOADING_FAILED, 2000).show();
        }

    }

    private void fillScoreboard(Map<String, Object> scoreboard){

        TableLayout scoreTable = findViewById(R.id.scoreTable);

        for(Map.Entry e : scoreboard.entrySet()){
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