package com.github.wnder.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;

import com.github.wnder.Score;
import com.github.wnder.Storage;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class FirebasePicturesDatabase implements PicturesDatabase {

    private final StorageReference storage;
    private final CollectionReference picturesCollection;
    private final CollectionReference usersCollection;

    private enum PictureType {guess, upload};

    @Inject
    public FirebasePicturesDatabase(){
        storage = FirebaseStorage.getInstance().getReference();
        picturesCollection = FirebaseFirestore.getInstance().collection("pictures");
        usersCollection = FirebaseFirestore.getInstance().collection("users");
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
                        if(geoPoint != null){

                            Location locationEntry = new Location("");
                            locationEntry.setLatitude(geoPoint.getLatitude());
                            locationEntry.setLongitude(geoPoint.getLongitude());
                            convertedResult.put(e.getKey(), locationEntry);
                        }
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
                        convertedResult.put(e.getKey(), documentSnapshot.getDouble(e.getKey()));
                    }
                    cf.complete(convertedResult);
                })
                .addOnFailureListener(cf::completeExceptionally);
        return cf;
    }

    @Override
    public CompletableFuture<Void> sendUserGuess(String uniqueId, String user, Location guessedLocation, Bitmap mapSnapshot) {
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
                picturesCollection.document(uniqueId).collection("userData").document("userGuesses")
                        .set(convertedGuesses)
                        .addOnSuccessListener(result -> guessSent.complete(null)).addOnFailureListener(guessSent::completeExceptionally);

            });
            getScoreboard(uniqueId).thenAccept((scoreboard) -> {
                //Compute score
                Double score = Score.computeScore(location, guessedLocation);
                Map<String, Double> newScoreboard = new HashMap<>(scoreboard);
                newScoreboard.put(user, score);

                picturesCollection.document(uniqueId).collection("userData").document("userScores")
                        .set(newScoreboard)
                        .addOnSuccessListener(result -> scoreSent.complete(null)).addOnFailureListener(scoreSent::completeExceptionally);

            });
        });
        return CompletableFuture.allOf(guessSent, scoreSent, addToUserPictures(uniqueId, user, PictureType.guess));
    }

    @Override
    public CompletableFuture<Bitmap> getBitmap(String uniqueId) {
        CompletableFuture<Bitmap> cf = new CompletableFuture<>();
        storage.child("pictures/"+uniqueId+".jpg").getBytes(Long.MAX_VALUE)
                .addOnSuccessListener(bytes -> cf.complete(BitmapFactory.decodeByteArray(bytes, 0, bytes.length)))
                .addOnFailureListener(cf::completeExceptionally);
        return cf;
    }

    @Override
    public CompletableFuture<Bitmap> getMapSnapshot(Context context, String uniqueId) {
        CompletableFuture<Bitmap> cf = new CompletableFuture<>();
        cf.completeExceptionally(new IllegalStateException("This method is only available on the local database"));
        return cf;
    }

    @Override
    public CompletableFuture<Location> getUserGuess(String uniqueId) {
        CompletableFuture<Location> cf = new CompletableFuture<>();
        cf.completeExceptionally(new IllegalStateException("This method is only available on the local database"));
        return cf;
    }

    @Override
    public CompletableFuture<Void> uploadPicture(String uniqueId, String user, Location location, Uri uri) {
        CompletableFuture<Void> attributesCf = new CompletableFuture<>();
        CompletableFuture<Void> userGuessesCf = new CompletableFuture<>();
        CompletableFuture<Void> userScoresCf = new CompletableFuture<>();
        CompletableFuture<Void> pictureCf = new CompletableFuture<>();
        CompletableFuture<Void> userUploadListCf = addToUserPictures(uniqueId, user, PictureType.upload);

        //coordinates
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("latitude", location.getLatitude());
        attributes.put("longitude", location.getLongitude());
        attributes.put("karma", 0);
        picturesCollection.document(uniqueId).set(attributes)
                .addOnSuccessListener(result -> attributesCf.complete(null)).addOnFailureListener(attributesCf::completeExceptionally);

        //default instantiation for the guesses ()
        //necessary to have the correct documents created in firestore
        Map<String, GeoPoint> emptyGuesses = new HashMap<>();
        GeoPoint defaultGuess = new GeoPoint(location.getLatitude(),location.getLongitude());
        emptyGuesses.put(user, defaultGuess);
        picturesCollection.document(uniqueId).collection("userData").document("userGuesses").set(emptyGuesses)
                .addOnSuccessListener(result -> userGuessesCf.complete(null)).addOnFailureListener(userGuessesCf::completeExceptionally);


        //necessary to have the correct documents created in firestore
        Map<String, Double> emptyScoreboard= new HashMap<>();
        emptyScoreboard.put(user, -1.);
        picturesCollection.document(uniqueId).collection("userData").document("userScores").set(emptyScoreboard)
                .addOnSuccessListener(result -> userScoresCf.complete(null)).addOnFailureListener(userScoresCf::completeExceptionally);

        StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/jpeg").build();
        storage.child("pictures/"+uniqueId+".jpg").putFile(uri, metadata)
                .addOnSuccessListener(result -> pictureCf.complete(null)).addOnFailureListener(pictureCf::completeExceptionally);

        return CompletableFuture.allOf(userUploadListCf, attributesCf, userGuessesCf, userScoresCf, pictureCf);
    }

    @Override
    public CompletableFuture<Void> addToReportedPictures(String uniqueId) {
        CompletableFuture<Void> pictureAdded = new CompletableFuture<>();
        Storage.uploadToFirestore(new HashMap<String, Object>() {
        }, "reportedPictures", uniqueId)
                .addOnSuccessListener((nothing)->{
                    pictureAdded.complete(null);
                });
        return pictureAdded;
    }

    /**
     * Add this picture to the list of uploaded or guessed pictures of the user
     */
    private CompletableFuture<Void> addToUserPictures(String uniqueId, String user, PictureType type){
        CompletableFuture<Void> cf = new CompletableFuture<>();
        //get current user data
        usersCollection.document(user).get()
                .addOnSuccessListener((documentSnapshot) ->{

                    //Get current user data
                    List<String> guessedPictures = (List<String>) documentSnapshot.get("guessedPics");
                    List<String> uploadedPictures = (List<String>) documentSnapshot.get("uploadedPics");

                    //Create lists if they don't exist
                    if (guessedPictures == null) {
                        guessedPictures = new ArrayList<>();
                    }
                    if (uploadedPictures == null) {
                        uploadedPictures = new ArrayList<>();
                    }

                    if(type.equals(PictureType.upload)){
                        //If uploaded pictures doesn't contain the new picture, add it
                        if (!uploadedPictures.contains(uniqueId)){
                            uploadedPictures.add(uniqueId);
                        }
                    }
                    else{
                        //add picture to the list of guessed pictures if it isn't already in
                        if (!guessedPictures.contains(uniqueId)) {
                            guessedPictures.add(uniqueId);
                        }
                    }

                    uploadBack(user, guessedPictures, uploadedPictures, cf);
                })
                .addOnFailureListener(cf::completeExceptionally);

        return cf;
    }

    private void uploadBack(String user, List<String> guessedPics, List<String> uploadedPics, CompletableFuture<Void> cf){
        //Upload everything back to Firestore
        Map<String, Object> toUpload = new HashMap<>();
        toUpload.put("guessedPics", guessedPics);
        toUpload.put("uploadedPics", uploadedPics);
        usersCollection.document(user).set(toUpload)
                .addOnSuccessListener(result -> cf.complete(null))
                .addOnFailureListener(cf::completeExceptionally);
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
        getKarma(uniqueId)
                .thenAccept(karma -> {
                    picturesCollection.document(uniqueId)
                            .update("karma", karma+delta)
                            .addOnSuccessListener(result -> cf.complete(null))
                            .addOnFailureListener(cf::completeExceptionally);
                });
        return cf;
    }
}
