package com.github.wnder.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

import com.github.wnder.Score;
import com.github.wnder.user.SignedInUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
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
    public CompletableFuture<Void> sendUserGuess(String uniqueId, SignedInUser user, Location guessedLocation, Bitmap mapSnapshot) {
        CompletableFuture<Void> guessSent = new CompletableFuture<>();

        getLocation(uniqueId).thenAccept(location -> {
            GeoPoint guessGeoPoint = new GeoPoint(guessedLocation.getLatitude(), guessedLocation.getLongitude());
            double score = Score.computeScore(location, guessedLocation);
            PictureGuessEntry guessEntry = new PictureGuessEntry(user.getName(), guessGeoPoint, score);
            picturesCollection.document(uniqueId).collection("userData")
                    .add(guessEntry)
                    .addOnSuccessListener(result -> guessSent.complete(null))
                    .addOnFailureListener(guessSent::completeExceptionally);

        });
        return CompletableFuture.allOf(guessSent, addToUserPictures(uniqueId, user, PictureType.guess));
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
        UploadTask pictureTask = storage.child("pictures/"+uniqueId+".jpg").putFile(uploadInfo.getPictureUri(), metadata);

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
        return attachUploadListener(uniqueId, uploadInfo.getUserUid(), uploadInfo.getLocation(), pictureTask);
    }

    /**
     * Attach new listener on picture upload success to upload the associated metadata
     * @param uniqueId uploaded picture unique id
     * @param userName uploading user name
     * @param location location the picture was taken from
     * @param task the task to attach the metadata task to
     * @return a completable future that completes when everything is uploaded
     */
    private CompletableFuture<Void> attachUploadListener(String uniqueId, String userUid, Location location, UploadTask task){
        CompletableFuture<Void> attributesCf = new CompletableFuture<>();
        CompletableFuture<Void> userGuessesCf = new CompletableFuture<>();
        CompletableFuture<Void> userScoresCf = new CompletableFuture<>();
        CompletableFuture<Void> userUploadListCf = addToUserPictures(uniqueId, userUid, PictureType.upload);
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
     * Add a picture to the list of uploaded pictures of a user
     * @param uniqueId the id of the picture to add
     * @param user the user who uploaded the picture
     * @return a Future complete when the list is updated
     */
    private CompletableFuture<Void> addToUploadedPictures(String uniqueId, SignedInUser user){
        CompletableFuture<Void> cf = new CompletableFuture<>();
        //get current user data
        usersCollection.document(user.getUniqueId())
                .update("uploaded", FieldValue.arrayUnion(uniqueId))
                .addOnSuccessListener((documentSnapshot) -> cf.complete(null))
                .addOnFailureListener(cf::completeExceptionally);
        return cf;
    }

    /**
     * Add a picture to the list of guessed pictures of a user
     * @param uniqueId the id of the picture to add
     * @param user the user who uploaded the picture
     * @param guessEntry
     * @return a Future complete when the picture is added
     */
    private CompletableFuture<Void> addToGuessedPictures(String uniqueId, SignedInUser user, UserGuessEntry guessEntry){
        CompletableFuture<Void> cf = new CompletableFuture<>();
        //get current user data
        usersCollection.document(user.getUniqueId()).collection("guessed")
                .document(uniqueId).set(guessEntry)
                .addOnSuccessListener((documentSnapshot) -> cf.complete(null))
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
