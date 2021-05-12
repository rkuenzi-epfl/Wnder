package com.github.wnder;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.user.GlobalUser;

import java.util.ArrayList;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private ArrayList<String> pictureList;
    private PicturesDatabase picturesDb;
    private Context context;

    public HistoryAdapter(Context context, ArrayList<String> pictureList, PicturesDatabase picturesDatabase){
        this.pictureList = pictureList;
        this.picturesDb = picturesDatabase;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String pictureId = pictureList.get(position);
        picturesDb.getBitmap(pictureId).thenAccept(bitmap -> holder.getHistoryImageView().setImageBitmap(bitmap));
        picturesDb.getScoreboard(pictureId).thenAccept(scoreboard -> {
            String score = String.format(Locale.getDefault(),"%4.1f", scoreboard.getOrDefault(GlobalUser.getUser().getName(), 0.));
            holder.getYourScoreView().setText(score);
        });
        if(holder.getHistoryImageView() != null){

            holder.getHistoryImageView().setOnClickListener(image -> {
                Intent intent = new Intent(context, PictureHistoryActivity.class);
                intent.putExtra(PictureHistoryActivity.EXTRA_PICTURE_ID, pictureId);
                context.startActivity(intent);
            });
        }
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

        public ViewHolder(View view) {
            super(view);
            historyImageView = view.findViewById(R.id.historyImage);

            yourScoreView = view.findViewById(R.id.yourScore);
        }

        public ImageView getHistoryImageView() {
            return historyImageView;
        }

        public TextView getYourScoreView(){
            return yourScoreView;
        }
    }
}
