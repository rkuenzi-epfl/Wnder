package com.github.wnder.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

import com.github.wnder.Score;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebasePicturesDatabase implements PicturesDatabase {

    private final StorageReference storage;
    private final CollectionReference picturesCollection;
    private final CollectionReference usersCollection;
    private final CollectionReference reportedPicturesCollection;

    private enum PictureType {guess, upload};

    private final static String ACTIVE_UPLOAD_URI_DIR_NAME = "active_uploads";
    private final File activeUploadDirectory;

    @Inject
    public FirebasePicturesDatabase(Context context){
        storage = FirebaseStorage.getInstance().getReference();
        picturesCollection = FirebaseFirestore.getInstance().collection("pictures");
        usersCollection = FirebaseFirestore.getInstance().collection("users");
        reportedPicturesCollection = FirebaseFirestore.getInstance().collection("reportedPictures");

        activeUploadDirectory = context.getDir(ACTIVE_UPLOAD_URI_DIR_NAME, Context.MODE_PRIVATE);

        // List all files waiting that we resume upload
        String[] activeUploadFiles = activeUploadDirectory.list();
        for (String uniqueId: activeUploadFiles) {

            File file = new File(activeUploadDirectory, uniqueId);
            UploadInfo uploadInfo = UploadInfo.loadUploadInfo(file);

            if(uploadInfo != null){
                uploadPicture(uniqueId, uploadInfo);
            } else {
                // Not all information are available so we can't upload
                // Abort and delete the malformed file
                UploadInfo.deleteUploadInfo(file);
            }
        }
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
    public CompletableFuture<Void> uploadPicture(String uniqueId, UploadInfo uploadInfo) {

        // Start upload
        StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/jpeg").build();
        UploadTask pictureTask = storage.child("pictures/"+uniqueId+".jpg").putFile(uploadInfo.pictureUri, metadata);

        File file = new File(activeUploadDirectory, uniqueId);

        // Write json to file
        try{
            UploadInfo.storeUploadInfo(file, uploadInfo);
        } catch (Exception e) {
            // If it fails return an exceptionnaly completed future
            CompletableFuture<Void> cf = new CompletableFuture<>();
            cf.completeExceptionally(e);
            return cf;
        }

        // Attach upload SuccessListener
        return attachUploadListener(uniqueId, uploadInfo.userName, uploadInfo.location, pictureTask);
    }

    /**
     * Attach new listener on picture upload success to upload the associated metadata
     * @param uniqueId uploaded picture unique id
     * @param userName uploading user name
     * @param location location the picture was taken from
     * @param task the task to attach the metadata task to
     * @return a completable future that completes when everything is uploaded
     */
    private CompletableFuture<Void> attachUploadListener(String uniqueId, String userName, Location location, UploadTask task){
        CompletableFuture<Void> attributesCf = new CompletableFuture<>();
        CompletableFuture<Void> userGuessesCf = new CompletableFuture<>();
        CompletableFuture<Void> userScoresCf = new CompletableFuture<>();
        CompletableFuture<Void> userUploadListCf = addToUserPictures(uniqueId, userName, PictureType.upload);
        CompletableFuture<Void> cf = CompletableFuture.allOf(userUploadListCf, attributesCf, userGuessesCf, userScoresCf);

        task.addOnSuccessListener(pictureUploadResult -> {
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
            emptyGuesses.put(userName, defaultGuess);
            picturesCollection.document(uniqueId).collection("userData").document("userGuesses").set(emptyGuesses)
                    .addOnSuccessListener(result -> userGuessesCf.complete(null)).addOnFailureListener(userGuessesCf::completeExceptionally);


            //necessary to have the correct documents created in firestore
            Map<String, Double> emptyScoreboard= new HashMap<>();
            emptyScoreboard.put(userName, -1.);
            picturesCollection.document(uniqueId).collection("userData").document("userScores").set(emptyScoreboard)
                    .addOnSuccessListener(result -> userScoresCf.complete(null)).addOnFailureListener(userScoresCf::completeExceptionally);
        }).addOnFailureListener(cf::completeExceptionally);

        // Delete local metadata when everything is uploaded
        cf.thenAccept(nothing -> UploadInfo.deleteUploadInfo(new File(activeUploadDirectory, uniqueId)));
        return cf;

    }

    @Override
    public CompletableFuture<Void> addToReportedPictures(String uniqueId) {
        CompletableFuture<Void> pictureAdded = new CompletableFuture<>();
        reportedPicturesCollection.document(uniqueId).set(new HashMap<String, Object>()).addOnSuccessListener((result -> {
            pictureAdded.complete(null);
        })).addOnFailureListener(pictureAdded::completeExceptionally);

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
