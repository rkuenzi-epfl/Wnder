package com.github.wnder.guessLocation;

import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import com.github.wnder.R;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;

public class GuessLocationSensor{

    /**
     * non-instantiable
     */
    private GuessLocationSensor(){
    }

    /**
     * Calculates new camera position
     * @param event given event
     * @param mapboxMap instantiated mapbox map
     * @param picturePosition picture position
     * @param guessPosition guess location
     * @return CameraPosition new position of the camera
     */
    protected static CameraPosition calculateNewPosition(SensorEvent event, MapboxMap mapboxMap, LatLng picturePosition, LatLng guessPosition) {

        float[] vectorPosition = event.values;
        float[] quat = new float[4];
        float[] rotMat = new float[9];
        float[] result = new float[3];
        SensorManager.getQuaternionFromVector(quat, vectorPosition);
        SensorManager.getRotationMatrixFromVector(rotMat, quat);
        SensorManager.getOrientation(rotMat, result);

        float angleFromNorthToImagePosition = angleFromNorthToImagePosition(mapboxMap, picturePosition, guessPosition); //To take account of the sign

        float angleAroundZ = (float) (result[2]*180/Math.PI + 180); //Value of the sensor

        SymbolLayer layer = (SymbolLayer) mapboxMap.getStyle().getLayer(String.valueOf(R.string.ORANGE_ARROW_LAYER_ID));
        layer.setProperties(PropertyFactory.iconRotate(angleFromNorthToImagePosition - angleAroundZ)); //Arrow angle

        return new CameraPosition.Builder()
                .target(guessPosition)
                .bearing(angleAroundZ)
                .build();
    }

    /**
     * Get angle from north to image position
     * @param mapboxMap instantiated mapbox map
     * @param picturePosition location of the picture
     * @param guessPosition location of the guess
     * @return angle from north to image position
     */
    private static float angleFromNorthToImagePosition(MapboxMap mapboxMap, LatLng picturePosition, LatLng guessPosition){
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
        return vectorX < 0 ? -angleFromNorthToImagePosition : angleFromNorthToImagePosition;
    }


}
