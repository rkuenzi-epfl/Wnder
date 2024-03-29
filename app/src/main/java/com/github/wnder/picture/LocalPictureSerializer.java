package com.github.wnder.picture;

import android.location.Location;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public final class LocalPictureSerializer {

    /**
     * Constructor, not instantiable
     */
    private LocalPictureSerializer(){
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
            //Create json
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
        //get json
        JSONObject json = null;
        try {
            json = new JSONObject(Json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Get real location from deserialized JSONObject
     * @param json deserialized JSONObject
     * @return real location
     */
    public static Location getRealLocation(JSONObject json){
        return getLocation(json, LocationType.REAL);
    }

    /**
     * Get guessed location from deserialized JSONObject
     * @param json deserialized JSONObject
     * @return guessed location
     */
    public static Location getGuessLocation(JSONObject json){
        return getLocation(json, LocationType.GUESSED);
    }

    private static Location getLocation(JSONObject json, LocationType type){
        Location toRet = new Location("");
        try {
            if (type == LocationType.GUESSED){
                toRet.setLongitude( json.getDouble("guessedLongitude"));
                toRet.setLatitude( json.getDouble("guessedLatitude"));
            }
            else{
                toRet.setLongitude( json.getDouble("realLongitude"));
                toRet.setLatitude( json.getDouble("realLatitude"));
            }
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
            //get scoreboard from json, as hashmap thanks to Gson
            String jsonScoreboard = (String)json.get("scoreboard");
            Gson gson = new Gson();
            scoreboard = gson.fromJson(jsonScoreboard, HashMap.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return scoreboard;
    }
}
