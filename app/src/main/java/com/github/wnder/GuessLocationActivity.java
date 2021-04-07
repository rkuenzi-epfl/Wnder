package com.github.wnder;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import com.mapbox.mapboxsdk.style.layers.SymbolLayer;


public class GuessLocationActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, MapboxMap.OnMapClickListener{

    private Intent intent;
    private Bundle extras;

    //Default parameters if there  isn't an extra attached to the intend of this activity (EPFL position)
    final private double defaultGuessLat = 46.5197;
    final private double defaultGuessLng = 6.5657;
    final private double defaultCameraLat = 46.5197;
    final private double defaultCameraLng = 6.5657;

    private MapView mapView;
    private MapboxMap mapboxMap;
    private LatLng guessPosition;
    private LatLng cameraPosition;
    private GeoJsonSource geoJsonSource;
    private ValueAnimator animator;

    /*
     * MapBox gestion function
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intent = getIntent();
        if (intent != null) {
            extras = intent.getExtras();
        }
        if (extras != null) {
            guessPosition = new LatLng(extras.getDouble(GuessPreviewActivity.EXTRA_GUESSLAT), extras.getDouble(GuessPreviewActivity.EXTRA_GUESSLNG));
            cameraPosition = new LatLng(extras.getDouble(GuessPreviewActivity.EXTRA_CAMERALAT), extras.getDouble(GuessPreviewActivity.EXTRA_CAMERALNG));
        } else {
            guessPosition = new LatLng(defaultGuessLat, defaultGuessLng);
            cameraPosition = new LatLng(defaultCameraLat, defaultCameraLng);
        }

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_guess_location);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        //inisialisation of the camera position
        CameraPosition position = new CameraPosition.Builder().target(this.cameraPosition).build();
        this.mapboxMap.setCameraPosition(position);

        geoJsonSource = new GeoJsonSource("source-id", Feature.fromGeometry(
                Point.fromLngLat(guessPosition.getLongitude(), guessPosition.getLatitude())));

        mapboxMap.setStyle(Style.SATELLITE_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                style.addImage(("marker_icon"), BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_marker_icon_default));
                style.addSource(geoJsonSource);
                style.addLayer(new SymbolLayer("layer-id", "source-id")
                        .withProperties(
                                PropertyFactory.iconImage("marker_icon"),
                                PropertyFactory.iconIgnorePlacement(true),
                                PropertyFactory.iconAllowOverlap(true)
                        ));

                mapboxMap.addOnMapClickListener(GuessLocationActivity.this);
            }
        });
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        // When the user clicks on the map, we want to animate the marker to that location.
        if (animator != null && animator.isStarted()) {
            guessPosition = (LatLng) animator.getAnimatedValue();
            animator.cancel();
        }

        animator = ObjectAnimator
                .ofObject(latLngEvaluator, guessPosition, point)
                .setDuration(500);
        animator.addUpdateListener(animatorUpdateListener);
        animator.start();

        guessPosition = point;
        return true;
    }

    private final ValueAnimator.AnimatorUpdateListener animatorUpdateListener =
            new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    LatLng animatedPosition = (LatLng) valueAnimator.getAnimatedValue();
                    geoJsonSource.setGeoJson(Point.fromLngLat(animatedPosition.getLongitude(), animatedPosition.getLatitude()));
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

    /*
     * Activity button flow gestion
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirmButton:
                openGuessResultActivity();
                break;
            default:
                break;
            // Other buttons can be setup in this switch
        }
    }

    private void openGuessResultActivity() {
        //TODO Go to the Result Activity (might have to put extras to give the guessed and actual positions to the Result Activity)
        /*
         * Another and maybe better solution could be to display the result: score and actual position of the photo on this activity so that we don't have to reload the map in another activity.
         * In that case clicking on "confirm guess" could
         * 1 make the actual photo's position appear on the map with a marker and make a score appear
         * 2 change the "confirm button" to a "GuessNewImage button" that put back the user to the GuesspreviewActivity
         * 3 make appear a "scoreboard button" that could send the user to an activity where there is a full view of the scoreboard for that image
         *
         * This activity flow needs to be discussed with the tree-activity-flow Jemery is currently doing
         */
    }
}