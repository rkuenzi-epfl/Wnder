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
    public static String seralizePicture(String uniqueId, Location realLocation, Location guessedLocation, Map<String, Double> scoreboard){
        JSONObject json = new JSONObject();
        try {
            json.put("realLongiture", realLocation.getLongitude());
            json.put("realLatitude", realLocation.getLatitude());
            json.put("guessedLongitude", guessedLocation.getLongitude());
            json.put("guessedLatitude", guessedLocation.getLatitude());
            json.put("scoreboard", scoreboard);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json.toString();
    }

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
