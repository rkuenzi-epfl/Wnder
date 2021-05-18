package com.github.wnder.guessLocation;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.github.wnder.R;
import com.github.wnder.user.GlobalUser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;

import java.util.List;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class GuessLocationCompass {

    private static View hotbarView;
    private static LatLng picturePosition;

    /**
     * non-instantiable
     */
    public GuessLocationCompass(View hotbarView, LatLng picturePosition){
        this.hotbarView = hotbarView;
        this.picturePosition = picturePosition;
    }

    /**
     *  Update the compass and the hotbar according to the mode and the distance between the current guess and picture location
     */
    protected static void updateCompass(MapboxMap mapboxMap, LatLng guessPosition, boolean compassMode){
        SymbolLayer layer = (SymbolLayer) mapboxMap.getStyle().getLayer(String.valueOf(R.string.ORANGE_ARROW_LAYER_ID));

        //Arbitrary value based on the radius to check if we are close enough
        double referenceDistance = GlobalUser.getUser().getRadius() * 1000 / 100;

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
}
