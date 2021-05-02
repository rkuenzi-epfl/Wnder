package com.github.wnder.picture;

import android.graphics.Bitmap;
import android.location.Location;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Map;

public final class LocalPictureSerializer {

    /**
     * Constructor, not instantiable
     */
    public LocalPictureSerializer(){
        //not instantiable
    }

    /**
     * Transforms attributes of the image to a string of a JSON
     * @param realLocation real location of the image
     * @param guessedLocation location that the user guessed
     * @param scoreboard scoreboard of the image
     * @return a string of the associated json object
     */
    public static String serializePicture(Location realLocation, Location guessedLocation, Map<String, Double> scoreboard){
        JSONObject json = new JSONObject();
        try {
            json.put("realLongiture", realLocation.getLongitude());
            json.put("realLatitude", realLocation.getLatitude());
            json.put("guessedLongitude", guessedLocation.getLongitude());
            json.put("guessedLatitude", guessedLocation.getLatitude());
            String gson = new Gson().toJson(scoreboard);
            json.put("scoreboard", gson);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json.toString();
    }

    /**
     * Transforms a serialized picture into a JSONObject
     * @param Json string to deserialize
     * @return a json object of the image
     */
    public static JSONObject deserializePicture(String Json){
        JSONObject json;
        try {
            json = new JSONObject(Json);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }
}
