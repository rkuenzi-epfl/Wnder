package com.github.wnder;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.wnder.picture.Picture;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
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

/**
 * Location activity
 */
public class GuessLocationActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener{
    //Define all necessary and recurrent strings
    public static final String EXTRA_CAMERA_LAT = "cameraLat";
    public static final String EXTRA_CAMERA_LNG = "cameraLng";
    public static final String EXTRA_PICTURE_LAT = "pictureLat";
    public static final String EXTRA_PICTURE_LNG = "pictureLng";
    public static final String EXTRA_DISTANCE = "distance";
    public static final String EXTRA_PICTURE_ID = "picture_id";

    private static final String GUESS_SOURCE_ID = "guess-source-id";
    private static final String GUESS_LAYER_ID = "guess-layer-id";
    private static final String GUESS_ICON_ID = "guess-icon-id";
    private static final String PICTURE_SOURCE_ID = "picture-source-id";
    private static final String PICTURE_LAYER_ID = "picture-layer-id";

    private static final long ANIMATION_DURATION = 200;

    //Defines necessary mapBox setup
    private MapView mapView;
    private MapboxMap mapboxMap;
    private LatLng cameraPosition;
    private LatLng guessPosition;
    private LatLng picturePosition;
    private int distance;
    private GeoJsonSource guessSource;
    private ValueAnimator animator;

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
        distance = extras.getInt(EXTRA_DISTANCE);

        //Get picture ID
        pictureID = extras.getString(EXTRA_PICTURE_ID);

        //MapBox creation
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_guess_location);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        //On confirm, show real picture location
        findViewById(R.id.confirmButton).setOnClickListener(id -> showActualLocation());
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
                .zoom(zoomFromKilometers(distance))
                .build();
        this.mapboxMap.setCameraPosition(position);

        //Get guess source
        guessSource = new GeoJsonSource(GUESS_SOURCE_ID, Feature.fromGeometry(
                Point.fromLngLat(guessPosition.getLongitude(), guessPosition.getLatitude())));

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

    /**
     * Shows the real location of the picture
     */
    private void showActualLocation() {
        //TODO make the actual picture location appear
        CameraPosition camPosition = new CameraPosition.Builder().target(cameraPosition).zoom(zoomFromKilometers(distance)).build();
        mapboxMap.setCameraPosition(camPosition);
    }

    private void drawCircle(LatLng position) {
        Point center = Point.fromLngLat(position.getLongitude(), position.getLatitude());
        Polygon outerCirclePolygon = TurfTransformation.circle(center,  distance + distance/15.0, "kilometers");
        Polygon innerCirclePolygon = TurfTransformation.circle(center, (double) distance, "kilometers");

        GeoJsonSource outerCircleSource = new GeoJsonSource(PICTURE_SOURCE_ID, outerCirclePolygon);

        if (outerCircleSource != null) {
            outerCircleSource.setGeoJson(Polygon.fromOuterInner(
                    LineString.fromLngLats(TurfMeta.coordAll(outerCirclePolygon, false)),
                    LineString.fromLngLats(TurfMeta.coordAll(innerCirclePolygon, false))
            ));
        }

        Style style = mapboxMap.getStyle();
        style.addSource(outerCircleSource);
        style.addLayer(new FillLayer(PICTURE_LAYER_ID, PICTURE_SOURCE_ID).withProperties(
                PropertyFactory.fillColor(ContextCompat.getColor(this, R.color.red)),
                PropertyFactory.fillOpacity(0.4f)
        ));
    }

    private double zoomFromKilometers(int kilometers) {
        int absLat = Math.abs((int) cameraPosition.getLatitude());

        //The latitude deformation taken care manually because of the lack of a good function (https://docs.mapbox.com/help/glossary/zoom-level/)
        double latDeformation = 0.00046*Math.pow(absLat, 2);
        double offset = 13.6 - latDeformation;

        return - Math.log((double) kilometers)/Math.log(2) + offset;
    }


}
