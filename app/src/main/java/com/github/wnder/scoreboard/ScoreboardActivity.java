package com.github.wnder.scoreboard;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.wnder.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    private RecyclerView recyclerViewGlobalRankings;
    private RecyclerView recyclerViewOwnRank;

    private ScoreboardAdapter adapterGlobalRankings;
    private ScoreboardOwnRankAdapter adapterOwnRank;

    private FloatingActionButton returnButton;

    /**
     * Executes on activity creation
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Placeholder for now
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);

        viewModel = new ViewModelProvider(this).get(ScoreboardActivityViewModel.class);
        viewModel.getScoreboard().observe(this, this::updateScoreboard);

        recyclerViewGlobalRankings = findViewById(R.id.recyclerViewScoreboard);
        recyclerViewGlobalRankings.setHasFixedSize(true);
        recyclerViewGlobalRankings.setLayoutManager(new LinearLayoutManager(this));

        recyclerViewOwnRank = findViewById(R.id.recyclerViewOwnRank);
        recyclerViewOwnRank.setHasFixedSize(true);
        recyclerViewOwnRank.setLayoutManager(new LinearLayoutManager(this));

        adapterGlobalRankings = new ScoreboardAdapter();
        adapterOwnRank = new ScoreboardOwnRankAdapter();

        recyclerViewGlobalRankings.setAdapter(adapterGlobalRankings);
        recyclerViewOwnRank.setAdapter(adapterOwnRank);

        returnButton = findViewById(R.id.floatingActionButtonReturn);
        returnButton.setOnClickListener((button) -> finish());
    }

    /**
     * Update the scoreboard display
     * @param scoreboard the new scoreboard to display
     */
    private void updateScoreboard(List<Map.Entry<String, Double>> scoreboard){
        adapterGlobalRankings.updateScoreboard(scoreboard);
        adapterGlobalRankings.notifyDataSetChanged();

        adapterOwnRank.updateScoreboard(scoreboard);
        adapterOwnRank.notifyDataSetChanged();
    }
}