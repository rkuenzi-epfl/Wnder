package com.github.wnder.picture;

import android.graphics.Bitmap;
import android.location.Location;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface PicturesDatabase {

    /**
     * Provides (later) location associated to a picture
     * @param uniqueId the picture unique Id
     * @return a Future of Location
     */
    public abstract CompletableFuture<Location> getLocation(String uniqueId);

    /**
     * Provides (later) location with purposely reduced precision associated to a picture
     * @param uniqueId the picture unique Id
     * @return a Future of Location
     */
    public abstract CompletableFuture<Location> getApproximateLocation(String uniqueId);

    /**
     * Provides (later) all user guesses associated to a picture
     * @param uniqueId the picture unique Id
     * @return a Future of all user guesses
     */
    public abstract CompletableFuture<Map<String, Location>> getUserGuesses(String uniqueId);

    /**
     * Provides (later) all user scores associated to a picture
     * @param uniqueId the picture unique Id
     * @return a Future of all user scores
     */
    public abstract CompletableFuture<Map<String, Double>> getScoreboard(String uniqueId);

    /**
     * Upload a user guess for a picture
     * The future should determine if the guess was successfully sent
     * @param uniqueId the picture unique Id
     * @param user the user guessing
     * @param guessedLocation the user's guessed location
     * @return a Future
     */
    public abstract CompletableFuture<Void> sendUserGuess(String uniqueId, String user, Location guessedLocation);

    /**
     * Provides (later) the bitmap of a picture
     * @param uniqueId the picture unique Id
     * @return a Future of Bitmap
     */
    public abstract CompletableFuture<Bitmap> getBitmap(String uniqueId);

    /**
     * Upload a picture with all it's initial information to the database
     * @return a Future
     */
    public abstract CompletableFuture<Void> uploadPicture(NewPicture picture);

    /**
     * Provides (later) karma associated to a picture
     * @param uniqueId the picture unique Id
     * @return a Future of karma
     */
    public abstract CompletableFuture<Long> getKarma(String uniqueId);

    /**
     * Update karma on the database
     * The future should only determine if the update was successful
     * @param uniqueId the picture unique Id
     * @param delta the karma to add/subtract
     * @return a Future
     */
    public abstract CompletableFuture<Void> updateKarma(String uniqueId, int delta);


}
