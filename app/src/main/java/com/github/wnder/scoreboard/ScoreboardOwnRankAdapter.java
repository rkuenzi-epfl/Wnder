package com.github.wnder.scoreboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.wnder.R;
import com.github.wnder.user.GlobalUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScoreboardOwnRankAdapter extends RecyclerView.Adapter<ScoreboardViewHolder> {
    private List<Map.Entry<String, Double>> scoreboardOnlyForUser;
    private int rank;

    /**
     * Creates an adapter that will write to a recyclerView only the score of the user
     */
    public ScoreboardOwnRankAdapter() {
        scoreboardOnlyForUser = new ArrayList<>();
    }

    /**
     * Updates the scoreboard
     * @param scoreboard the scoreboard to show
     */
    public void updateScoreboard(List<Map.Entry<String, Double>> scoreboard) {
        int count = 1;
        for(Map.Entry<String, Double> entry : scoreboard){
            if(entry.getKey().equals(GlobalUser.getUser().getUniqueId())){
                scoreboardOnlyForUser.add(entry);
                rank = count;
            }
            count++;
        }
    }

    @NonNull
    @Override
    public ScoreboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_scoreboard_entry, parent, false);
        return new ScoreboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreboardViewHolder holder, int position) {
        Map.Entry<String, Double> entry = scoreboardOnlyForUser.get(position);

        holder.getRank().setText(String.format(Locale.getDefault(),"%d", rank));
        holder.getUsername().setText(entry.getKey());
        holder.getScore().setText(String.format(Locale.getDefault(),"%4.1f", entry.getValue()));
    }

    @Override
    public int getItemCount() {
        return scoreboardOnlyForUser.size();
    }


}
