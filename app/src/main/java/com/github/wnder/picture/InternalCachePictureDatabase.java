package com.github.wnder.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;

import com.github.wnder.networkService.NetworkInformation;
import com.github.wnder.networkService.NetworkService;
import com.github.wnder.user.GlobalUser;

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
        remoteDatabase = new FirebasePicturesDatabase(context);
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
    public CompletableFuture<Void> sendUserGuess(String uniqueId, String user, Location guessedLocation, Bitmap mapSnapshot) throws IllegalStateException{
        if (isOnline()) {
            getBitmap(uniqueId).thenAccept(bmp -> {
                getLocation(uniqueId).thenAccept(location->{
                    getScoreboard(uniqueId).thenAccept(scoreboard ->{
                        storePictureLocally(new LocalPicture(uniqueId, bmp, mapSnapshot, location, guessedLocation, scoreboard));
                    });
                });
            });

            return remoteDatabase.sendUserGuess(uniqueId, user, guessedLocation, mapSnapshot);
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
            return localDatabase.getBitmap(uniqueId);
        }
    }

    @Override
    public CompletableFuture<Bitmap> getMapSnapshot(Context context, String uniqueId) {
        return getUserGuess(uniqueId).thenCompose((userLocation) ->
                getLocation(uniqueId).thenCompose((pictureLocation) ->
                        localDatabase.getMapSnapshot(context, userLocation, pictureLocation, uniqueId)));
    }

    @Override
    public CompletableFuture<Location> getUserGuess(String uniqueId) {
        Location userGuess = localDatabase.getGuessedLocation(uniqueId);
        CompletableFuture<Location> cf = new CompletableFuture<>();
        if (userGuess != null) {
            cf.complete(userGuess);
        } else if (isOnline()) {
            return remoteDatabase.getUserGuesses(uniqueId).thenApply((guesses) -> guesses.get(GlobalUser.getUser().getName()));
        }
        return cf;
    }

    @Override
    public CompletableFuture<Void> uploadPicture(String uniqueId, UploadInfo uploadInfo) throws IllegalStateException{
        return remoteDatabase.uploadPicture(uniqueId, uploadInfo);
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
        localDatabase.storePicture(picture);
    }

    /**
     * Update the scoreboard of the picture in the internal storage
     * @param scoreboard updated scoreboard
     */
    public void updateLocalScoreboard(String uniqueId, Map<String, Double> scoreboard){
        localDatabase.updateScoreboard(uniqueId, scoreboard);
    }

    /**
     * Deletes picture file AND metadata file
     * @param uniqueId uniqueId of picture
     */
    public void deleteLocalPicture(String uniqueId){
        localDatabase.deletePicture(uniqueId);
    }
}
