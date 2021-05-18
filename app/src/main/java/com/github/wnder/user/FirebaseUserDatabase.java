package com.github.wnder.user;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;

import com.github.wnder.networkService.NetworkInformation;
import com.github.wnder.picture.InternalCachePictureDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class FirebaseUserDatabase implements UserDatabase{

    private final CollectionReference picturesCollection;
    private final CollectionReference usersCollection;
    private final Context context;

    private NetworkInformation networkInfo;
    private final InternalCachePictureDatabase ICPD;

    @Inject
    public FirebaseUserDatabase(Context context){
        picturesCollection = FirebaseFirestore.getInstance().collection("pictures");
        usersCollection = FirebaseFirestore.getInstance().collection("users");
        this.context = context;

        //setup network info
        networkInfo = new NetworkInformation((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        ICPD = new InternalCachePictureDatabase(context);

    }

    @Override
    public CompletableFuture<List<String>> getPictureList(User user, String picturesListName) {
        CompletableFuture<List<String>> cf = new CompletableFuture<>();
        if(user instanceof GuestUser){
            cf.complete(new ArrayList<>());
        } else{

            usersCollection.document(user.getName()).get().addOnSuccessListener(documentSnapshot -> {
                List<String> pictureList = (List<String>) documentSnapshot.get(picturesListName);
                if(pictureList == null){
                    pictureList = new ArrayList<>();
                }
                cf.complete(pictureList);
            }).addOnFailureListener(cf::completeExceptionally);
        }
        return cf;
    }

    private CompletableFuture<List<String>> getGuessedAndUploadedPictureList(User user) {
        List<String> guessedAndUploaded = new ArrayList<>();
        if(user instanceof GuestUser){
            return CompletableFuture.completedFuture(guessedAndUploaded);
        }
        CompletableFuture<List<String>> guessedCf = new CompletableFuture<>();
        CompletableFuture<List<String>> uploadedCf = new CompletableFuture<>();
        getPictureList(user,"guessedPics").thenAccept(guessed -> {
            guessedAndUploaded.addAll(guessed);
            guessedCf.complete(guessed);
        });
        getPictureList(user,"uploadedPics").thenAccept(uploaded -> {
            guessedAndUploaded.addAll(uploaded);
            uploadedCf.complete(uploaded);
        });

        return CompletableFuture.allOf(guessedCf, uploadedCf).thenApply(nothing -> guessedAndUploaded);
    }

    @Override
    public CompletableFuture<String> getNewPictureForUser(User user, int radius) {
        CompletableFuture<String> cf = new CompletableFuture<>();
        // Get all pictures and remove the ones not in radius
        getAllIdsAndLocation().thenAccept( idsAndLocation -> {
            Location userLocation = user.getPositionFromGPS((LocationManager)context.getSystemService(Context.LOCATION_SERVICE),context);

            Set<String> inRadius = keepOnlyInRadius(userLocation, idsAndLocation, radius);

            // Get all guessed and uploaded pictures and remove them from the ones in radius
            getGuessedAndUploadedPictureList(user).thenAccept(guessedAndUploaded ->{
                inRadius.removeAll(guessedAndUploaded);

                // Get the karma of the rest of the pictures and return on of them randomly (weighted with karma)
                getKarmaForAllPictures(inRadius).thenAccept(idsAndKarma -> {
                    cf.complete(selectImageBasedOnKarma(idsAndKarma));
                }).exceptionally(exception -> {
                    cf.completeExceptionally(exception);
                    return null;
                });

            }).exceptionally(exception -> {
                cf.completeExceptionally(exception);
                return null;
            });

        }).exceptionally(exception -> {
            cf.completeExceptionally(exception);
            return null;
        });

        return cf;
    }

    /**
     * Retrieve all the ids and the location of all the pictures
     * @return a Future of all the ids and locations
     */
    private CompletableFuture<Map<String, Location>> getAllIdsAndLocation(){
        CompletableFuture<Map<String, Location>> cf = new CompletableFuture<>();
        Map<String, Location> idsAndLoc = new HashMap<>();

        picturesCollection.get().addOnSuccessListener((queryDocumentSnapshots) -> {
            List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
            for(int i = 0; i < docs.size(); i++){
                Location loc = new Location("");
                loc.setLatitude(docs.get(i).getDouble("latitude"));
                loc.setLongitude(docs.get(i).getDouble("longitude"));

                idsAndLoc.put(docs.get(i).getId(), loc);
            }
            cf.complete(idsAndLoc);
        }).addOnFailureListener(cf::completeExceptionally);
        return cf;
    }

    /**
     * Retrieve the karma of a set of pictures
     * @param pictureIds the picture ids
     * @return a Future with a map from picture id to karma value
     */
    private CompletableFuture<Map<String, Long>> getKarmaForAllPictures(Set<String> pictureIds){
        CompletableFuture<Map<String, Long>> cf = new CompletableFuture<>();
        Map<String, Long> picsAndKarma = new HashMap<>();

        picturesCollection.get().addOnSuccessListener((queryDocumentSnapshots)->{
            for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()){
                if(pictureIds.contains(doc.getId())){
                    picsAndKarma.put(doc.getId(), doc.getLong("karma"));
                }
            }
            cf.complete(picsAndKarma);
        }).addOnFailureListener(cf::completeExceptionally);

        return cf;
    }


    /**
     * Get a subset of only the pictures that are in a given radius around a location
     * @param location the location around which we search
     * @param idsAndLocs all pictures ids and their location
     * @param radius the chosen radius
     * @return a set of all pictures ids in radius
     */
    private Set<String> keepOnlyInRadius(Location location, Map<String, Location> idsAndLocs, int radius){
        Set<String> correctIds = new HashSet<>();
        for(Map.Entry<String, Location> entry : idsAndLocs.entrySet()){
            float[] res = new float[1];
            Location.distanceBetween(entry.getValue().getLatitude(), entry.getValue().getLongitude(),
                    location.getLatitude(), location.getLongitude(), res);
            if(res[0] < radius*1000){
                correctIds.add(entry.getKey());
            }
        }
        return correctIds;
    }

    /**
     * Find the string associated with a random number (for the tombola)
     * @param randomNumber the random number
     * @param map the ids associated with numbers
     * @return picture id, "" if none is correct
     */
    private String findAssociatedRandomId(int randomNumber, Map<String, Long> map){
        //Select a random image
        int counter = 0;
        for(Map.Entry<String, Long> entry : map.entrySet()){
            counter += entry.getValue();
            if(counter >= randomNumber){
                return entry.getKey();
            }
        }

        //If there is no image, return this
        return "";
    }

    /**
     * Returns the id of a picture from the parameter selected randomly. The more karma a picture have, the more chances the image has to get selected
     * @param idsAndKarma the ids associated with the karma of the pictures
     * @return
     */
    protected String selectImageBasedOnKarma(Map<String, Long> idsAndKarma){

        //The default image is the empty string if no images were found on the database
        if(idsAndKarma.size() == 0){
            return "";
        }
        //Compute the minimum karma of the pictures
        long minKarma = Collections.min(idsAndKarma.values());
        Map<String, Long> correctedMap = new HashMap<>();
        int sumKarma = 0;

        //Compute the sum of all images' karma and change linearly the karma of all pictures so that the karma of the picture that has the least karma is one
        for(Map.Entry<String, Long> entry : idsAndKarma.entrySet()){
            long newKarma = entry.getValue() - minKarma + 1L;
            sumKarma += newKarma;
            correctedMap.put(entry.getKey(), newKarma);
        }

        Random rand = new Random();
        int randomNumber = rand.nextInt(sumKarma);

        return findAssociatedRandomId(randomNumber, correctedMap);
    }

    /**
     * get all the scores of a user
     * @return a set of all the scores a user achieved
     */
    @Override
    public CompletableFuture<Set<Double>> getAllScores(User user){
        CompletableFuture<Set<Double>> allScoresFuture = new CompletableFuture<>();
        Set<Double> allScores = new HashSet<>();

        CompletableFuture<List<String>> guessedPics = new CompletableFuture<>();

        //only available if there's an internet connection, else every return will be empty
        if(networkInfo.isNetworkAvailable()){
            guessedPics = getPictureList(user, "guessedPics");
        }
        else{
            Set<Double> emptySet = new HashSet<>();
            allScoresFuture.complete(emptySet);
            return allScoresFuture;
        }
        //get all the guessed pics
        guessedPics.thenAccept(pics ->{
            //create an array to store all the score futures
            CompletableFuture[] futureScores = new CompletableFuture[pics.size()];

            //for each guessed pic, get its score and store this future into the array
            for(String uniqueId: pics){
                futureScores[pics.indexOf(uniqueId)] = (ICPD.getScoreboard(uniqueId).thenApply(s -> s.get(user.getName())));
            }

            //Once a score is completed, complete add it to all the scores already completed
            for(CompletableFuture<Double> futureScore: futureScores){
                futureScore.thenAccept(score -> allScores.add(score));
            }

            //once all scores have been completed, complete the future
            CompletableFuture<Void> allScoresReceived = CompletableFuture.allOf(futureScores);
            allScoresReceived.thenAccept(empty -> allScoresFuture.complete(allScores));
        });

        return allScoresFuture;
    }
}
