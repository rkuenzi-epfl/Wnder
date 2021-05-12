package com.github.wnder.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;

import com.github.wnder.networkService.NetworkInformation;
import com.github.wnder.networkService.NetworkService;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

/**
 * Class that links firebase db and local db depending on internet connection availability
 */
public class InternalCachePictureDatabase implements PicturesDatabase{
    private final FirebasePicturesDatabase remoteDatabase;
    private final LocalPictureDatabase localDatabase;
    private final Context context;
    public NetworkService networkInfo;

    /**
     * Constructor
     * @param context app context
     */
    @Inject
    public InternalCachePictureDatabase(Context context){
        remoteDatabase = new FirebasePicturesDatabase();
        localDatabase = new LocalPictureDatabase(context);
        this.context = context;
        networkInfo = new NetworkInformation((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
    }

    /**
     * Checks if app is online or not
     * @return true if available internet connection, false o/w
     */
    public boolean isOnline(){
        return networkInfo.isNetworkAvailable();
    }

    @Override
    public CompletableFuture<Location> getLocation(String uniqueId) {
        if (isOnline()) {
            return remoteDatabase.getLocation(uniqueId);
        }
        else {
            CompletableFuture<Location> cf = new CompletableFuture<>();
            cf.complete(localDatabase.getLocation(uniqueId));
            return cf;
        }
    }

    @Override
    public CompletableFuture<Location> getApproximateLocation(String uniqueId) throws IllegalStateException{
        if (isOnline()) {
            return remoteDatabase.getApproximateLocation(uniqueId);
        }
        else {
            CompletableFuture<Location> cf = new CompletableFuture<>();
            cf.completeExceptionally(new IllegalStateException("This method is not available on offline mode"));
            return cf;
        }
    }

    @Override
    public CompletableFuture<Map<String, Location>> getUserGuesses(String uniqueId) throws IllegalStateException{
        if (isOnline()) {
            return remoteDatabase.getUserGuesses(uniqueId);
        }
        else {
            CompletableFuture<Map<String, Location>> cf = new CompletableFuture<>();
            cf.completeExceptionally(new IllegalStateException("This method is not available on offline mode"));
            return cf;
        }
    }

    @Override
    public CompletableFuture<Map<String, Double>> getScoreboard(String uniqueId) {
        if (isOnline()) {
            return remoteDatabase.getScoreboard(uniqueId);
        }
        else {
            CompletableFuture<Map<String, Double>> cf = new CompletableFuture<>();
            cf.complete(localDatabase.getScoreboard(uniqueId));
            return cf;
        }
    }

    @Override
    public CompletableFuture<Void> sendUserGuess(String uniqueId, String user, Location guessedLocation) throws IllegalStateException{
        if (isOnline()) {
            getBitmap(uniqueId).thenAccept(bmp -> {
                getLocation(uniqueId).thenAccept(location->{
                    getScoreboard(uniqueId).thenAccept(scoreboard ->{
                        storePictureLocally(new LocalPicture(uniqueId, bmp, location, guessedLocation, scoreboard));
                    });
                });
            });

            return remoteDatabase.sendUserGuess(uniqueId, user, guessedLocation);
        }
        else {
            CompletableFuture<Void> cf = new CompletableFuture<>();
            cf.completeExceptionally(new IllegalStateException("This method is not available on offline mode"));
            return cf;
        }
    }

    @Override
    public CompletableFuture<Bitmap> getBitmap(String uniqueId) {
        if (isOnline()) {

            return remoteDatabase.getBitmap(uniqueId);
        }
        else {
            CompletableFuture<Bitmap> cf = new CompletableFuture<>();
            try {
                cf.complete(localDatabase.getPicture(uniqueId));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return cf;
        }
    }

    @Override
    public CompletableFuture<Void> uploadPicture(String uniqueId, String user, Location location, Uri uri) throws IllegalStateException{
        if (isOnline()) {
            return remoteDatabase.uploadPicture(uniqueId, user, location, uri);
        }
        else {
            CompletableFuture<Void> cf = new CompletableFuture<>();
            cf.completeExceptionally(new IllegalStateException("This method is not available on offline mode"));
            return cf;
        }
    }

    @Override
    public CompletableFuture<Long> getKarma(String uniqueId) throws IllegalStateException{
        if (isOnline()) {
            return remoteDatabase.getKarma(uniqueId);
        }
        else {
            throw new IllegalStateException("The karma is not available locally");
        }
    }

    @Override
    public CompletableFuture<Void> updateKarma(String uniqueId, int delta) throws IllegalStateException{
        if (isOnline()) {
            return remoteDatabase.updateKarma(uniqueId, delta);
        }
        else {
            CompletableFuture<Void> cf = new CompletableFuture<>();
            cf.completeExceptionally(new IllegalStateException("This method is not available on offline mode"));
            return cf;
        }
    }

    /**
     * Stores the picture in the internal storage
     * @param picture local picture to store
     */
    public void storePictureLocally(LocalPicture picture) {
        localDatabase.storePictureAndMetadata(picture);
    }

    /**
     * Update the scoreboard of the picture in the internal storage
     * @param scoreboard updated scoreboard
     */
    public void updateLocalScoreboard(String uniqueId, Map<String, Double> scoreboard){
        localDatabase.updateScoreboard(uniqueId, scoreboard);
    }

    /**
     * Get the location of the image the user guessed
     * @param uniqueId id of the image
     * @return the location the user guessed
     */
    public Location getLocalGuessedLocation(String uniqueId){
        return localDatabase.getGuessedLocation(uniqueId);
    }

    /**
     * Deletes picture file AND metadata file
     * @param uniqueId uniqueId of picture
     */
    public void deleteLocalPicture(String uniqueId){
        localDatabase.deleteFile(uniqueId);
    }
}
