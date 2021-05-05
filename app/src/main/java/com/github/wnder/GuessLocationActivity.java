package com.github.wnder;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.wnder.picture.ExistingPicture;
import com.github.wnder.picture.Picture;
import com.github.wnder.user.GlobalUser;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
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

import java.util.Timer;

import static com.github.wnder.picture.ReportedPictures.addToReportedPictures;

/**
 * Location activity
 */
public class GuessLocationActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, MapboxMap.OnCameraMoveListener {
    //Define all necessary and recurrent strings
    public static final String EXTRA_CAMERA_LAT = "cameraLat";
    public static final String EXTRA_CAMERA_LNG = "cameraLng";
    public static final String EXTRA_PICTURE_LAT = "pictureLat";
    public static final String EXTRA_PICTURE_LNG = "pictureLng";
    public static final String EXTRA_PICTURE_ID = "picture_id";

    private static final String GUESS_SOURCE_ID = "guess-source-id";
    private static final String GUESS_LAYER_ID = "guess-layer-id";
    private static final String GUESS_ICON_ID = "guess-icon-id";
    private static final String PICTURE_SOURCE_ID = "picture-source-id";
    private static final String PICTURE_LAYER_ID = "picture-layer-id";
    private static final String ICONS_SOURCE_ID = "icons-source-id";
    private static final String ICONS_LAYER_ID = "icons-layer-id";

    private static final long ANIMATION_DURATION = 200;

    //Defines necessary mapBox setup
    private MapView mapView;
    private MapboxMap mapboxMap;
    private LatLng cameraPosition;
    private LatLng guessPosition;
    private LatLng picturePosition;
    private int distanceDiameter;
    private GeoJsonSource guessSource;
    private ValueAnimator animator;
    private boolean compassMode;
    private boolean mapClickOnCompassMode;
    private boolean guessConfirmed;

    private String pictureID = Picture.UNINITIALIZED_ID;


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

        //Get distance
        distanceDiameter = GlobalUser.getUser().getRadius();

        //Get picture ID
        pictureID = extras.getString(EXTRA_PICTURE_ID);

        //Starting mode
        compassMode = false;
        mapClickOnCompassMode = false;

        //MapBox creation
        guessConfirmed = false;

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_guess_location);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        //Enter and exit compass mode
        findViewById(R.id.compassMode).setOnClickListener(id -> switchMode());

        //On confirm, show real picture location
        findViewById(R.id.confirmButton).setOnClickListener(id -> showActualLocation());
        findViewById(R.id.scoreBoardButton).setOnClickListener(id -> openScoreboardActivity());
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
                .zoom(zoomFromKilometers(distanceDiameter))
                .build();
        this.mapboxMap.setCameraPosition(position);

        //Get guess source
        guessSource = new GeoJsonSource(GUESS_SOURCE_ID, Point.fromLngLat(guessPosition.getLongitude(), guessPosition.getLatitude()));

        //Set mapbox style
        mapboxMap.setStyle(Style.SATELLITE_STREETS, new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    drawCircle(cameraPosition);

                    style.addImage((GUESS_ICON_ID), BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_marker_icon_default));
                    style.addSource(guessSource);
                    style.addLayer(new SymbolLayer(GUESS_LAYER_ID, GUESS_SOURCE_ID)
                            .withProperties(
                                    PropertyFactory.iconImage(GUESS_ICON_ID),
                                    PropertyFactory.iconIgnorePlacement(true),
                                    PropertyFactory.iconAllowOverlap(true)
                            ));

                    mapboxMap.addOnMapClickListener(GuessLocationActivity.this);
                    mapboxMap.addOnCameraMoveListener(GuessLocationActivity.this);
                }
        });
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
                //Cancellation possible
                //builder.setNegativeButton(android.R.string.cancel, (DialogInterface dialog, int which) -> {});

                AlertDialog dialog = builder.create();
                dialog.show();
            }
            return true;
        }

        // When the user clicks on the map, we want to animate the marker to that location.
        if (animator != null && animator.isStarted()) {
            guessPosition = (LatLng) animator.getAnimatedValue();
            animator.cancel();
        }

        animator = ObjectAnimator
                .ofObject(latLngEvaluator, guessPosition, point)
                .setDuration(ANIMATION_DURATION);
        animator.addUpdateListener(animatorUpdateListener);
        animator.start();

        guessPosition = point;
        return true;
    }

    //Animator update listener
    private final ValueAnimator.AnimatorUpdateListener animatorUpdateListener =
            new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    LatLng animatedPosition = (LatLng) valueAnimator.getAnimatedValue();
                    guessSource.setGeoJson(Point.fromLngLat(animatedPosition.getLongitude(), animatedPosition.getLatitude()));
                }
            };

    // Class is used to interpolate the marker animation.
    private static final TypeEvaluator<LatLng> latLngEvaluator = new TypeEvaluator<LatLng>() {
        private final LatLng latLng = new LatLng();

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude() + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude() + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
    };

    /**
     * To execute when a map is Moved
     */
    @Override
    public void onCameraMove() {
        if(compassMode) updateCompassMode(); //Would be nice to find a way to know if the map is rotating instead of simply moving
    }

    /**
     *  Update the compass and the hotbar according to the mode and distance between the current guess and picture location
     */
    private void updateCompassMode(){
        if (!compassMode) {
            findViewById(R.id.compassView).setVisibility(View.INVISIBLE);
            findViewById(R.id.hotbarView).setVisibility(View.INVISIBLE);

        } else {
            if (!guessIsClose()) { //compass update
                double mapRotation = mapboxMap.getCameraPosition().bearing; //rotation of the map
                float pictureRotation = 0; //TODO rotation of the picture location from the current position
                findViewById(R.id.compassView).setRotation((float) - mapRotation + pictureRotation);

                findViewById(R.id.compassView).setVisibility(View.VISIBLE);
                findViewById(R.id.hotbarView).setVisibility(View.INVISIBLE);

            } else { //hotbar update
                //TODO update hotbar

                findViewById(R.id.compassView).setVisibility(View.INVISIBLE);
                findViewById(R.id.hotbarView).setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Return true if the guess is close enough to display the hotbar instead of the compass
     */
    private boolean guessIsClose() {
        return false; //TODO compute or hardcode a distance in which the hotbar should be displayed
    }

    /**
     * Switch between compass mode and normal mode
     */
    private void switchMode() {
        TextView compassModeButtonView = (TextView) findViewById(R.id.compassMode);

        compassMode = !compassMode;
        updateCompassMode();
        if(compassMode) {
            compassModeButtonView.setText("Exit Compass Mode");
        } else {
            compassModeButtonView.setText("Compass Mode");
        }
    }

    /**
     * Shows the real location of the picture
     */
    private void showActualLocation() {
        //Update karma after a guess
        if(!pictureID.equals(Picture.UNINITIALIZED_ID)){
            ExistingPicture pic = new ExistingPicture(pictureID);
            pic.addKarmaForGuess();
        }

        //Remove old style with only the guess source
        Style style = mapboxMap.getStyle();
        style.removeSource(guessSource);
        style.removeLayer(GUESS_LAYER_ID);

        //Features
        Feature guessFeature = Feature.fromGeometry(Point.fromLngLat(guessPosition.getLongitude(), guessPosition.getLatitude()));
        Feature pictureFeature = Feature.fromGeometry(Point.fromLngLat(picturePosition.getLongitude(), picturePosition.getLatitude()));

        //Add new style with both icons
        style.addSource(new GeoJsonSource(ICONS_SOURCE_ID,
                FeatureCollection.fromFeatures(new Feature[] {
                        guessFeature,
                        pictureFeature
                })));
        style.addLayer(new SymbolLayer(ICONS_LAYER_ID, ICONS_SOURCE_ID)
                .withProperties(
                        PropertyFactory.iconImage(GUESS_ICON_ID),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.iconAllowOverlap(true)
                ));

        //Set camera position
        CameraPosition position = new CameraPosition.Builder().target(picturePosition).zoom(14).build();
        mapboxMap.setCameraPosition(position);
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(guessPosition)
                .include(picturePosition)
                .build();
        mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,100), 200);

        findViewById(R.id.confirmButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.scoreBoardButton).setVisibility(View.VISIBLE);

        guessConfirmed = true;

        double distanceFromPicture = guessPosition.distanceTo(picturePosition);
        TextView distanceText = findViewById(R.id.distanceText);
        String dText = getString(R.string.distance_meter,(int)distanceFromPicture);
        if(distanceFromPicture > 10000){
            dText = getString(R.string.distance_kilometer,(int)distanceFromPicture/1000);
        }
        distanceText.setText(dText);

        double score = Score.calculationScore(distanceFromPicture, GlobalUser.getUser().getRadius() * 1000);
        TextView scoreText = findViewById(R.id.scoreText);
        scoreText.setText(getString(R.string.score,(int)score));
    }

    /**
     * Open the scoreboard activity
     */
    private void openScoreboardActivity() {
        Intent intent = new Intent(this, ScoreboardActivity.class);
        intent.putExtra(ScoreboardActivity.EXTRA_PICTURE_ID, pictureID);
        startActivity(intent);
    }

    /**
     * Draw the red circle that indicate where to guess
     * @param position of the center of the circle
     */
    private void drawCircle(LatLng position) {
        //Create circles
        Point center = Point.fromLngLat(position.getLongitude(), position.getLatitude());
        Polygon outerCirclePolygon = TurfTransformation.circle(center,  distanceDiameter + distanceDiameter/15.0, "kilometers");
        Polygon innerCirclePolygon = TurfTransformation.circle(center, (double) distanceDiameter, "kilometers");

        GeoJsonSource outerCircleSource = new GeoJsonSource(PICTURE_SOURCE_ID, outerCirclePolygon);

        //Create hollow circle
        if (outerCircleSource != null) {
            outerCircleSource.setGeoJson(Polygon.fromOuterInner(
                    LineString.fromLngLats(TurfMeta.coordAll(outerCirclePolygon, false)),
                    LineString.fromLngLats(TurfMeta.coordAll(innerCirclePolygon, false))
            ));
        }

        //Set mapbox style
        Style style = mapboxMap.getStyle();
        style.addSource(outerCircleSource);
        style.addLayer(new FillLayer(PICTURE_LAYER_ID, PICTURE_SOURCE_ID).withProperties(
                PropertyFactory.fillColor(ContextCompat.getColor(this, R.color.red)),
                PropertyFactory.fillOpacity(0.4f)
        ));
    }

    /**
     * Compute the zoom to use on mapbox knowing how far away at maximum we want to guess
     * @param kilometers radius of the circle in which to guess a picture location
     * @return the zoom to use on mapbox
     */
    private double zoomFromKilometers(int kilometers) {
        int absLat = Math.abs((int) cameraPosition.getLatitude());

        //The latitude deformation taken care manually because of the lack of a good function (https://docs.mapbox.com/help/glossary/zoom-level/)
        double latDeformation = 0.00046*Math.pow(absLat, 2);
        double offset = 13.6 - latDeformation;

        return - Math.log((double) kilometers)/Math.log(2) + offset;
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
