package com.github.wnder.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class InternalCachePictureDatabase implements PicturesDatabase{
//uniqueId, bitmap, realLocation, guessLocation, scoreboard
    private FirebasePicturesDatabase remoteDatabase;
    private LocalPictureDatabase localDatabase;

    private boolean IS_ONLINE = true;

    public InternalCachePictureDatabase(Context context){
        remoteDatabase = new FirebasePicturesDatabase();
        localDatabase = new LocalPictureDatabase(context);
    }

    @Override
    public CompletableFuture<Location> getLocation(String uniqueId) {
        return null;
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

    public void updateLocalScoreboard(){

    }
}
