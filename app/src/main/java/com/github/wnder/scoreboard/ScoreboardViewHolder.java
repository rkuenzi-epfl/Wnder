package com.github.wnder.scoreboard;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.wnder.R;

public class ScoreboardViewHolder extends RecyclerView.ViewHolder{

    private final TextView rank;
    private final TextView username;
    private final TextView score;

    public ScoreboardViewHolder(@NonNull View itemView) {
        super(itemView);

        rank = itemView.findViewById(R.id.scoreboardRank);
        username = itemView.findViewById(R.id.scoreboardUsername);
        score = itemView.findViewById(R.id.scoreboardScore);
    }

    /**
     * Returns the textView of the rank
     * @return a textview of the rank
     */
    public TextView getRank(){
        return rank;
    }

    /**
     * Returns the textView of the username
     * @return a textview of the username
     */
    public TextView getUsername(){
        return username;
    }

    /**
     * Returns the textView of the score
     * @return a textview of the score
     */
    public TextView getScore(){
        return score;
    }
}
