package com.github.wnder;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.github.wnder.user.GlobalUser;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfMeta;
import com.mapbox.turf.TurfTransformation;

public class MapBoxHelper {

    private static final long POINT_ANIMATION_DURATION = 200;

    /**
     * Animate on a line a point from an origin to a destination for a MapBox GeoJsonSource.
     *
     * @param animator the animator
     * @param originPoint is the point where the animation begins
     * @param destinationPoint is the point where the animation ends
     * @param source of the GeoJsonSource on which to apply the animation
     * @return the new position reached at the end of the animation
     */
    protected static LatLng updatePositionByLineAnimation(GeoJsonSource source, ValueAnimator animator, LatLng originPoint, @NonNull LatLng destinationPoint) {

        if (animator != null && animator.isStarted()) {
            originPoint = (LatLng) animator.getAnimatedValue();
            animator.cancel();
        }

        animator = ObjectAnimator
                .ofObject(latLngEvaluator, originPoint, destinationPoint)
                .setDuration(POINT_ANIMATION_DURATION);
        animator.addUpdateListener(animatorUpdateListenerForGJSource(source));
        animator.start();

        return destinationPoint;
    }

    //Animator update listener creator for a GeoJsonSource
    private static ValueAnimator.AnimatorUpdateListener animatorUpdateListenerForGJSource(GeoJsonSource source) {
        return new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                LatLng animatedPosition = (LatLng) valueAnimator.getAnimatedValue();
                source.setGeoJson(Point.fromLngLat(animatedPosition.getLongitude(), animatedPosition.getLatitude()));
            }
        };
    }

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
     * Draw a hollow red circle on a MapBox
     *
     * @param context context of the application
     * @param mapboxMap MapBox map on which to draw
     * @param position position of the center of the circle
     */
    protected static void drawCircle(@NonNull android.content.Context context, MapboxMap mapboxMap, LatLng position) {
        int distanceDiameter = GlobalUser.getUser().getRadius();

        //Create circles
        Point center = Point.fromLngLat(position.getLongitude(), position.getLatitude());
        Polygon outerCirclePolygon = TurfTransformation.circle(center,  distanceDiameter + distanceDiameter/15.0, "kilometers");
        Polygon innerCirclePolygon = TurfTransformation.circle(center, (double) distanceDiameter, "kilometers");

        GeoJsonSource outerCircleSource = new GeoJsonSource(String.valueOf(R.string.RED_CIRCLE_SOURCE_ID), outerCirclePolygon);

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
        style.addLayer(new FillLayer(String.valueOf(R.string.RED_CIRCLE_LAYER_ID), String.valueOf(R.string.RED_CIRCLE_SOURCE_ID)).withProperties(
                PropertyFactory.fillColor(ContextCompat.getColor(context, R.color.red)),
                PropertyFactory.fillOpacity(0.4f)
        ));
    }

    /**
     * Compute the zoom to use on mapbox knowing how big the region to see is.
     *
     * @param cameraPosition the mamera position on MapBox
     * @param kilometers radius of the circle we want to be able to see
     * @return the zoom to use on mapbox
     */
    protected static double zoomFromKilometers(LatLng cameraPosition, int kilometers) {
        int absLat = Math.abs((int) cameraPosition.getLatitude());

        //Documentation used for this calcul: https://docs.mapbox.com/help/glossary/zoom-level/
        double zoomFromKilometerTheory = 13.6 - Math.log((double) kilometers)/Math.log(2); //The zoom to apply given an amount of kilometer in Theory for latitude 0
        double latitudeDeformation = -0.00046*Math.pow(absLat, 2); //The latitude deformation effect to apply on the zoom parameter if the latitude isn't 0

        return zoomFromKilometerTheory + latitudeDeformation;
    }

    /**
     * Set the Progress bar to a predefined set of color (red, yellow, cyan and blue)
     * @param bar
     * @param barValue
     */
    protected static void setHotBarColor(ProgressBar bar, int barValue){
        final int ONE_QUARTER = 25;
        final int TWO_QUARTER = 50;
        final int THREE_QUARTER = 75;

        bar.setProgress(barValue);
        bar.setProgressTintList(ColorStateList.valueOf(Color.BLUE));
        if (barValue > THREE_QUARTER){
            bar.setProgressTintList(ColorStateList.valueOf(Color.RED));
        }
        else if (barValue > TWO_QUARTER && barValue <= THREE_QUARTER){
            bar.setProgressTintList(ColorStateList.valueOf(Color.YELLOW));
        }
        else if (barValue > ONE_QUARTER && barValue <= TWO_QUARTER){
            bar.setProgressTintList(ColorStateList.valueOf(Color.CYAN));
        }
    }
}
