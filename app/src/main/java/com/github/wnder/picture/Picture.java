package com.github.wnder.picture;

import android.location.Location;

import com.github.wnder.Storage;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * abstract class for all kind of pictures
 */
public abstract class Picture {

    //Image unique ID
    private String uniqueId;

    //Image location
    protected Location location;

    /**
     * Constructor for picture
     * @param uniqueId unique id of picture
     */
    public Picture(String uniqueId){
        this.uniqueId = uniqueId;
        this.location = null;
    }

    /**
     * Constructor for picture
     * @param uniqueId unique id of picture
     * @param location location of picture
     */
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

    /**
     * Set location of a picture
     * @param location location to be set
     */
    private void setLocation(Location location){
        this.location = location;
    }

    /**
     * Apply consumer function when the location is available
     * @param locationAvailable consumer function to call when the location is available
     */
    public void onLocationAvailable(Consumer<Location> locationAvailable){
        //Check if we already have it, if yes, accept it directly
        if(location != null){

            locationAvailable.accept(location);
        } else {
            //Get current location
            Task<DocumentSnapshot> locationTask = Storage.downloadFromFirestore("pictures", uniqueId);

            locationTask.addOnSuccessListener((documentSnapshot) -> {
                //once we get it, convert it to a Location (because it's two double in Firestore)
                Location convertedResult = new Location("");
                double latitude = documentSnapshot.getDouble("latitude");
                double longitude = documentSnapshot.getDouble("longitude");
                convertedResult.setLatitude(latitude);
                convertedResult.setLongitude(longitude);
                setLocation(convertedResult);

                //Once converted, accept
                locationAvailable.accept(convertedResult);
            });
        }
    }


    /**
     * Apply consumer function when an updated version of the scoreboard is available
     * @param scoreboardAvailable consumer function to call when scoreboard is available
     */
    public void onUpdatedScoreboardAvailable(Consumer<Map<String, Double>> scoreboardAvailable){
        //Get scoreboard
        String[] path = new String[]{"pictures", uniqueId, "userData", "userScores"};
        Task<DocumentSnapshot> scoreboardTask = Storage.downloadFromFirestore(path);
        scoreboardTask.addOnSuccessListener((documentSnapshot) -> {
            //Once done, organize results and accept them
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

        //Retrieve guesses from Firestore
        String[] path = new String[]{"pictures", uniqueId, "userData", "userGuesses"};
        Task<DocumentSnapshot> scoreboardTask = Storage.downloadFromFirestore(path);
        scoreboardTask.addOnSuccessListener((documentSnapshot) -> {

            //Once done, organize and convert them inot Locations
            Map<String, Location> convertedResult = new TreeMap<>();
            for(Map.Entry<String, Object> e : documentSnapshot.getData().entrySet()){
                GeoPoint geoPoint = documentSnapshot.getGeoPoint(e.getKey());
                Location locationEntry = new Location("");
                locationEntry.setLatitude(geoPoint.getLatitude());
                locationEntry.setLongitude(geoPoint.getLongitude());
                convertedResult.put(e.getKey(), locationEntry);
            }

            //Accept the converted result
            guessesAvailable.accept(convertedResult);
        });
    }

    /**
     * Get karma of a picture
     * @param karmaAvailable
     */
    public void onKarmaUpdated(Consumer<Map<String, Object>> karmaAvailable){
        //Get karma from Firestore
        Task<DocumentSnapshot> karmaTask = Storage.downloadFromFirestore("pictures", uniqueId);
        karmaTask.addOnSuccessListener((documentSnapshot) -> {
            //Convert and accept result
            Map<String, Object> fields = new HashMap<>();
            //We put these attributes back because if we don't, they disappear from the db
            fields.put("longitude", documentSnapshot.getDouble("longitude"));
            fields.put("latitude", documentSnapshot.getDouble("latitude"));
            fields.put("karma", documentSnapshot.getLong("karma"));
            karmaAvailable.accept(fields);
        });
    }

    /**
     * get karma of a picture
     * @param karmaAvailable consumer which will accept the karma
     */
    public abstract void onKarmaAvailable(Consumer<Long> karmaAvailable);
}
