package com.github.wnder.picture;

import android.graphics.Bitmap;
import android.location.Location;

import com.github.wnder.Score;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class FirebasePicturesDatabase implements PicturesDatabase {

    private final StorageReference storage;
    private final CollectionReference picturesCollection;

    @Inject
    public FirebasePicturesDatabase(){
        storage = FirebaseStorage.getInstance().getReference();
        picturesCollection = FirebaseFirestore.getInstance().collection("pictures");
    }

    @Override
    public CompletableFuture<Location> getLocation(String uniqueId) {
        CompletableFuture<Location> cf = new CompletableFuture<>();
        picturesCollection.document(uniqueId).get().
                addOnSuccessListener((documentSnapshot) -> {
                    //once we get it, convert it to a Location (because it's two double in Firestore)
                    Location convertedResult = new Location("");
                    double latitude = documentSnapshot.getDouble("latitude");
                    double longitude = documentSnapshot.getDouble("longitude");
                    convertedResult.setLatitude(latitude);
                    convertedResult.setLongitude(longitude);

                    cf.complete(convertedResult);

                })
                .addOnFailureListener(cf::completeExceptionally);
        return cf;
    }

    @Override
    public CompletableFuture<Location> getApproximateLocation(String uniqueId) {

        return getLocation(uniqueId).thenApply((location) -> {
            //randomize location a bit in a 200 meters radius
            double radius = 200; // meters
            Random random = new Random();
            double distance = Math.sqrt(random.nextDouble()) * radius / 111000;
            double angle = 2 * Math.PI * random.nextDouble();
            double longitude_delta = distance * Math.cos(angle);
            double latitude_delta = distance * Math.sin(angle);
            longitude_delta /= Math.cos(Math.toRadians(location.getLatitude()));

            //Create and set new location
            Location al = new Location("");
            al.setLongitude(location.getLongitude() + longitude_delta);
            al.setLatitude(location.getLatitude() + latitude_delta);

            return al;
        });
    }

    @Override
    public CompletableFuture<Map<String, Location>> getUserGuesses(String uniqueId) {
        CompletableFuture<Map<String, Location>> cf = new CompletableFuture<>();
        picturesCollection.document(uniqueId).collection("userData").document("userGuesses").get()
                .addOnSuccessListener((documentSnapshot) -> {
                    //Once done, organize and convert them into Locations
                    Map<String, Location> convertedResult = new TreeMap<>();
                    for(Map.Entry<String, Object> e : documentSnapshot.getData().entrySet()){
                        GeoPoint geoPoint = documentSnapshot.getGeoPoint(e.getKey());
                        Location locationEntry = new Location("");
                        locationEntry.setLatitude(geoPoint.getLatitude());
                        locationEntry.setLongitude(geoPoint.getLongitude());
                        convertedResult.put(e.getKey(), locationEntry);
                    }

                    cf.complete(convertedResult);

                })
                .addOnFailureListener(cf::completeExceptionally);
        return cf;
    }

    @Override
    public CompletableFuture<Map<String, Double>> getScoreboard(String uniqueId) {
        CompletableFuture<Map<String, Double>> cf = new CompletableFuture<>();
        picturesCollection.document(uniqueId).collection("userData").document("userScores").get()
                .addOnSuccessListener((documentSnapshot) -> {
                    //Once done, organize results and accept them
                    Map<String, Double> convertedResult = new TreeMap<>();
                    for(Map.Entry<String, Object> e : documentSnapshot.getData().entrySet()){
                        Double value = documentSnapshot.getDouble(e.getKey());
                        if(value != -1){

                            convertedResult.put(e.getKey(), documentSnapshot.getDouble(e.getKey()));
                        }
                    }
                    cf.complete(convertedResult);
                })
                .addOnFailureListener(cf::completeExceptionally);
        return cf;
    }

    @Override
    public CompletableFuture<Void> sendUserGuess(String uniqueId, String user, Location guessedLocation) {
        CompletableFuture<Void> guessSent = new CompletableFuture<>();
        CompletableFuture<Void> scoreSent = new CompletableFuture<>();
        getLocation(uniqueId).thenAccept(location -> {
            getUserGuesses(uniqueId).thenAccept((userGuesses) -> {
                userGuesses.put(user, guessedLocation);

                //Convert it to GeoPoint to fit Firestore (because Location doesn't work)
                Map<String, GeoPoint> convertedGuesses = new TreeMap<>();
                for (Map.Entry<String, Location> e : userGuesses.entrySet()) {
                    GeoPoint guess = new GeoPoint(e.getValue().getLatitude(), e.getValue().getLongitude());
                    convertedGuesses.put(e.getKey(), guess);
                }
                picturesCollection.document(uniqueId).collection("userData").document("userScores")
                        .set(userGuesses)
                        .addOnSuccessListener(result -> guessSent.complete(null))
                        .addOnFailureListener(guessSent::completeExceptionally);

            });
            getScoreboard(uniqueId).thenAccept((scoreboard) -> {
                //Compute score
                Double score = Score.computeScore(location, guessedLocation);
                scoreboard.put(user, score);

                picturesCollection.document(uniqueId).collection("userData").document("userScores")
                        .set(scoreboard)
                        .addOnSuccessListener(result -> scoreSent.complete(null))
                        .addOnFailureListener(scoreSent::completeExceptionally);

            });
        });
        return CompletableFuture.allOf(guessSent, scoreSent);
    }

    @Override
    public CompletableFuture<Bitmap> getBitmap(String uniqueId) {
        return null;
    }

    @Override
    public CompletableFuture<Void> uploadPicture(NewPicture picture) {
        return null;
    }

    @Override
    public CompletableFuture<Long> getKarma(String uniqueId) {
        CompletableFuture<Long> cf = new CompletableFuture<>();

        picturesCollection.document(uniqueId).get()
                .addOnSuccessListener(documentSnapshot -> cf.complete(documentSnapshot.getLong("karma")))
                .addOnFailureListener(cf::completeExceptionally);

        return cf;
    }

    @Override
    public CompletableFuture<Void> updateKarma(String uniqueId, int delta) {
        CompletableFuture<Void> cf = new CompletableFuture<>();
        //Get current karma
        picturesCollection.document(uniqueId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, Object> toUpload = new HashMap<>();
                    toUpload.put("latitude", documentSnapshot.getDouble("latitude"));
                    toUpload.put("longitude", documentSnapshot.getDouble("longitude"));
                    toUpload.put("karma", documentSnapshot.getLong("karma") + delta);
                    picturesCollection.document(uniqueId)
                            .set(toUpload)
                            .addOnSuccessListener(result -> cf.complete(null))
                            .addOnFailureListener(cf::completeExceptionally);
                })
                .addOnFailureListener(cf::completeExceptionally);
        return cf;
    }
}
