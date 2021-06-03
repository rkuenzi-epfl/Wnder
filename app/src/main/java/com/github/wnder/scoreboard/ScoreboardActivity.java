package com.github.wnder.scoreboard;

import android.location.Location;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.wnder.R;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;
import com.github.wnder.user.User;
import com.github.wnder.user.UserDatabase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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

    @Inject
    public UserDatabase userDb;

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

        User user = GlobalUser.getUser();
        if(user instanceof SignedInUser){

            String pictureId = getIntent().getExtras().getString(EXTRA_PICTURE_ID);
            userDb.getGuessEntryForPicture((SignedInUser) user, pictureId).thenAccept(guessEntry -> {
                List<Map.Entry<String,Double>> scoreAsList = new ArrayList<>();
                scoreAsList.add(new AbstractMap.SimpleEntry<>(user.getName(),guessEntry.getScore()));
                adapterOwnRank.updateScoreboard(scoreAsList);
                adapterOwnRank.notifyDataSetChanged();
            });
        }

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


    }
}