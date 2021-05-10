package com.github.wnder.scoreboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.wnder.R;
import com.github.wnder.scoreboard.ScoreboardViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScoreboardAdapter extends RecyclerView.Adapter<ScoreboardViewHolder> {
    private List<Map.Entry<String, Double>> scoreboard;

    public ScoreboardAdapter() {
        scoreboard = new ArrayList<>();
    }

    public void updateScoreboard(List<Map.Entry<String, Double>> scoreboard) {
        this.scoreboard = scoreboard;
    }


    @NonNull
    @Override
    public ScoreboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scoreboard_entry, parent, false);
        return new ScoreboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreboardViewHolder holder, int position) {
        Map.Entry<String, Double> entry = scoreboard.get(position);

        holder.getRank().setText(String.format(Locale.getDefault(),"%d", position + 1));
        holder.getUsername().setText(entry.getKey());
        holder.getScore().setText(String.format(Locale.getDefault(),"%4.1f", entry.getValue()));
    }

    @Override
    public int getItemCount() {
        return scoreboard.size();
    }
}
