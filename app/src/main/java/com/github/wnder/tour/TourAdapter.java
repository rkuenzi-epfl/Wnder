package com.github.wnder.tour;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.github.wnder.AlertBuilder;
import com.github.wnder.R;
import com.github.wnder.guessLocation.GuessLocationActivity;
import com.github.wnder.networkService.NetworkService;
import com.github.wnder.picture.Picture;
import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.User;

import java.util.ArrayList;
import java.util.List;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.ViewHolder> {

    private final List<String> tourList;
    private final PicturesDatabase picturesDb;
    private final TourDatabase tourDb;
    private final NetworkService networkService;

    public TourAdapter(List<String> tourList, PicturesDatabase picturesDb, TourDatabase tourDb, NetworkService networkService) {
        this.tourList = tourList;
        this.picturesDb = picturesDb;
        this.tourDb = tourDb;
        this.networkService = networkService;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tour_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int index) {
        Context context = holder.itemView.getContext();
        String tourId = tourList.get(index);

        tourDb.getTourPics(tourId).thenAccept(tourPics -> {
            String firstPicId = tourPics.get(0);
            picturesDb.getBitmap(firstPicId).thenAccept(bitmap -> {
                holder.getFirstPicImageView().setImageBitmap(bitmap);
            });

            int tourPicCount = tourPics.size();
            holder.getPicCountTextView().setText(String.valueOf(tourPicCount));
        });

        tourDb.getTourName(tourId).thenAccept(tourName -> {
            holder.getNameTextView().setText(tourName);
        });

        User user = GlobalUser.getUser();
        Location userLocation = user.getPositionFromGPS((LocationManager) context.getSystemService(Context.LOCATION_SERVICE), context);
        tourDb.getTourDistance(tourId, userLocation).thenAccept(tourDistance -> setDistanceTextToView(context, tourDistance, holder.getDistanceTextView()));

        tourDb.getTourLength(tourId).thenAccept(tourLength -> setDistanceTextToView(context, tourLength, holder.getLengthTextView()));

        holder.itemView.setOnClickListener(view -> {
            openGuessActivity(context, tourId);
        });
    }

    private void setDistanceTextToView(Context context, Double distance, TextView view) {
        String dText = context.getString(R.string.distance_meter, distance.intValue());
        if (distance > 10000) {
            dText = context.getString(R.string.distance_kilometer, distance.intValue() / 1000);
        }
        view.setText(dText);
    }

    private void openGuessActivity(Context context, String tourId){
        if (networkService.isNetworkAvailable()){
            Intent intent = new Intent(context, GuessLocationActivity.class);
            tourDb.getTourPics(tourId).thenAccept(list -> {
                picturesDb.getLocation(list.get(0)).thenAccept((lct) -> {
                    intent.putExtra(GuessLocationActivity.EXTRA_GUESS_MODE, R.string.guess_tour_mode);

                    Picture pictureToGuess = new Picture(list.get(0), lct.getLatitude(), lct.getLongitude());
                    intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_TO_GUESS, pictureToGuess);

                    intent.putExtra(GuessLocationActivity.EXTRA_TOUR_ID, tourId);

                    context.startActivity(intent);
                });
            });
        } else {
            AlertDialog alert = AlertBuilder.okAlert(context.getString(R.string.no_connection), context.getString(R.string.no_internet_body), context);
            alert.show();
        }
    }

    @Override
    public int getItemCount() {
        return tourList.size();
    }

    /**
     * Tour entry ViewHolder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView firstPicImageView;
        private final TextView nameTextView;
        private final TextView picCountTextView;
        private final TextView distanceTextView;
        private final TextView lengthTextView;

        public ViewHolder(View view) {
            super(view);
            firstPicImageView = view.findViewById(R.id.tourFirstPic);
            nameTextView = view.findViewById(R.id.tourName);
            picCountTextView = view.findViewById(R.id.tourPicCount);
            distanceTextView = view.findViewById(R.id.tourDistance);
            lengthTextView = view.findViewById(R.id.tourLength);
        }

        public ImageView getFirstPicImageView() {
            return firstPicImageView;
        }

        public TextView getNameTextView(){
            return nameTextView;
        }

        public TextView getPicCountTextView(){
            return picCountTextView;
        }

        public TextView getDistanceTextView(){
            return distanceTextView;
        }

        public TextView getLengthTextView(){ return lengthTextView; }
    }
}
