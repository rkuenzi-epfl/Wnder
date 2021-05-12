package com.github.wnder;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.wnder.picture.ExistingPicture;
import com.github.wnder.picture.Picture;
import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.scoreboard.ScoreboardActivity;
import com.github.wnder.user.GlobalUser;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfMeta;
import com.mapbox.turf.TurfTransformation;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Detailed history for one specific history picture
 */
@AndroidEntryPoint
public class PictureHistoryActivity extends AppCompatActivity {
    public static final String EXTRA_PICTURE_ID = "picture_id";

    private String pictureID = Picture.UNINITIALIZED_ID;

    @Inject
    public PicturesDatabase picturesDb;

    /**
     * Executes on activity creation
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_history);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        pictureID = extras.getString(EXTRA_PICTURE_ID);

        ImageView image = findViewById(R.id.pictureHistoryImage);
        picturesDb.getBitmap(pictureID).thenAccept(bmp -> image.setImageBitmap(bmp));

        ImageView mapImage = findViewById(R.id.pictureHistoryMapImage);
        picturesDb.getMapSnapshot(pictureID).thenAccept(mapSnapshot -> mapImage.setImageBitmap(mapSnapshot));

        picturesDb.getUserGuess(pictureID).thenAccept(guessLocation -> {
            picturesDb.getLocation(pictureID).thenAccept(pictureLocation -> {
                double distanceFromPicture = guessLocation.distanceTo(pictureLocation);
                TextView distanceText = findViewById(R.id.pictureHistoryDistanceText);
                String dText = getString(R.string.distance_meter,(int)distanceFromPicture);
                if(distanceFromPicture > 10000){
                    dText = getString(R.string.distance_kilometer,(int)distanceFromPicture/1000);
                }
                distanceText.setText(dText);
            });
        });

        picturesDb.getScoreboard(pictureID).thenAccept(scoreboard -> {
            double score = scoreboard.get(GlobalUser.getUser().getName());
            TextView scoreText = findViewById(R.id.pictureHistoryScoreText);
            scoreText.setText(getString(R.string.score,(int)score));
        });

        Button scoreBoardButton = findViewById(R.id.pictureHistoryToScoreboardButton);
        scoreBoardButton.setOnClickListener((view) -> openScoreBoardActivity());
    }

    private void openScoreBoardActivity() {
        Intent intent = new Intent(this, ScoreboardActivity.class);
        intent.putExtra(ScoreboardActivity.EXTRA_PICTURE_ID, pictureID);
        startActivity(intent);
    }
}
