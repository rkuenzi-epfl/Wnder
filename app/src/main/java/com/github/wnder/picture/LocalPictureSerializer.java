package com.github.wnder.picture;

import android.graphics.Bitmap;
import android.location.Location;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
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
            json.put("realLongitude", realLocation.getLongitude());
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

    /**
     * Get real location from deserialized JSONObject
     * @param json deserialized JSONObject
     * @return real location
     */
    public static Location getRealLocation(JSONObject json){
        Location toRet = new Location("");
        try {
            toRet.setLongitude((Double) json.get("realLongitude"));
            toRet.setLatitude((Double) json.get("realLatitude"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return toRet;
    }

    /**
     * Get guessed location from deserialized JSONObject
     * @param json deserialized JSONObject
     * @return guessed location
     */
    public static Location getGuessLocation(JSONObject json){
        Location toRet = new Location("");
        try {
            toRet.setLongitude((Double) json.get("guessedLongitude"));
            toRet.setLatitude((Double) json.get("guessedLatitude"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return toRet;
    }

    /**
     * Get scoreboard from deserialized JSONObject
     * @param json deserialized JSONObject
     * @return scoreboard
     */
    public static Map<String, Double> getScoreboard(JSONObject json){
        Map<String, Double> scoreboard = new HashMap<>();
        try {
            String jsonScoreboard = (String)json.get("scoreboard");
            Gson gson = new Gson();
            scoreboard = gson.fromJson(jsonScoreboard, HashMap.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return scoreboard;
    }
}
