package com.github.wnder.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class InternalCachePictureDatabase implements PicturesDatabase{
    private final FirebasePicturesDatabase remoteDatabase;
    private final LocalPictureDatabase localDatabase;

    //The field exists only because there is still not way to know if we are online
    private boolean isOnline = true;

    //Exists principally for testing for now.
    /**
     * Set online status
     * @param newState new state
     */
    public void setOnlineStatus(boolean newState){
        isOnline = newState;
    }

    @Inject
    public InternalCachePictureDatabase(Context context){
        remoteDatabase = new FirebasePicturesDatabase();
        localDatabase = new LocalPictureDatabase(context);
    }

    @Override
    public CompletableFuture<Location> getLocation(String uniqueId) {
        if (isOnline) {
            return remoteDatabase.getLocation(uniqueId);
        }
        else {
            CompletableFuture<Location> cf = new CompletableFuture<>();
            cf.complete(localDatabase.getLocation(uniqueId));
            return cf;
        }
    }

    @Override
    public CompletableFuture<Location> getApproximateLocation(String uniqueId) {
        if (isOnline) {
            return remoteDatabase.getApproximateLocation(uniqueId);
        }
        else {
            throw new IllegalStateException("The approximate location is not available locally");
        }
    }

    @Override
    public CompletableFuture<Map<String, Location>> getUserGuesses(String uniqueId) {
        if (isOnline) {
            return remoteDatabase.getUserGuesses(uniqueId);
        }
        else {
            throw new IllegalStateException("This method is not available on offline mode");
        }
    }

    @Override
    public CompletableFuture<Map<String, Double>> getScoreboard(String uniqueId) {
        if (isOnline) {
            return remoteDatabase.getScoreboard(uniqueId);
        }
        else {
            CompletableFuture<Map<String, Double>> cf = new CompletableFuture<>();
            cf.complete(localDatabase.getScoreboard(uniqueId));
            return cf;
        }
    }

    @Override
    public CompletableFuture<Void> sendUserGuess(String uniqueId, String user, Location guessedLocation) {
        if (isOnline) {
            return remoteDatabase.sendUserGuess(uniqueId, user, guessedLocation);
        }
        else {
            throw new IllegalStateException("This method is not available on offline mode");
        }
    }

    @Override
    public CompletableFuture<Bitmap> getBitmap(String uniqueId) {
        if (isOnline) {
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
    public CompletableFuture<Void> uploadPicture(String uniqueId, String user, Location location, Uri uri) {
        if (isOnline) {
            return remoteDatabase.uploadPicture(uniqueId, user, location, uri);
        }
        else {
            throw new IllegalStateException("This method is not available on offline mode");
        }
    }

    @Override
    public CompletableFuture<Long> getKarma(String uniqueId) {
        if (isOnline) {
            return remoteDatabase.getKarma(uniqueId);
        }
        else {
            throw new IllegalStateException("The karma is not available locally");
        }
    }

    @Override
    public CompletableFuture<Void> updateKarma(String uniqueId, int delta) {
        if (isOnline) {
            return remoteDatabase.updateKarma(uniqueId, delta);
        }
        else {
            throw new IllegalStateException("The karma is not stored locally");
        }
    }

    /**
     * Stores the picture in the internal storage
     * @param uniqueId id of the image
     * @param bmp bitmap of the image
     * @param realLocation real location of the image
     * @param guessedLocation location that the user guessed
     * @param scoreboard scoreboard of the image
     */
    public void storePictureLocally(String uniqueId, Bitmap bmp, Location realLocation, Location guessedLocation, Map<String, Double> scoreboard) {
        localDatabase.storePictureAndMetadata(uniqueId, bmp, realLocation, guessedLocation, scoreboard);
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
