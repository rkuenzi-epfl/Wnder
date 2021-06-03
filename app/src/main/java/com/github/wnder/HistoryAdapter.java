package com.github.wnder;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.scoreboard.ScoreboardActivity;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;
import com.github.wnder.user.User;
import com.github.wnder.user.UserDatabase;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final ArrayList<String> pictureList;
    private final PicturesDatabase picturesDb;
    private final UserDatabase userDb;

    public HistoryAdapter(ArrayList<String> pictureList, PicturesDatabase picturesDatabase, UserDatabase userDatabase){
        this.pictureList = pictureList;
        this.picturesDb = picturesDatabase;
        this.userDb = userDatabase;
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
        picturesDb.getBitmap(pictureId).thenAccept(bitmap -> {
            holder.getHistoryImageView().setImageBitmap(bitmap);
            holder.getHistoryImageView().setOnClickListener(view -> showPopup(holder, bitmap, pictureId));
        });
        User user = GlobalUser.getUser();
        if(user instanceof SignedInUser){
            userDb.getGuessEntryForPicture(((SignedInUser) user),pictureId).thenAccept(guessEntry -> {
                // Set score
                String score = String.format(Locale.getDefault(),"%4.1f", guessEntry.getScore());
                holder.getYourScoreView().setText(score);

                picturesDb.getLocation(pictureId).thenAccept(realLocation -> {

                    // Set distance
                    GeoPoint userGuessGeoPoint = guessEntry.getLocation();
                    Location userGuess = new Location("");
                    userGuess.setLatitude(userGuessGeoPoint.getLatitude());
                    userGuess.setLongitude(userGuessGeoPoint.getLongitude());
                    int distanceFromPicture = 0;
                    if (userGuess != null) {
                        distanceFromPicture = (int) userGuess.distanceTo(realLocation);
                    }
                    String dText = context.getString(R.string.history_distance_meter, distanceFromPicture);
                    if (distanceFromPicture > 10000) {
                        dText = context.getString(R.string.history_distance_kilometer, distanceFromPicture / 1000);
                    }
                    holder.getHistoryDistanceView().setText(dText);
                });
            });
        }

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

    private void showPopup(ViewHolder holder, Bitmap bmp, String pictureID){
        PopupMenu popup = new PopupMenu(holder.getHistoryImageView().getContext(), holder.getHistoryImageView(), Gravity.END, R.style.popupMenu, R.style.popupMenu);
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if(id == R.id.save){
                saveToGallery(pictureID, bmp, holder);
                return true;
            }
            return false;
        });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.history_menu, popup.getMenu());
        popup.show();
    }

    private void saveToGallery(String pictureID, Bitmap bmp, ViewHolder holder){
        if(pictureID.equals(Utils.UNINITIALIZED_ID)){
            //Snack bar
            Snackbar.make(holder.getHistoryImageView(), R.string.bar_save_is_impossible, BaseTransientBottomBar.LENGTH_SHORT).show();
        }
        else{
            MediaStore.Images.Media.insertImage(holder.getHistoryImageView().getContext().getContentResolver(), bmp, pictureID, "");
            Snackbar.make(holder.getHistoryImageView(), R.string.bar_save_is_ok, BaseTransientBottomBar.LENGTH_SHORT).show();
        }
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
