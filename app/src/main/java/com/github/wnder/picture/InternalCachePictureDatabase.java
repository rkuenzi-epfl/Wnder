package com.github.wnder.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;

import com.github.wnder.NetworkInformation;
import com.github.wnder.WnderApplication;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

/**
 * Class that links firebase db and local db depending on internet connection availability
 */
public class InternalCachePictureDatabase implements PicturesDatabase{
    private final FirebasePicturesDatabase remoteDatabase;
    private final LocalPictureDatabase localDatabase;
    private Context context;

    /**
     * Constructor
     * @param context app context
     */
    @Inject
    public InternalCachePictureDatabase(Context context){
        remoteDatabase = new FirebasePicturesDatabase();
        localDatabase = new LocalPictureDatabase(context);
        this.context = context;
    }

    /**
     * Checks if app is online or not
     * @return true if available internet connection, false o/w
     */
    public boolean isOnline(){
        return NetworkInformation.isNetworkAvailable(context);
    }

    @Override
    public CompletableFuture<Location> getLocation(String uniqueId) {
        //If connection available, remote db
        if (isOnline()) {
            return remoteDatabase.getLocation(uniqueId);
        }
        //else, local db
        else {
            CompletableFuture<Location> cf = new CompletableFuture<>();
            cf.complete(localDatabase.getLocation(uniqueId));
            return cf;
        }
    }

    @Override
    public CompletableFuture<Location> getApproximateLocation(String uniqueId) throws IllegalStateException{
        //If connection available, remote db
        if (isOnline()) {
            return remoteDatabase.getApproximateLocation(uniqueId);
        }
        //Not available when no internet
        else {
            throw new IllegalStateException("The approximate location is not available locally");
        }
    }

    @Override
    public CompletableFuture<Map<String, Location>> getUserGuesses(String uniqueId) throws IllegalStateException{
        //If connection available, remote db
        if (isOnline()) {
            return remoteDatabase.getUserGuesses(uniqueId);
        }
        //Not available when no internet
        else {
            throw new IllegalStateException("This method is not available on offline mode");
        }
    }

    @Override
    public CompletableFuture<Map<String, Double>> getScoreboard(String uniqueId) {
        //If connection available, remote db
        if (isOnline()) {
            return remoteDatabase.getScoreboard(uniqueId);
        }
        //else, local db
        else {
            CompletableFuture<Map<String, Double>> cf = new CompletableFuture<>();
            cf.complete(localDatabase.getScoreboard(uniqueId));
            return cf;
        }
    }

    @Override
    public CompletableFuture<Void> sendUserGuess(String uniqueId, String user, Location guessedLocation) throws IllegalStateException{
        //If connection available, remote db
        if (isOnline()) {
            return remoteDatabase.sendUserGuess(uniqueId, user, guessedLocation);
        }
        //Not available when no internet
        else {
            throw new IllegalStateException("This method is not available on offline mode");
        }
    }

    @Override
    public CompletableFuture<Bitmap> getBitmap(String uniqueId) {
        //If connection available, remote db
        if (isOnline()) {
            return remoteDatabase.getBitmap(uniqueId);
        }
        //else, local db
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
        //If connection available, remote db
        if (isOnline()) {
            return remoteDatabase.uploadPicture(uniqueId, user, location, uri);
        }
        //Not available when no internet
        else {
            throw new IllegalStateException("This method is not available on offline mode");
        }
    }

    @Override
    public CompletableFuture<Long> getKarma(String uniqueId) throws IllegalStateException{
        //If connection available, remote db
        if (isOnline()) {
            return remoteDatabase.getKarma(uniqueId);
        }
        //Not available when no internet
        else {
            throw new IllegalStateException("The karma is not available locally");
        }
    }

    @Override
    public CompletableFuture<Void> updateKarma(String uniqueId, int delta) throws IllegalStateException{
        //If connection available, remote db
        if (isOnline()) {
            return remoteDatabase.updateKarma(uniqueId, delta);
        }
        //Not available when no internet
        else {
            throw new IllegalStateException("The karma is not stored locally");
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
