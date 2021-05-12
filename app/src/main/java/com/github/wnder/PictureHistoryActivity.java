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
public class PictureHistoryActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String EXTRA_PICTURE_ID = "picture_id";

    private static final String SOURCE_ID_SUFFIX = "-source-id";
    private static final String ICON_ID_SUFFIX = "-icon-id";
    private static final String LAYER_ID_SUFFIX = "-layer-id";
    private static final String GUESS_PREFIX = "guess";
    private static final String PICTURE_PREFIX = "picture";
    private static final String USER_CIRCLE_SOURCE_ID = "user-circle-source-id";
    private static final String USER_CIRCLE_LAYER_ID = "user-circle-layer-id";
    private static final String PICTURE_CIRCLE_SOURCE_ID = "picture-circle-source-id";
    private static final String PICTURE_CIRCLE_LAYER_ID = "picture-circle-layer-id";

    private MapView mapView;
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

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        pictureID = extras.getString(EXTRA_PICTURE_ID);

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_picture_history);

        ImageView image = findViewById(R.id.pictureHistoryImage);
        picturesDb.getBitmap(pictureID).thenAccept(bmp -> image.setImageBitmap(bmp));


        mapView = findViewById(R.id.pictureHistoryMap);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        Button seeScoreBoard = findViewById(R.id.pictureHistoryToScoreboardButton);
        seeScoreBoard.setOnClickListener((view) -> openScoreBoardActivity());
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        ExistingPicture picture = new ExistingPicture(pictureID);
        String user = GlobalUser.getUser().getName();
        picture.onUserPositionAvailable(user, (userLocation) -> {
            picture.onUserGuessAvailable(user, (guessLocation) -> {
                picture.onLocationAvailable((pictureLocation) -> {
                    picture.onUserRadiusAvailable(user, (radius) -> {
                        setupMap(mapboxMap, userLocation, guessLocation, pictureLocation, radius);
                        setupDistanceAndScoreText(guessLocation, pictureLocation, radius);
                    });
                });
            });
        });
    }

    private void setupMap(MapboxMap mapboxMap, Location userLocation, Location guessLocation, Location pictureLocation, int radius) {
        LatLng guessLatLng = new LatLng(guessLocation.getLatitude(), guessLocation.getLongitude());
        LatLng pictureLatLng = new LatLng(pictureLocation.getLatitude(), pictureLocation.getLongitude());

        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(guessLatLng)
                .include(pictureLatLng)
                .build();
        mapboxMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100));

        mapboxMap.setStyle(Style.SATELLITE_STREETS, (style) -> {
            addIconToStyle(style, guessLatLng, GUESS_PREFIX);
            addIconToStyle(style, pictureLatLng, PICTURE_PREFIX);

            Point point = Point.fromLngLat(pictureLatLng.getLongitude(), pictureLatLng.getLatitude());
            Polygon circle = TurfTransformation.circle(point, 200, "meters");
            GeoJsonSource pictureSource = new GeoJsonSource(PICTURE_CIRCLE_SOURCE_ID, circle);

            style.addSource(pictureSource);
            style.addLayer(new FillLayer(PICTURE_CIRCLE_LAYER_ID, PICTURE_CIRCLE_SOURCE_ID).withProperties(
                    PropertyFactory.fillColor("#ff0000"),
                    PropertyFactory.fillOpacity(0.4f)
            ));

            LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            drawCircle(style, userLatLng, radius);
        });
    }

    private void addIconToStyle(Style style, LatLng position, String prefix) {
        GeoJsonSource source = new GeoJsonSource(prefix + SOURCE_ID_SUFFIX,
                Point.fromLngLat(position.getLongitude(), position.getLatitude()));

        style.addImage(prefix + ICON_ID_SUFFIX, BitmapFactory.decodeResource(
                getResources(), R.drawable.mapbox_marker_icon_default));
        style.addSource(source);
        style.addLayer(new SymbolLayer(prefix + LAYER_ID_SUFFIX, prefix + SOURCE_ID_SUFFIX)
                .withProperties(
                        PropertyFactory.iconImage(prefix + ICON_ID_SUFFIX),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.iconAllowOverlap(true)
                ));
    }

    private void drawCircle(Style style, LatLng position, int radius) {
        //Create circles
        Point center = Point.fromLngLat(position.getLongitude(), position.getLatitude());
        Polygon outerCirclePolygon = TurfTransformation.circle(center,  radius + radius/15.0, "kilometers");
        Polygon innerCirclePolygon = TurfTransformation.circle(center, (double) radius, "kilometers");

        GeoJsonSource outerCircleSource = new GeoJsonSource(USER_CIRCLE_SOURCE_ID, outerCirclePolygon);

        //Create hollow circle
        if (outerCircleSource != null) {
            outerCircleSource.setGeoJson(Polygon.fromOuterInner(
                    LineString.fromLngLats(TurfMeta.coordAll(outerCirclePolygon, false)),
                    LineString.fromLngLats(TurfMeta.coordAll(innerCirclePolygon, false))
            ));
        }

        //Set mapbox style
        style.addSource(outerCircleSource);
        style.addLayer(new FillLayer(USER_CIRCLE_LAYER_ID, USER_CIRCLE_SOURCE_ID).withProperties(
                PropertyFactory.fillColor(ContextCompat.getColor(this, R.color.red)),
                PropertyFactory.fillOpacity(0.4f)
        ));
    }

    private void setupDistanceAndScoreText(Location guessLocation, Location pictureLocation, int radius) {
        double distanceFromPicture = guessLocation.distanceTo(pictureLocation);
        TextView distanceText = findViewById(R.id.pictureHistoryDistanceText);
        String dText = getString(R.string.distance_meter,(int)distanceFromPicture);
        if(distanceFromPicture > 10000){
            dText = getString(R.string.distance_kilometer,(int)distanceFromPicture/1000);
        }
        distanceText.setText(dText);

        double score = Score.calculationScore(distanceFromPicture, radius * 1000);
        TextView scoreText = findViewById(R.id.pictureHistoryScoreText);
        scoreText.setText(getString(R.string.score,(int)score));
    }

    // Necessary overwrites for MapView lifecycle methods

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    private void openScoreBoardActivity() {
        Intent intent = new Intent(this, ScoreboardActivity.class);
        intent.putExtra(ScoreboardActivity.EXTRA_PICTURE_ID, pictureID);
        startActivity(intent);
    }
}
