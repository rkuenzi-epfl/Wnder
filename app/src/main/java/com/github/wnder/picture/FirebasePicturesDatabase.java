package com.github.wnder.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.github.wnder.Score;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

public class FirebasePicturesDatabase implements PicturesDatabase {

    private final StorageReference storage;
    private final CollectionReference picturesCollection;
    private final CollectionReference usersCollection;
    private final Context context;

    private final String ACTIVE_UPLOAD_URI_DIR_NAME = "active_uploads";
    private final File activeUploadDirectory;

    @Inject
    public FirebasePicturesDatabase(Context context){
        storage = FirebaseStorage.getInstance().getReference();
        picturesCollection = FirebaseFirestore.getInstance().collection("pictures");
        usersCollection = FirebaseFirestore.getInstance().collection("users");
        this.context = context;

        activeUploadDirectory = context.getDir(ACTIVE_UPLOAD_URI_DIR_NAME, Context.MODE_PRIVATE);

        // List all files waiting that we resume upload
        String[] activeUploadFiles = activeUploadDirectory.list();
        for (String uniqueId: activeUploadFiles) {
            Log.d("File", uniqueId);
            JSONObject json = loadUploadMetadata(uniqueId);
            if(json != null){
                Double latitude = null;
                Double longitude = null;
                Location location = new Location("");
                String userName = null;
                String pictureUri = null;
                String uploadSessionUri = null;
                try {
                    latitude = json.getDouble("latitude");
                    longitude= json.getDouble("longitude");
                    userName = json.getString("user_name");
                    pictureUri = json.getString("file_uri");
                    uploadSessionUri = json.getString("upload_session_uri");
                } catch (JSONException e){

                }

                if(latitude != null && longitude != null && userName != null && pictureUri != null){
                    location.setLatitude(latitude);
                    location.setLongitude(longitude);
                    // Resume uploads of found in files
                    if(uploadSessionUri != null){
                        Log.d("Upload", "RESUME");

                        UploadTask pictureTask = storage.putFile(Uri.parse(pictureUri), new StorageMetadata.Builder().build(), Uri.parse(uploadSessionUri));
                        attachUploadListener(uniqueId, userName, location, pictureTask);
                    } else {

                        Log.d("Upload", "START UNSTARTED");
                        uploadPicture(uniqueId, userName, location, Uri.parse(pictureUri));
                    }
                }
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
        return CompletableFuture.allOf(guessSent, scoreSent, addToUserGuessedPictures(uniqueId, user));
    }

    /**
     * Add a picture to the guessed pictures of a user on the database
     * @param user the user
     */
    private CompletableFuture<Void> addToUserGuessedPictures(String uniqueId, String user) {
        CompletableFuture<Void> cf = new CompletableFuture<>();
        //user guessed pictures
        usersCollection.document(user).get().addOnSuccessListener((documentSnapshot) -> {
            //Get uploaded and guessed pics
            List<String> guessedPictures = (List<String>) documentSnapshot.get("guessedPics");
            List<String> uploadedPictures = (List<String>) documentSnapshot.get("uploadedPics");

            //create the lists if they don't exist
            if (guessedPictures == null) {
                guessedPictures = new ArrayList<>();
            }
            if (uploadedPictures == null) {
                uploadedPictures = new ArrayList<>();
            }

            //add picture to list if it isn't already in
            if (!guessedPictures.contains(uniqueId)) {
                guessedPictures.add(uniqueId);
            }

            //Upload everything back to Firestore
            Map<String, Object> toUpload = new HashMap<>();
            toUpload.put("guessedPics", guessedPictures);
            toUpload.put("uploadedPics", uploadedPictures);
            usersCollection.document(user).set(toUpload)
                    .addOnSuccessListener(result -> cf.complete(null))
                    .addOnFailureListener(cf::completeExceptionally);
        }).addOnFailureListener(cf::completeExceptionally);
        return cf;
    }

    @Override
    public CompletableFuture<Bitmap> getBitmap(String uniqueId) {
        CompletableFuture<Bitmap> cf = new CompletableFuture<>();
        storage.child("pictures/"+uniqueId+".jpg").getBytes(Long.MAX_VALUE)
                .addOnSuccessListener(bytes -> cf.complete(BitmapFactory.decodeByteArray(bytes, 0, bytes.length)))
                .addOnFailureListener(cf::completeExceptionally);
        return cf;
    }

    private void storeUploadMetadata(String uniqueId, String userName, Location location, Uri pictureUri, Uri uploadSessionUri) throws IOException, JSONException{
        //Create json
        JSONObject json = new JSONObject();
        json.put("id", uniqueId);
        json.put("latitude", location.getLatitude());
        json.put("longitude", location.getLongitude());
        json.put("user_name", userName);
        json.put("file_uri", pictureUri);
        json.put("upload_session_uri", uploadSessionUri);

        // Store in file
        File file = new File(activeUploadDirectory, uniqueId);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(json.toString().getBytes());
        fos.close();
    }

    private JSONObject loadUploadMetadata(String uniqueId){
        File file = new File(activeUploadDirectory, uniqueId);
        if(file.exists()) {

            StringBuilder stringBuilder = new StringBuilder();
            try {

                FileInputStream fis = new FileInputStream(file);
                InputStreamReader inputStreamReader =
                        new InputStreamReader(fis, StandardCharsets.UTF_8);
                try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                    String line = reader.readLine();
                    while (line != null) {
                        stringBuilder.append(line);
                        line = reader.readLine();
                    }
                    fis.close();
                } catch (IOException e) {
                    return null;
                }

            } catch (Exception e) {
                return null;
            }
            try{
                Log.d("Json", stringBuilder.toString());
                return new JSONObject(stringBuilder.toString());
            } catch(Exception e){
                return null;
            }
        }
        return null;
    }

    private void deleteUploadMetadata(String uniqueId){
        new File(activeUploadDirectory, uniqueId).delete();
    }

    @Override
    public CompletableFuture<Void> uploadPicture(String uniqueId, String userName, Location location, Uri uri) {
        // Start upload
        StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/jpeg").build();
        UploadTask pictureTask = storage.child("pictures/"+uniqueId+".jpg").putFile(uri, metadata);

        // Write json to file
        try{
            // uploadSessionUri might be null
            storeUploadMetadata(uniqueId, userName, location, uri, pictureTask.getSnapshot().getUploadSessionUri());
        } catch (Exception e) {
            CompletableFuture<Void> cf = new CompletableFuture<>();
            cf.completeExceptionally(e);
            return cf;
        }

        // Attach upload SuccessListener
        return attachUploadListener(uniqueId, userName, location, pictureTask);
    }

    private CompletableFuture<Void> attachUploadListener(String uniqueId, String userName, Location location, UploadTask task){
        CompletableFuture<Void> attributesCf = new CompletableFuture<>();
        CompletableFuture<Void> userGuessesCf = new CompletableFuture<>();
        CompletableFuture<Void> userScoresCf = new CompletableFuture<>();
        CompletableFuture<Void> userUploadListCf = addPhotoToUploadedUserPhoto(uniqueId, userName);

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

        return CompletableFuture.allOf(userUploadListCf, attributesCf, userGuessesCf, userScoresCf)
                .thenAccept(nothing -> deleteUploadMetadata(uniqueId));
    }

    /**
     * Add this picture to the list of uploaded pictures of the user
     */
    private CompletableFuture<Void> addPhotoToUploadedUserPhoto(String uniqueId, String user){
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

                    //If uploaded pictures doesn't contain new picture, add it
                    if (!uploadedPictures.contains(uniqueId)){
                        uploadedPictures.add(uniqueId);
                    }

                    //Upload everything back to Firestore
                    Map<String, Object> toUpload = new HashMap<>();
                    toUpload.put("guessedPics", guessedPictures);
                    toUpload.put("uploadedPics", uploadedPictures);
                    usersCollection.document(user).set(toUpload)
                            .addOnSuccessListener(result -> cf.complete(null))
                            .addOnFailureListener(cf::completeExceptionally);
                })
                .addOnFailureListener(cf::completeExceptionally);

        return cf;
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
