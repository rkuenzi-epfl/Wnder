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
//uniqueId, bitmap, realLocation, guessLocation, scoreboard
    private FirebasePicturesDatabase remoteDatabase;
    private LocalPictureDatabase localDatabase;

    private boolean IS_ONLINE = true;

    @Inject
    public InternalCachePictureDatabase(Context context){
        remoteDatabase = new FirebasePicturesDatabase();
        localDatabase = new LocalPictureDatabase(context);
    }

    @Override
    public CompletableFuture<Location> getLocation(String uniqueId) {
        if (IS_ONLINE) {
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
        return null;
    }

    @Override
    public CompletableFuture<Map<String, Location>> getUserGuesses(String uniqueId) {
        return null;
    }

    @Override
    public CompletableFuture<Map<String, Double>> getScoreboard(String uniqueId) {
        return null;
    }

    @Override
    public CompletableFuture<Void> sendUserGuess(String uniqueId, String user, Location guessedLocation) {
        return null;
    }

    @Override
    public CompletableFuture<Bitmap> getBitmap(String uniqueId) {
        return null;
    }

    @Override
    public CompletableFuture<Void> uploadPicture(String uniqueId, String user, Location location, Uri uri) {
        return null;
    }

    @Override
    public CompletableFuture<Long> getKarma(String uniqueId) {
        return null;
    }

    @Override
    public CompletableFuture<Void> updateKarma(String uniqueId, int delta) {
        return null;
    }

    /**
     * Stores the picture in the internal storage
     * @param uniqueId id of the image
     * @param bmp bitmap of the image
     * @param realLocation real location of the image
     * @param guessedLocation location that the user guessed
     * @param scoreboard scoreboard of the image
     */
    public void storePictureLocally(String uniqueId, Bitmap bmp, Location realLocation, Location guessedLocation, Map<String, Double> scoreboard) throws IOException {
        localDatabase.storePictureAndMetadata(uniqueId, bmp, realLocation, guessedLocation, scoreboard);
    }

    /**
     * Update thescoreboard of the picture in the internal storage
     * @param scoreboard updated scoreboard
     */
    public void updateLocalScoreboard(String uniqueId, Map<String, Double> scoreboard){
        localDatabase.updateScoreboard(uniqueId, scoreboard);
    }

    /**
     *
     * @param uniqueId
     * @return
     */
    public Location getGuessedLocation(String uniqueId){
        return localDatabase.getGuessedLocation(uniqueId);
    }
}
