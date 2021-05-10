package com.github.wnder;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.wnder.user.GlobalUser;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScoreboardAdapter extends RecyclerView.Adapter<ScoreboardAdapter.ViewHolder> {
    private List<Map.Entry<String, Double>> scoreboard;

    public ScoreboardAdapter() {
        scoreboard = new ArrayList<>();
    }

    public void updateScoreboard(List<Map.Entry<String, Double>> scoreboard) {
        this.scoreboard = scoreboard;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView rank;
        private final TextView username;
        private final TextView score;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            rank = itemView.findViewById(R.id.scoreboardRank);
            username = itemView.findViewById(R.id.scoreboardUsername);
            score = itemView.findViewById(R.id.scoreboardScore);
        }
    }

    @NonNull
    @Override
    public ScoreboardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scoreboard_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map.Entry<String, Double> entry = scoreboard.get(position);

        holder.rank.setText(String.format(Locale.getDefault(),"%d", position + 1));
        holder.username.setText(entry.getKey());
        holder.score.setText(String.format(Locale.getDefault(),"%4.1f", entry.getValue()));
    }

    @Override
    public int getItemCount() {
        return scoreboard.size();
    }
}
