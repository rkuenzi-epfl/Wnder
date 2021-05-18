package com.github.wnder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
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
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.github.wnder.picture.FirebasePicturesDatabase;
import com.github.wnder.picture.InternalCachePictureDatabase;
import com.github.wnder.picture.Picture;
import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.scoreboard.ScoreboardActivity;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.UserDatabase;
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
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.github.wnder.MapBoxHelper.drawCircle;
import static com.github.wnder.MapBoxHelper.updatePositionByLineAnimation;
import static com.github.wnder.MapBoxHelper.zoomFromKilometers;

/**
 * Location activity
 */
@AndroidEntryPoint
public class GuessLocationActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, MapboxMap.OnCameraMoveListener {

    @Inject
    public FirebasePicturesDatabase db;

    private int zoomAnimationTime;

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
    private CardView littleCard;
    private CardView bigCard;

    private String pictureID = Picture.UNINITIALIZED_ID;

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
                float[] vectorPosition = event.values;
                float[] quat = new float[4];
                float[] rotMat = new float[9];
                float[] result = new float[3];
                SensorManager.getQuaternionFromVector(quat, vectorPosition);
                SensorManager.getRotationMatrixFromVector(rotMat, quat);
                SensorManager.getOrientation(rotMat, result);

                double guessY = mapboxMap.getProjection().getProjectedMetersForLatLng(guessPosition).getNorthing();
                double guessX = mapboxMap.getProjection().getProjectedMetersForLatLng(guessPosition).getEasting();

                double imageY = mapboxMap.getProjection().getProjectedMetersForLatLng(picturePosition).getNorthing();
                double imageX = mapboxMap.getProjection().getProjectedMetersForLatLng(picturePosition).getEasting();

                double vectorX = imageX - guessX;
                double vectorY = imageY - guessY;
                double vectorLength = Math.sqrt(vectorX*vectorX + vectorY*vectorY);

                //This is the vector going from the position of the phone to a random position in the north
                double vectorReferenceX = 0;
                double vectorReferenceY = 5;
                double vectorReferenceLength = Math.sqrt(vectorReferenceX*vectorReferenceX + vectorReferenceY*vectorReferenceY);

                double numerator = (vectorX*vectorReferenceX + vectorY*vectorReferenceY);
                double denominator = vectorLength*vectorReferenceLength;

                double cosValue = numerator / denominator;

                float angleFromNorthToImagePosition = (float) (Math.acos(cosValue)*180/Math.PI);
                angleFromNorthToImagePosition = vectorX < 0 ? -angleFromNorthToImagePosition : angleFromNorthToImagePosition; //To take account of the sign

                float angleAroundZ = (float) (result[2]*180/Math.PI + 180); //Value of the sensor

                SymbolLayer layer = (SymbolLayer) mapboxMap.getStyle().getLayer(String.valueOf(R.string.ORANGE_ARROW_LAYER_ID));
                layer.setProperties(PropertyFactory.iconRotate(angleFromNorthToImagePosition - angleAroundZ)); //Arrow angle

                CameraPosition position = new CameraPosition.Builder()
                        .target(guessPosition)
                        .bearing(angleAroundZ)
                        .build();
                mapboxMap.setCameraPosition(position);
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
        findViewById(R.id.compassMode).setOnClickListener(id -> switchMode());
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

        //Setup image preview
        //littleImage, encapsulated in littleCard, is the image shown when zoomed out
        //bigImage, encapsulated in bigCard, is the image shown when zoomed in
        littleCard = findViewById(R.id.imageToGuessCard);
        bigCard = findViewById(R.id.imageToGuessCardZoomedIn);
        bigCard.setVisibility(INVISIBLE);
        littleImage = findViewById(R.id.imageToGuess);
        bigImage = findViewById(R.id.imageToGuessZoomedIn);
        db.getBitmap(pictureID).thenAccept(bmp -> {
            littleImage.setImageBitmap(bmp);
            littleImage.setVisibility(VISIBLE);
            bigImage.setImageBitmap(bmp);
        });
        //Setup zoom animation
        zoomAnimationTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        List<View> toHide = new ArrayList<>();
        toHide.add(findViewById(R.id.compassMode));
        toHide.add(findViewById(R.id.confirmButton));
        zoomAnimation = new GuessLocationZoom(littleCard, bigCard, findViewById(R.id.guessLocationLayout), zoomAnimationTime, toHide);

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
        mapboxMap.setStyle(Style.SATELLITE_STREETS, style -> onStyleLoaded(style));
    }

    /**
     * To be executed when the style has loaded
     * @param style on the mapbox map
     */
    private void onStyleLoaded(Style style){
        mapboxMap.getUiSettings().setCompassEnabled(false); //Hide the default mapbox compass because we use our compass

        drawCircle(GuessLocationActivity.this, mapboxMap, cameraPosition);

        style.addImage((String.valueOf(R.string.GUESS_ICON_ID)), BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_marker_icon_20px_red));
        style.addSource(guessSource);
        style.addLayer(new SymbolLayer(String.valueOf(R.string.GUESS_LAYER_ID), String.valueOf(R.string.GUESS_SOURCE_ID))
                .withProperties(
                        PropertyFactory.iconImage(String.valueOf(R.string.GUESS_ICON_ID)),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.iconAllowOverlap(true)
                ));

        style.addImage((String.valueOf(R.string.ORANGE_ARROW_ICON_ID)), BitmapFactory.decodeResource(getResources(), R.drawable.fleche_orange));
        style.addSource(arrowSource);
        style.addLayer(new SymbolLayer(String.valueOf(R.string.ORANGE_ARROW_LAYER_ID), String.valueOf(R.string.ORANGE_ARROW_SOURCE_ID))
                .withProperties(
                        PropertyFactory.visibility(Property.NONE),
                        PropertyFactory.iconImage(String.valueOf(R.string.ORANGE_ARROW_ICON_ID)),
                        PropertyFactory.iconRotate((float) mapboxMap.getCameraPosition().bearing),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.iconAllowOverlap(true)
                ));

        style.addImage((String.valueOf(R.string.PICTURE_ICON_ID)), BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_marker_icon_20px_purple));
        style.addSource(pictureSource);
        style.addLayer(new SymbolLayer(String.valueOf(R.string.PICTURE_LAYER_ID), String.valueOf(R.string.PICTURE_SOURCE_ID))
                .withProperties(
                        PropertyFactory.visibility(Property.NONE),
                        PropertyFactory.iconImage(String.valueOf(R.string.PICTURE_ICON_ID)),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.iconAllowOverlap(true)
                ));

        mapboxMap.addOnMapClickListener(GuessLocationActivity.this);
        mapboxMap.addOnCameraMoveListener(GuessLocationActivity.this);

        timer.scheduleAtFixedRate(updateGuessPositionFromGPS, GET_POSITION_FROM_GPS_PERIOD, GET_POSITION_FROM_GPS_PERIOD);
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
                builder.setPositiveButton("Ok", (DialogInterface dialog, int which) -> {
                    mapClickOnCompassMode = true;
                });

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
        if(compassMode) updateCompassMode();
    }

    /**
     *  Update the compass and the hotbar according to the mode and the distance between the current guess and picture location
     */
    private void updateCompassMode(){
        SymbolLayer layer = (SymbolLayer) mapboxMap.getStyle().getLayer(String.valueOf(R.string.ORANGE_ARROW_LAYER_ID));
        View hotbarView = findViewById(R.id.hotbarView);
        //Arbitrary value based on the radius to check if we are close enough
        double referenceDistance = user.getRadius() * 1000 / 100;

        if (!compassMode) {
            layer.setProperties(PropertyFactory.visibility(Property.NONE));
            hotbarView.setVisibility(INVISIBLE);

        } else {
            double distanceDiff = guessPosition.distanceTo(picturePosition);
            if (referenceDistance < distanceDiff) { //compass update
                layer.setProperties(PropertyFactory.visibility(Property.VISIBLE));
                hotbarView.setVisibility(INVISIBLE);

            } else { //hotbar update
                layer.setProperties(PropertyFactory.visibility(Property.NONE));
                hotbarView.setVisibility(VISIBLE);

                double ratio = distanceDiff / referenceDistance;

                ProgressBar bar = (ProgressBar) hotbarView;
                int barValue = (int) (bar.getMax() - (ratio * bar.getMax()));
                MapBoxHelper.setHotBarColor(bar, barValue);
            }
        }
    }

    private void compassModeSetup(){
        FloatingActionButton compassModeButtonView = findViewById(R.id.compassMode);

        List<Sensor> list = sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);

        if(list.isEmpty()){
            //We can't use the sensor, so we inform the user
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true).setTitle(R.string.SensorNotAvailableTitle).setMessage(R.string.SensorNotAvailable)
            .setPositiveButton("Ok", (DialogInterface dialog, int which) -> {
                compassMode = false;
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else{
            updateCompassMode();
            sensorManager.registerListener(listener, (Sensor) list.get(0), SensorManager.SENSOR_DELAY_NORMAL);
            updateGuessPositionFromGPS.run();
            mapboxMap.getUiSettings().setRotateGesturesEnabled(false);
            compassModeButtonView.setForeground(ContextCompat.getDrawable(context, R.drawable.ic_outline_explore_24));
        }
    }

    /**
     * Switch between compass mode and normal mode
     */
    private void switchMode() {
        //If the guess has already been done or that the map didn't load yet do not switch mode
        if (guessConfirmed || mapboxMap.getStyle() == null) {
            return;
        }

        View compassModeButtonView = findViewById(R.id.compassMode);

        compassMode = !compassMode;
        if(compassMode) {
            compassModeSetup();
        } else {
            updateCompassMode();
            sensorManager.unregisterListener(listener);
            mapboxMap.getUiSettings().setRotateGesturesEnabled(true);
            compassModeButtonView.setForeground(ContextCompat.getDrawable(context, R.drawable.ic_outline_explore_off_24));
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

        if (!guessConfirmed) {
            if (compassMode) switchMode();
            guessConfirmed = true;

            //don't show little image anymore
            findViewById(R.id.imageToGuessCard).setVisibility(INVISIBLE);

            findViewById(R.id.compassMode).setVisibility(INVISIBLE);
            View confirmButtonView = findViewById(R.id.confirmButton);
            confirmButtonView.setForeground(ContextCompat.getDrawable(context, R.drawable.ic_baseline_military_tech_24));

            //Send guess and update karma
            if(!pictureID.equals(Picture.UNINITIALIZED_ID) && !(user instanceof GuestUser)){
                Location guessedLocation = new Location("");
                guessedLocation.setLatitude(guessPosition.getLatitude());
                guessedLocation.setLongitude(guessPosition.getLongitude());
                picturesDb.sendUserGuess(pictureID, user.getName(), guessedLocation);
            }
            picturesDb.updateKarma(pictureID, 1);

            showActualLocation();
        } else {
            //Open the scoreboard activity
            Intent intent = new Intent(this, ScoreboardActivity.class);
            intent.putExtra(ScoreboardActivity.EXTRA_PICTURE_ID, pictureID);
            startActivity(intent);
        }
    }

    /**
     * Shows the real location of the picture
     */
    @SuppressLint("SetTextI18n")
    private void showActualLocation() {
        //Make the icon of the picture visible
        Style style = mapboxMap.getStyle();
        style.getLayer(String.valueOf(R.string.PICTURE_LAYER_ID)).setProperties(PropertyFactory.visibility(Property.VISIBLE));

        //Animate camera position to englobe the picture and the guess position
        double latDiff = Math.abs(guessPosition.getLatitude() - picturePosition.getLatitude());
        double latMax = Math.max(guessPosition.getLatitude(), picturePosition.getLatitude());
        double latMin = Math.min(guessPosition.getLatitude(), picturePosition.getLatitude());
        LatLng topPosition = new LatLng(latMax + latDiff > MAX_LAT ? MAX_LAT : latMax + latDiff, guessPosition.getLongitude());
        LatLng downPosition = new LatLng(latMin - latDiff < -MAX_LAT ? -MAX_LAT : latMin - latDiff, guessPosition.getLongitude());

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
    protected void onSaveInstanceState(Bundle outState) {
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
        super.onDestroy();
        mapView.onDestroy();
    }
}
