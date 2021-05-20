package com.github.wnder.guessLocation;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.github.wnder.GuessPreviewActivity;
import com.github.wnder.R;
import com.github.wnder.Score;
import com.github.wnder.Utils;
import com.github.wnder.picture.FirebasePicturesDatabase;
import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.scoreboard.ScoreboardActivity;
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
    public static final String EXTRA_CAMERA_LAT = "cameraLat";
    public static final String EXTRA_CAMERA_LNG = "cameraLng";
    public static final String EXTRA_PICTURE_LAT = "pictureLat";
    public static final String EXTRA_PICTURE_LNG = "pictureLng";
    public static final String EXTRA_PICTURE_ID = "picture_id";

    private static final int CAMERA_PADDING = 100;
    private static final long CAMERA_ANIMATION_DURATION = 200; //0.2 secondes
    private static final long GET_POSITION_FROM_GPS_PERIOD = 1000; //10 secondes

    private static final double MAX_LAT = 90;

    //Button
    private Button nextGuessButton;

    //Defines necessary mapBox setup
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
    private boolean compassMode;
    private boolean mapClickOnCompassMode;
    private boolean guessConfirmed;
    private Timer timer;
    private TimerTask updateGuessPositionFromGPS;
    private SensorManager sensorManager;
    private SensorEventListener listener;

    private ImageView littleImage;
    private ImageView bigImage;
    private GuessLocationCompass compass;

    private String pictureID = Utils.UNINITIALIZED_ID;

    private User user;
    private Context context;
    private GuessLocationZoom zoomAnimation;

    @Inject
    public PicturesDatabase picturesDb;

    /**
     * Executed on activity creation
     * @param savedInstanceState instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        //Get camera position
        double cameraLat = extras.getDouble(EXTRA_CAMERA_LAT);
        double cameraLng = extras.getDouble(EXTRA_CAMERA_LNG);
        cameraPosition = new LatLng(cameraLat, cameraLng);

        //Setup guess position
        guessPosition = new LatLng(cameraPosition);

        //Get picture position
        double pictureLat = extras.getDouble(EXTRA_PICTURE_LAT);
        double pictureLng = extras.getDouble(EXTRA_PICTURE_LNG);
        picturePosition = new LatLng(pictureLat, pictureLng);

        //Get picture ID
        pictureID = extras.getString(EXTRA_PICTURE_ID);

        //Starting mode
        compassMode = false;
        mapClickOnCompassMode = false;

        //MapBox creation
        guessConfirmed = false;

        //Sensor initialization
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                mapboxMap.setCameraPosition(GuessLocationSensor.calculateNewPosition(event, mapboxMap, picturePosition, guessPosition));
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy){}
        };

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_guess_location);

        user = GlobalUser.getUser();
        context = this;

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        //Buttons
        nextGuessButton = findViewById(R.id.backToGuessPreview);
        //Invisible at first
        nextGuessButton.setVisibility(INVISIBLE);

        nextGuessButton.setOnClickListener(id -> nextGuess());
        findViewById(R.id.compassMode).setOnClickListener(id -> compassButton());
        findViewById(R.id.confirmButton).setOnClickListener(id -> confirmButton());

        //Timer setup
        final Handler handler = new Handler();
        updateGuessPositionFromGPS = new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> {
                    Location loc = user.getPositionFromGPS((LocationManager) getSystemService(Context.LOCATION_SERVICE), GuessLocationActivity.this);
                    if (compassMode) {
                        LatLng destinationPoint = new LatLng(loc.getLatitude(), loc.getLongitude());
                        updatePositionByLineAnimation(guessSource, guessAnimator, guessPosition, destinationPoint);
                        guessPosition = updatePositionByLineAnimation(arrowSource, arrowAnimator, guessPosition, destinationPoint);
                    }
                });
            }
        };
        timer = new Timer(true);

        View hotbarView = findViewById(R.id.hotbarView);

        zoomAnimation = setupZoomAnimation();

        compass = new GuessLocationCompass(hotbarView, picturePosition);
    }

    /**
     * To call in onCreate to setup the zoom animation correctly.
     */
    private GuessLocationZoom setupZoomAnimation(){
        //Setup image preview
        //littleImage, encapsulated in littleCard, is the image shown when zoomed out
        //bigImage, encapsulated in bigCard, is the image shown when zoomed in
        CardView littleCard = findViewById(R.id.imageToGuessCard);
        CardView bigCard = findViewById(R.id.imageToGuessCardZoomedIn);
        bigCard.setVisibility(INVISIBLE);
        littleImage = findViewById(R.id.imageToGuess);
        bigImage = findViewById(R.id.imageToGuessZoomedIn);
        db.getBitmap(pictureID).thenAccept(bmp -> {
            littleImage.setImageBitmap(bmp);
            littleImage.setVisibility(VISIBLE);
            bigImage.setImageBitmap(bmp);
        });

        //Setup zoom animation
        int zoomAnimationTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        List<View> toHide = new ArrayList<>();
        toHide.add(findViewById(R.id.compassMode));
        toHide.add(findViewById(R.id.confirmButton));
        return new GuessLocationZoom(littleCard, bigCard, findViewById(R.id.guessLocationLayout), zoomAnimationTime, toHide);
    }

    /**
     * Executed when map is ready
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

        timer.scheduleAtFixedRate(updateGuessPositionFromGPS, GET_POSITION_FROM_GPS_PERIOD, GET_POSITION_FROM_GPS_PERIOD);
    }

    /**
     * To be executed when the style has loaded
     * @param style on the mapbox map
     */
    private void onStyleLoaded(Style style){
        drawCircle(GuessLocationActivity.this, mapboxMap, cameraPosition);

        addGuessToStyle(this, style, guessSource);
        addArrowToStyle(this, style, arrowSource, mapboxMap);
        addPictureToStyle(this, style, pictureSource);
    }

    /**
     * To execute when a map is clicked
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
            if(!mapClickOnCompassMode) {
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
        if(compassMode) compass.updateCompass(mapboxMap, guessPosition, compassMode);
    }


    private void enableCompassMode(){
        FloatingActionButton compassModeButtonView = findViewById(R.id.compassMode);

        List<Sensor> list = sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);

        if(list.isEmpty()){
            //We can't use the sensor, so we inform the user
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true).setTitle(R.string.SensorNotAvailableTitle).setMessage(R.string.SensorNotAvailable)
            .setPositiveButton("Ok", (DialogInterface dialog, int which) -> compassMode = false);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else{
            compass.updateCompass(mapboxMap, guessPosition, compassMode);
            sensorManager.registerListener(listener, list.get(0), SensorManager.SENSOR_DELAY_NORMAL);
            updateGuessPositionFromGPS.run();
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

        //If guess has been confirmed, confirm button becomes button leading to scoreboard
        if (guessConfirmed) {
            //Open the scoreboard activity
            Intent intent = new Intent(this, ScoreboardActivity.class);
            intent.putExtra(ScoreboardActivity.EXTRA_PICTURE_ID, pictureID);
            startActivity(intent);
            return;
        }

        if (compassMode) compassButton();
        guessConfirmed = true;

        //don't show little image anymore and disable the compass button
        findViewById(R.id.imageToGuessCard).setVisibility(INVISIBLE);
        findViewById(R.id.compassMode).setClickable(false);

        //Once guess has been confirmed, confirm button becomes button leading to scoreboard
        ((FloatingActionButton) findViewById(R.id.confirmButton)).setImageResource(R.drawable.ic_baseline_list_24);

        //Send guess and update karma
        if (!pictureID.equals(Utils.UNINITIALIZED_ID) && !(user instanceof GuestUser)) {
            Location guessedLocation = new Location("");
            guessedLocation.setLatitude(guessPosition.getLatitude());
            guessedLocation.setLongitude(guessPosition.getLongitude());
            MapBoxHelper.onMapSnapshotAvailable(this.getApplicationContext(), guessPosition, picturePosition, (mapSnapshot) -> {
                picturesDb.sendUserGuess(pictureID, user.getName(), guessedLocation, mapSnapshot);
            });
        }
        picturesDb.updateKarma(pictureID, 1);

        showActualLocation();
    }

    /**
     * Shows the real location of the picture
     */
    @SuppressLint("SetTextI18n")
    private void showActualLocation(){
        //Make the icon of the picture visible
        Style style = mapboxMap.getStyle();
        style.getLayer(String.valueOf(R.string.PICTURE_LAYER_ID)).setProperties(PropertyFactory.visibility(Property.VISIBLE));

        //Animate camera position to englobe the picture and the guess position
        double latDiff = Math.abs(guessPosition.getLatitude() - picturePosition.getLatitude());
        double latMax = Math.max(guessPosition.getLatitude(), picturePosition.getLatitude());
        double latMin = Math.min(guessPosition.getLatitude(), picturePosition.getLatitude());
        LatLng topPosition = new LatLng(Math.min(latMax + latDiff, MAX_LAT), guessPosition.getLongitude());
        LatLng downPosition = new LatLng(Math.max(latMin - latDiff, -MAX_LAT), guessPosition.getLongitude());

        LatLngBounds latLngBounds = new LatLngBounds.Builder().include(guessPosition).include(picturePosition).include(topPosition).include(downPosition).build();
        mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, CAMERA_PADDING), (int) CAMERA_ANIMATION_DURATION);

        //Set the score text
        double distanceFromPicture = guessPosition.distanceTo(picturePosition);

        String dText = getString(R.string.distance_meter,(int)distanceFromPicture);
        if(distanceFromPicture > 10000){
            dText = getString(R.string.distance_kilometer,(int)distanceFromPicture/1000);
        }

        double score = Score.calculationScore(distanceFromPicture, user.getRadius() * 1000, user.getRadius());
        TextView scoreText = findViewById(R.id.scoreText);
        scoreText.setText(getString(R.string.score, (int)score) + "\n" + dText);

        //Animate the text
        scoreText.setVisibility(VISIBLE);
        Animation score_animation = AnimationUtils.loadAnimation(this, R.anim.score_anim);

        scoreText.startAnimation(score_animation);

        //Animate the next guess button
        nextGuessButton.setVisibility(VISIBLE);
        Animation button_animation = AnimationUtils.loadAnimation(this, R.anim.next_guess_button_anim);
        nextGuessButton.startAnimation(button_animation);
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

    /**
     * Goes to the next guest preview activity
     */
    private void nextGuess(){
        Intent intent = new Intent(this, GuessPreviewActivity.class);
        startActivity(intent);
        finish();
    }
}
