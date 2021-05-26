package com.github.wnder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.test.core.app.ApplicationProvider;

import com.github.wnder.guessLocation.GuessLocationActivity;
import com.github.wnder.networkService.NetworkService;
import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.tour.FirebaseTourDatabase;
import com.github.wnder.tour.TourDatabase;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.User;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TemporaryActivity extends AppCompatActivity {

    @Inject
    public NetworkService networkInfo;

    public TourDatabase tourDb;

    @Inject
    public PicturesDatabase picturesDb;

    public static final double DEFAULT_LAT = 46.5197;
    public static final double DEFAULT_LNG = 6.5657;

    private User user;
    private double pictureLat = DEFAULT_LAT;
    private double pictureLng = DEFAULT_LNG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tourDb = new FirebaseTourDatabase(this);

        setContentView(R.layout.temporary_layout);

        findViewById(R.id.temporary_button).setOnClickListener(id -> openGuessActivity());


    }

    @Override
    protected void onStart() {
        super.onStart();

        //Get user
        user = GlobalUser.getUser();

    }

    private void openGuessActivity(){
        if (networkInfo.isNetworkAvailable()){

            Intent intent = new Intent(this, GuessLocationActivity.class);
            // TODO change this to match the tour selected by the user
            String tour_id = "tourUniqueId";
            tourDb.getTourPics(tour_id).thenAccept(list -> {
                picturesDb.getLocation(list.get(0)).thenAccept((Lct) -> {
                    intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_ID, list.get(0));
                    intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_LAT, Lct.getLatitude());
                    intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_LNG, Lct.getLongitude());
                    intent.putExtra(GuessLocationActivity.EXTRA_CAMERA_LAT, user.getPositionFromGPS((LocationManager) getSystemService(Context.LOCATION_SERVICE), this).getLatitude());
                    intent.putExtra(GuessLocationActivity.EXTRA_CAMERA_LNG, user.getPositionFromGPS((LocationManager) getSystemService(Context.LOCATION_SERVICE), this).getLongitude());


                    intent.putExtra(GuessLocationActivity.TOUR_ID, tour_id);


                    startActivity(intent);
                    finish();
                });
            });

        }
        else{
            AlertDialog alert = AlertBuilder.okAlert(getString(R.string.no_connection), getString(R.string.no_internet_body), this);
            alert.show();
        }
    }
}
