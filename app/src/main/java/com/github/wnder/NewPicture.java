package com.github.wnder;

import android.net.Uri;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NewPicture implements Picture{
    private String user;

    //Image unique ID
    private String uniqueId;
    private Uri uri;

    //Image location
    private double longitude;
    private double latitude;

    //User data for the image: global scoreboard + all guesses
    private Map<String, Double> scoreboard;
    //Map<[user ID], Map<[longitude/latitude], [value]>>
    private Map<String, Map<String, Double>> guesses;

    public NewPicture(String user, double longitude, double latitude, Uri uri){
        this.user = user;

        this.longitude = longitude;
        this.latitude = latitude;
        this.uri = uri;

        this.scoreboard = new HashMap<String, Double>();
        this.scoreboard.put("default", -1.);

        this.guesses = new HashMap<String, Map<String, Double>>();
        Map<String, Double> defaultGuess = new HashMap<>();
        defaultGuess.put("longitude", -1.);
        defaultGuess.put("latitude", -1.);
        this.guesses.put("default", defaultGuess);

        this.uniqueId = this.user + Calendar.getInstance().getTimeInMillis();
    }

    public Boolean sendPictureToDb(){
        return false;
    }

    public String getUniqueId(){
        return uniqueId;
    }

    public Uri getUri(){
        return uri;
    }

    public Double getLongitude(){
        return longitude;
    }

    public Double getLatitude(){
        return latitude;
    }

    public Map<String, Double> getScoreboard(){
        return scoreboard;
    }

    public Map<String, Map<String, Double>> getGuesses(){
        return guesses;
    }
}
