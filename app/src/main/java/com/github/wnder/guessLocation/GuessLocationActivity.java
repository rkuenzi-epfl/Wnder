package com.github.wnder.guessLocation;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.github.wnder.AlertBuilder;
import com.github.wnder.GuessPreviewActivity;
import com.github.wnder.R;
import com.github.wnder.Score;
import com.github.wnder.Utils;
import com.github.wnder.picture.FirebasePicturesDatabase;
import com.github.wnder.picture.Picture;
import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.scoreboard.ScoreboardActivity;
import com.github.wnder.tour.FirebaseTourDatabase;
import com.github.wnder.tour.TourDatabase;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.GuestUser;
import com.github.wnder.user.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.github.wnder.guessLocation.MapBoxHelper.addArrowToStyle;
import static com.github.wnder.guessLocation.MapBoxHelper.addGuessToStyle;
import static com.github.wnder.guessLocation.MapBoxHelper.addPictureToStyle;
import static com.github.wnder.guessLocation.MapBoxHelper.drawCircle;
import static com.github.wnder.guessLocation.MapBoxHelper.updatePositionByLineAnimation;
import static com.github.wnder.guessLocation.MapBoxHelper.zoomFromKilometers;

/**
 * Location activity
 */
@AndroidEntryPoint
public class GuessLocationActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, MapboxMap.OnCameraMoveListener {

    @Inject
    public FirebasePicturesDatabase db;

    //Define all necessary and recurrent strings
    public static final String EXTRA_GUESS_MODE = "guess_mode";
    public static final String EXTRA_PICTURE_TO_GUESS = "picture_to_guess";
    public static final String EXTRA_PICTURE_ID = "picture_id";
    public static final String EXTRA_TOUR_ID = "tour_id";

    private static final int CAMERA_PADDING = 100;
    private static final long CAMERA_ANIMATION_DURATION = 200; //0.2 secondes
    private static final long GET_POSITION_FROM_GPS_PERIOD = 1000; //1 secondes
    private static final double MAX_LAT = 90;
    private static final double ARRIVED_DISTANCE = 20000; //meters

    //Button
    private Button nextGuessButton;

    //Mapbox setup
    private MapView mapView;
    private MapboxMap mapboxMap;
    private LatLng cameraPosition;
    private LatLng guessPosition;
    private LatLng picturePosition;
    private GeoJsonSource guessSource;
    private GeoJsonSource arrowSource;
    private GeoJsonSource pictureSource;
    private ValueAnimator guessAnimator;
    private ValueAnimator arrowAnimator;
    private int guessMode;
    private boolean guessConfirmed;
    private boolean guessPossible;
    private Picture picToGuess;
    private String tourID;
    private List<String> tourIDs;
    private int tourIndex = 0;
    private User user;

    private SensorManager sensorManager;
    private SensorEventListener listener;

    private Timer gpsTimer;
    private TimerTask gpsTimerTask;

    private boolean compassMode;
    private boolean mapClickOnCompassMode;
    private GuessLocationCompass compass;


    @Inject
    public PicturesDatabase picturesDb;

    public TourDatabase TourDb;

    /**
     * Executed on activity creation
     *
     * @param savedInstanceState instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialisation of class variables (MapBox creation)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        setContentView(R.layout.activity_guess_location);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        user = GlobalUser.getUser();

        //Get extras
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        guessMode = extras.getInt(EXTRA_GUESS_MODE);
        picToGuess = extras.getParcelable(EXTRA_PICTURE_TO_GUESS);
        if (guessMode == R.string.guess_tour_mode) {
            tourID = extras.getString(EXTRA_TOUR_ID);
            findViewById(R.id.compassMode).setVisibility(INVISIBLE);
            TourDb = new FirebaseTourDatabase(this);
            TourDb.getTourPics(tourID).thenAccept(ls -> tourIDs = ls);
        }

        double gpsLat = user.getPositionFromGPS((LocationManager) getSystemService(Context.LOCATION_SERVICE), this).getLatitude();
        double gpsLng = user.getPositionFromGPS((LocationManager) getSystemService(Context.LOCATION_SERVICE), this).getLongitude();
        cameraPosition = new LatLng(gpsLat, gpsLng);
        guessPosition = new LatLng(cameraPosition);

        picturePosition = new LatLng(picToGuess.getPicLat(), picToGuess.getPicLng());

        //Starting mode
        compassMode = false;
        mapClickOnCompassMode = false;
        guessConfirmed = false;
        guessPossible = false;

        compass = new GuessLocationCompass(findViewById(R.id.hotbarView), picturePosition);
        setupZoomAnimation();

        //Buttons
        nextGuessButton = findViewById(R.id.backToGuessPreview);
        nextGuessButton.setVisibility(INVISIBLE);

        nextGuessButton.setOnClickListener(id -> nextGuess());
        findViewById(R.id.compassMode).setOnClickListener(id -> compassButton());
        findViewById(R.id.confirmButton).setOnClickListener(id -> confirmButton());

        //Sensor initialization
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        listener = createMapSensorListener();

        //Timer setup
        gpsTimer = new Timer(true);
        gpsTimerTask = createGpsTimerTask();
    }

    /**
     * Create the TimerTask that update the location of the user with the GPS
     */
    private TimerTask createGpsTimerTask() {
        final Handler handler = new Handler();

        return new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> {
                    Location loc = user.getPositionFromGPS((LocationManager) getSystemService(Context.LOCATION_SERVICE), GuessLocationActivity.this);
                    if (compassMode) {
                        LatLng destinationPoint = new LatLng(loc.getLatitude(), loc.getLongitude());
                        updatePositionByLineAnimation(guessSource, guessAnimator, guessPosition, destinationPoint);
                        guessPosition = updatePositionByLineAnimation(arrowSource, arrowAnimator, guessPosition, destinationPoint);
                    }

                    if (guessPosition.distanceTo(picturePosition) < ARRIVED_DISTANCE) {
                        guessPossible = true;
                    } else {
                        guessPossible = false;
                    }
                });
            }
        };
    }

    /**
     * Create the sensor for the MapBox map the listen to the phone rotations
     */
    private SensorEventListener createMapSensorListener() {
        return new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                mapboxMap.setCameraPosition(GuessLocationSensor.calculateNewPosition(event, mapboxMap, picturePosition, guessPosition));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }

    /**
     * To call in onCreate to setup the zoom animation correctly.
     */
    private void setupZoomAnimation() {
        //Setup image preview
        //littleImage, encapsulated in littleCard, is the image shown when zoomed out
        //bigImage, encapsulated in bigCard, is the image shown when zoomed in
        CardView littleCard = findViewById(R.id.imageToGuessCard);
        CardView bigCard = findViewById(R.id.imageToGuessCardZoomedIn);
        bigCard.setVisibility(INVISIBLE);
        ImageView littleImage = findViewById(R.id.imageToGuess);
        ImageView bigImage = findViewById(R.id.imageToGuessZoomedIn);
        db.getBitmap(picToGuess.getUniqueId()).thenAccept(bmp -> {
            littleImage.setImageBitmap(bmp);
            littleImage.setVisibility(VISIBLE);
            bigImage.setImageBitmap(bmp);
        });

        //Setup zoom animation
        int zoomAnimationTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        List<View> toHide = new ArrayList<>();
        if (guessMode != R.string.guess_tour_mode) {
            toHide.add(findViewById(R.id.compassMode));
        }
        toHide.add(findViewById(R.id.confirmButton));
        new GuessLocationZoom(littleCard, bigCard, findViewById(R.id.guessLocationLayout), zoomAnimationTime, toHide);
    }

    /**
     * Executed when map is ready
     *
     * @param mapboxMap MapboxMap for mapbox
     */
    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        //Set camera position
        CameraPosition position = new CameraPosition.Builder()
                .target(cameraPosition)
                .zoom(zoomFromKilometers(cameraPosition, user.getRadius()))
                .build();
        this.mapboxMap.setCameraPosition(position);

        //Get guess source
        guessSource = new GeoJsonSource(String.valueOf(R.string.GUESS_SOURCE_ID), Point.fromLngLat(guessPosition.getLongitude(), guessPosition.getLatitude()));
        arrowSource = new GeoJsonSource(String.valueOf(R.string.ORANGE_ARROW_SOURCE_ID), Point.fromLngLat(guessPosition.getLongitude(), guessPosition.getLatitude()));
        pictureSource = new GeoJsonSource(String.valueOf(R.string.PICTURE_SOURCE_ID), Point.fromLngLat(picturePosition.getLongitude(), picturePosition.getLatitude()));

        //Set mapbox style
        mapboxMap.getUiSettings().setCompassEnabled(false); //Hide the default mapbox compass because we use our compass

        mapboxMap.setStyle(Style.SATELLITE_STREETS, this::onStyleLoaded);

        mapboxMap.addOnMapClickListener(GuessLocationActivity.this);
        mapboxMap.addOnCameraMoveListener(GuessLocationActivity.this);

        gpsTimer.scheduleAtFixedRate(gpsTimerTask, GET_POSITION_FROM_GPS_PERIOD, GET_POSITION_FROM_GPS_PERIOD);
    }

    /**
     * To be executed when the style has loaded
     *
     * @param style on the mapbox map
     */
    private void onStyleLoaded(Style style) {
        drawCircle(GuessLocationActivity.this, mapboxMap, cameraPosition);

        addGuessToStyle(this, style, guessSource);
        addArrowToStyle(this, style, arrowSource, mapboxMap);
        addPictureToStyle(this, style, pictureSource);

        if (guessMode == R.string.guess_tour_mode) {
            compassButton();
        }
    }

    /**
     * To execute when a map is clicked
     *
     * @param point point where the map is clicked
     * @return true
     */
    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        if (guessConfirmed) {
            return true;
        }

        //Display a pop up to explain that you cannot move the guess marker by clicking while in compass mode
        if (compassMode) {
            if (!mapClickOnCompassMode) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(true);
                builder.setTitle(R.string.mapClickOnCompassMode_confirm_title);
                builder.setMessage(R.string.mapClickOnCompassMode_confirm_message);

                //What to do when OK is pressed
                builder.setPositiveButton("Ok", (DialogInterface dialog, int which) -> mapClickOnCompassMode = true);

                AlertDialog dialog = builder.create();
                dialog.show();
            }
            return true;
        }

        updatePositionByLineAnimation(guessSource, guessAnimator, guessPosition, point);
        guessPosition = updatePositionByLineAnimation(arrowSource, arrowAnimator, guessPosition, point);
        return true;
    }

    /**
     * To execute when a map is Moved
     */
    @Override
    public void onCameraMove() {
        if (compassMode) compass.updateCompass(mapboxMap, guessPosition, compassMode);
    }

    private void enableCompassMode() {
        FloatingActionButton compassModeButtonView = findViewById(R.id.compassMode);

        List<Sensor> list = sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);

        if (list.isEmpty()) {
            //We can't use the sensor, so we inform the user
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true).setTitle(R.string.SensorNotAvailableTitle).setMessage(R.string.SensorNotAvailable)
                    .setPositiveButton("Ok", (DialogInterface dialog, int which) -> compassMode = false);
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            compass.updateCompass(mapboxMap, guessPosition, compassMode);
            sensorManager.registerListener(listener, list.get(0), SensorManager.SENSOR_DELAY_NORMAL);
            gpsTimerTask.run();
            mapboxMap.getUiSettings().setRotateGesturesEnabled(false);
            compassModeButtonView.setImageResource(R.drawable.ic_outline_explore_24);
        }
    }

    /**
     * Switch between compass mode and normal mode
     */
    private void compassButton() {
        //If the map didn't load yet do not switch mode
        if (mapboxMap.getStyle() == null) {
            return;
        }

        //If guess hasn't been confirmed, then just switch mode
        //if(!guessConfirmed) { compassMode = !compassMode; }
        compassMode = !compassMode;

        //if we're now in compass mode, enable it and switch icon
        if (compassMode) {
            enableCompassMode();
            ((FloatingActionButton) findViewById(R.id.compassMode)).setImageResource(R.drawable.ic_outline_explore_24);
        }
        //else, update it and disable the map click
        else {
            compass.updateCompass(mapboxMap, guessPosition, compassMode);
            sensorManager.unregisterListener(listener);
            mapboxMap.getUiSettings().setRotateGesturesEnabled(true);
            ((FloatingActionButton) findViewById(R.id.compassMode)).setImageResource(R.drawable.ic_outline_explore_off_24);
        }
    }

    /**
     * Confirm button
     */
    private void confirmButton() {
        //If the map style didn't load yet, wait.
        if (mapboxMap.getStyle() == null) {
            return;
        }

        if (guessMode == R.string.guess_tour_mode) {
            confirmTourMode();
        } else {
            confirmSimpleMode();
        }
    }

    private void confirmSimpleMode() {
        //If guess has been confirmed, confirm button becomes button leading to scoreboard
        if (guessConfirmed) {
            startScoreBoard();
            return;
        }

        if (compassMode) {
            compassButton();
        }
        guessConfirmed = true;

        //don't show little image anymore and disable the compass button
        findViewById(R.id.imageToGuessCard).setVisibility(INVISIBLE);
        findViewById(R.id.compassMode).setClickable(false);
        findViewById(R.id.compassMode).setVisibility(INVISIBLE);

        //Once guess has been confirmed, confirm button becomes button leading to scoreboard
        ((FloatingActionButton) findViewById(R.id.confirmButton)).setImageResource(R.drawable.ic_baseline_list_24);

        //Send guess and update karma
        if (!picToGuess.getUniqueId().equals(Utils.UNINITIALIZED_ID) && !(user instanceof GuestUser)) {
            Location guessedLocation = new Location("");
            guessedLocation.setLatitude(guessPosition.getLatitude());
            guessedLocation.setLongitude(guessPosition.getLongitude());
            MapBoxHelper.onMapSnapshotAvailable(this.getApplicationContext(), guessPosition, picturePosition, (mapSnapshot) -> {
                picturesDb.sendUserGuess(picToGuess.getUniqueId(), user.getName(), guessedLocation, mapSnapshot);
            });
        }
        picturesDb.updateKarma(picToGuess.getUniqueId(), 1);

        showActualLocation(computeScoreText(), true);

        //Animate the next guess button
        nextGuessButton.setVisibility(VISIBLE);
        Animation button_animation = AnimationUtils.loadAnimation(this, R.anim.next_guess_button_anim);
        nextGuessButton.startAnimation(button_animation);
    }

    /**
     * Goes to the next guest preview activity
     */
    private void nextGuess() {
        Intent intent = new Intent(this, GuessPreviewActivity.class);
        startActivity(intent);
        finish();
    }

    private void confirmTourMode() {
        TextView scoreText = findViewById(R.id.scoreText);

        if (!guessPossible) {
            //Show pop up warning about skipping the picture
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle(R.string.tour_mode_confirm_while_far_title);
            builder.setMessage(R.string.tour_mode_confirm_while_far_text);

            //What to do when OK is pressed
            builder.setPositiveButton("Yes", (DialogInterface dialog, int which) -> {
                tourIndex += 1;
                if (tourIndex >= tourIDs.size()) {
                    guessPossibleConfirmTourMode();
                } else {
                    nextGuessTourMode();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        } else {
            guessPossibleConfirmTourMode();
        }
    }

    private void guessPossibleConfirmTourMode() {
        tourIndex += 1;

        //If guess has been confirmed, go to next guess or end the tour
        if (guessConfirmed) {
            nextGuessTourMode();
            return;
        }

        guessConfirmed = true;

        String animatedText = "" + tourIndex + "/" + tourIDs.size() + "\nPictures found";

        //If it was the last picture replace the confirm button by the next guess button
        if (tourIndex >= tourIDs.size()) {
            findViewById(R.id.confirmButton).setVisibility(INVISIBLE);

            //Animate the next guess button
            nextGuessButton.setVisibility(VISIBLE);
            Animation button_animation = AnimationUtils.loadAnimation(this, R.anim.next_guess_button_anim);
            nextGuessButton.startAnimation(button_animation);

            animatedText = "Tour finished.\nWell done!";
        }

        findViewById(R.id.imageToGuessCard).setVisibility(INVISIBLE);

        //Animate MapBox
        showActualLocation(animatedText, false);
    }

    private void nextGuessTourMode() {
        //Make the confirm button unavailable during the change/download of the next image
        findViewById(R.id.confirmButton).setClickable(false);

        guessConfirmed = false;
        guessPossible = false;

        //Animate the text
        TextView scoreText = findViewById(R.id.scoreText);
        scoreText.setVisibility(VISIBLE);
        Animation invisible_score_animation = AnimationUtils.loadAnimation(this, R.anim.invisible_score_anim);
        scoreText.startAnimation(invisible_score_animation);

        //Update little image and show it
        picturesDb.getBitmap(tourIDs.get(tourIndex)).thenAccept(bmp -> {
            picturesDb.getLocation(tourIDs.get(tourIndex)).thenAccept(loc -> {
                picturePosition = new LatLng(loc.getLatitude(), loc.getLongitude());
                ((ImageView) (findViewById(R.id.imageToGuessZoomedIn))).setImageBitmap(bmp);

                //Update compass
                compass = new GuessLocationCompass(findViewById(R.id.hotbarView), picturePosition);
                compass.updateCompass(mapboxMap, guessPosition, true);

                //Make the confirm button available again
                findViewById(R.id.confirmButton).setClickable(true);
            });
        });

        findViewById(R.id.imageToGuessCard).setVisibility(VISIBLE);



        //????
        /*
        pictureSource = new GeoJsonSource(String.valueOf(R.string.PICTURE_SOURCE_ID), Point.fromLngLat(picturePosition.getLongitude(), picturePosition.getLatitude()));
        addPictureToStyle(this, mapboxMap.getStyle(), pictureSource);
        mapboxMap.getStyle().getLayer(String.valueOf(R.string.PICTURE_LAYER_ID)).setProperties(PropertyFactory.visibility(Property.NONE));
         */
    }

    /**
     * Shows the real location of the picture
     */
    @SuppressLint("SetTextI18n")
    private void showActualLocation(String animatedBoardText, Boolean pictureIconVisibility) {
        //Make the icon of the picture visible
        if (pictureIconVisibility) {
            Style style = mapboxMap.getStyle();
            style.getLayer(String.valueOf(R.string.PICTURE_LAYER_ID)).setProperties(PropertyFactory.visibility(Property.VISIBLE));
        }

        //Animate camera position to englobe the picture and the guess position
        double latDiff = Math.abs(guessPosition.getLatitude() - picturePosition.getLatitude());
        double latMax = Math.max(guessPosition.getLatitude(), picturePosition.getLatitude());
        double latMin = Math.min(guessPosition.getLatitude(), picturePosition.getLatitude());
        LatLng topPosition = new LatLng(Math.min(latMax + latDiff, MAX_LAT), guessPosition.getLongitude());
        LatLng downPosition = new LatLng(Math.max(latMin - latDiff, -MAX_LAT), guessPosition.getLongitude());

        LatLngBounds latLngBounds = new LatLngBounds.Builder().include(guessPosition).include(picturePosition).include(topPosition).include(downPosition).build();
        mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, CAMERA_PADDING), (int) CAMERA_ANIMATION_DURATION);

        //Animate the text
        TextView scoreText = findViewById(R.id.scoreText);
        scoreText.setText(animatedBoardText);
        scoreText.setVisibility(VISIBLE);
        Animation score_animation = AnimationUtils.loadAnimation(this, R.anim.score_anim);
        scoreText.startAnimation(score_animation);
    }

    private String computeScoreText() {
        //Set the score text
        double distanceFromPicture = guessPosition.distanceTo(picturePosition);

        String dText = getString(R.string.distance_meter, (int) distanceFromPicture);
        if (distanceFromPicture > 10000) {
            dText = getString(R.string.distance_kilometer, (int) distanceFromPicture / 1000);
        }

        double score = Score.calculationScore(distanceFromPicture, user.getRadius() * 1000, user.getRadius());
        TextView scoreText = findViewById(R.id.scoreText);
        return getString(R.string.score, (int) score) + "\n" + dText;
    }

    /**
     * Start the ScoreBoard activity
     */
    private void startScoreBoard() {
        Intent intent = new Intent(this, ScoreboardActivity.class);
        intent.putExtra(ScoreboardActivity.EXTRA_PICTURE_ID,picToGuess.getUniqueId());
        startActivity(intent);

    }

    //Necessary overwrites for MapView lifecycle methods
    /**
     * start mapbox
     */
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    /**
     * resume mapbox
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * pause mapbox
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * stop mapbox
     */
    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    /**
     * save mapbox instance state
     * @param outState output
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * when memory is low
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    /**
     * on mapbox destruction
     */
    @Override
    protected void onDestroy() {
        //unregister listener!
        sensorManager.unregisterListener(listener);
        super.onDestroy();
        mapView.onDestroy();
    }
}
