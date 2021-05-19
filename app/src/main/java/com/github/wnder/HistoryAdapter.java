package com.github.wnder;


import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.scoreboard.ScoreboardActivity;
import com.github.wnder.user.GlobalUser;

import java.util.ArrayList;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final ArrayList<String> pictureList;
    private final PicturesDatabase picturesDb;

    public HistoryAdapter(ArrayList<String> pictureList, PicturesDatabase picturesDatabase){
        this.pictureList = pictureList;
        this.picturesDb = picturesDatabase;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        String pictureId = pictureList.get(position);
        picturesDb.getBitmap(pictureId).thenAccept(bitmap -> holder.getHistoryImageView().setImageBitmap(bitmap));
        picturesDb.getScoreboard(pictureId).thenAccept(scoreboard -> {
            String score = String.format(Locale.getDefault(),"%4.1f", scoreboard.getOrDefault(GlobalUser.getUser().getName(), 0.));
            holder.getYourScoreView().setText(score);
        });
        picturesDb.getLocation(pictureId).thenAccept(location -> {

            picturesDb.getUserGuesses(pictureId).thenAccept(guesses -> {
                Location userGuess = guesses.get(GlobalUser.getUser().getName());
                int distanceFromPicture = 0;
                if (userGuess != null) {
                    distanceFromPicture = (int) userGuess.distanceTo(location);
                }
                String dText = context.getString(R.string.history_distance_meter, distanceFromPicture);
                if (distanceFromPicture > 10000) {
                    dText = context.getString(R.string.history_distance_kilometer, distanceFromPicture / 1000);
                }
                holder.getHistoryDistanceView().setText(dText);
            });
        });

        holder.getToMapView().setOnClickListener(distanceField -> {
            Intent intent = new Intent(context, HistoryMapActivity.class);
            intent.putExtra(HistoryMapActivity.EXTRA_PICTURE_ID, pictureId);
            context.startActivity(intent);
        });
        holder.getToScoreboardView().setOnClickListener(scoreField -> {
            Intent intent = new Intent(context, ScoreboardActivity.class);
            intent.putExtra(ScoreboardActivity.EXTRA_PICTURE_ID, pictureId);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return pictureList.size();
    }


    /**
     * History entry ViewHolder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView historyImageView;
        private final TextView yourScoreView;
        private final TextView historyDistanceView;
        private final LinearLayout toMapView;
        private final LinearLayout toScoreboardView;

        public ViewHolder(View view) {
            super(view);
            historyImageView = view.findViewById(R.id.historyImage);

            yourScoreView = view.findViewById(R.id.yourScore);
            historyDistanceView = view.findViewById(R.id.historyDistance);

            toMapView = view.findViewById(R.id.historyToMap);
            toScoreboardView = view.findViewById(R.id.historyToScoreboard);
        }

        public ImageView getHistoryImageView() {
            return historyImageView;
        }

        public TextView getYourScoreView(){
            return yourScoreView;
        }

        public TextView getHistoryDistanceView(){
            return historyDistanceView;
        }

        public LinearLayout getToMapView(){ return toMapView; }

        public LinearLayout getToScoreboardView(){ return toScoreboardView; }
    }
}
