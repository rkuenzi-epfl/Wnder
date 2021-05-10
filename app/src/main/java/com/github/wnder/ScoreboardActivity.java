package com.github.wnder;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private ScoreboardAdapter adapter;
    private RecyclerView recyclerView;

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

        recyclerView = findViewById(R.id.recyclerViewScoreboard);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ScoreboardAdapter();

        recyclerView.setAdapter(adapter);
    }

    /**
     * Update the scoreboard display
     * @param scoreboard the new scoreboard to display
     */
    private void updateScoreboard(List<Map.Entry<String, Double>> scoreboard){
        adapter.updateScoreboard(scoreboard);
        adapter.notifyDataSetChanged();
    }
}