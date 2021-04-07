package com.github.wnder.picture;

import android.location.Location;

import com.github.wnder.Storage;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public abstract class Picture {

    //Image unique ID
    private String uniqueId;

    //Image location
    protected Location location;

    public Picture(String uniqueId){
        this.uniqueId = uniqueId;
        this.location = null;
    }

    public Picture(String uniqueId, Location location){
        this.uniqueId = uniqueId;
        this.location = location;
    }

    /**
     * Returns the unique ID of the picture
     * @return The unique ID of the picture
     */
    public String getUniqueId(){
        return uniqueId;
    }

    private void setLocation(Location location){
        this.location = location;
    }

    /**
     * Apply consumer function when the location is available
     * @param locationAvailable consumer function to call when the location is available
     */
    public void onLocationAvailable(Consumer<Location> locationAvailable){
        if(location != null){

            locationAvailable.accept(location);
        } else {

            Task<DocumentSnapshot> locationTask = Storage.downloadFromFirestore("pictures", uniqueId);
            locationTask.addOnSuccessListener((documentSnapshot) -> {
                Location convertedResult = new Location("");
                double latitude = documentSnapshot.getDouble("latitude");
                double longitude = documentSnapshot.getDouble("longitude");
                convertedResult.setLatitude(latitude);
                convertedResult.setLongitude(longitude);
                setLocation(convertedResult);
                locationAvailable.accept(convertedResult);
            });
        }
    }


    /**
     * Apply consumer function when an updated version of the scoreboard is available
     * @param scoreboardAvailable consumer function to call when scoreboard is available
     */
    public void onUpdatedScoreboardAvailable(Consumer<Map<String, Double>> scoreboardAvailable){

        String[] path = new String[]{"pictures", uniqueId, "userData", "userScores"};
        Task<DocumentSnapshot> scoreboardTask = Storage.downloadFromFirestore(path);
        scoreboardTask.addOnSuccessListener((documentSnapshot) -> {
            Map<String, Double> convertedResult = new TreeMap<>();
            for(Map.Entry<String, Object> e : documentSnapshot.getData().entrySet()){
                convertedResult.put(e.getKey(), documentSnapshot.getDouble(e.getKey()));
            }
            scoreboardAvailable.accept(convertedResult);
        });
    }

    /**
     * Apply consumer function when an updated version of the user guesses are available
     * @param guessesAvailable consumer function to call when user guesses are available
     */
    public void onUpdatedGuessesAvailable(Consumer<Map<String, Location>> guessesAvailable){

        String[] path = new String[]{"pictures", uniqueId, "userData", "userGuesses"};
        Task<DocumentSnapshot> scoreboardTask = Storage.downloadFromFirestore(path);
        scoreboardTask.addOnSuccessListener((documentSnapshot) -> {
            Map<String, Location> convertedResult = new TreeMap<>();
            for(Map.Entry<String, Object> e : documentSnapshot.getData().entrySet()){
                GeoPoint geoPoint = documentSnapshot.getGeoPoint(e.getKey());
                Location locationEntry = new Location("");
                locationEntry.setLatitude(geoPoint.getLatitude());
                locationEntry.setLongitude(geoPoint.getLongitude());
                convertedResult.put(e.getKey(), locationEntry);
            }
            guessesAvailable.accept(convertedResult);
        });
    }
}
