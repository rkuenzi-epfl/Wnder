package com.github.wnder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.wnder.user.GlobalUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScoreboardOwnRankAdapter extends RecyclerView.Adapter<ScoreboardOwnRankAdapter.ViewHolder> {
    private List<Map.Entry<String, Double>> scoreboardOnlyForUser;
    private int rank;

    public ScoreboardOwnRankAdapter() {
        scoreboardOnlyForUser = new ArrayList<>();
    }

    public void updateScoreboard(List<Map.Entry<String, Double>> scoreboard) {
        int count = 1;
        for(Map.Entry<String, Double> entry : scoreboard){
            if(entry.getKey().equals(GlobalUser.getUser().getName())){
                scoreboardOnlyForUser.add(entry);
                rank = count;
            }
            count++;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scoreboard_entry, parent, false);
        return new ScoreboardOwnRankAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map.Entry<String, Double> entry = scoreboardOnlyForUser.get(position);

        holder.rank.setText(String.format(Locale.getDefault(),"%d", rank));
        holder.username.setText(entry.getKey());
        holder.score.setText(String.format(Locale.getDefault(),"%4.1f", entry.getValue()));
    }

    @Override
    public int getItemCount() {
        return scoreboardOnlyForUser.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView score;

        private final TextView username;

        private final TextView rank;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);
            score = itemView.findViewById(R.id.scoreboardScore);
            username = itemView.findViewById(R.id.scoreboardUsername);
            rank = itemView.findViewById(R.id.scoreboardRank);
        }
    }
}
