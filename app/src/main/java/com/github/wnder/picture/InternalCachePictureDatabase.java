package com.github.wnder.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;

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

    public void storePictureLocally(String uniqueId, Location realLocation, Location guessedLocation, Map<String, Double> scoreboard){
        
    }
}
