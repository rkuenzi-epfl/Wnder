package com.github.wnder.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface PicturesDatabase {

    /**
     * Provides (later) location associated to a picture
     * @param uniqueId the picture unique Id
     * @return a Future of Location
     */
    CompletableFuture<Location> getLocation(String uniqueId);

    /**
     * Provides (later) location with purposely reduced precision associated to a picture
     * @param uniqueId the picture unique Id
     * @return a Future of Location
     */
    CompletableFuture<Location> getApproximateLocation(String uniqueId);

    /**
     * Provides (later) all user guesses associated to a picture
     * @param uniqueId the picture unique Id
     * @return a Future of all user guesses
     */
    CompletableFuture<Map<String, Location>> getUserGuesses(String uniqueId);

    /**
     * Provides (later) all user scores associated to a picture
     * @param uniqueId the picture unique Id
     * @return a Future of all user scores
     */
    CompletableFuture<Map<String, Double>> getScoreboard(String uniqueId);

    /**
     * Upload a user guess for a picture
     * The future should determine if the guess was successfully sent
     * @param uniqueId the picture unique Id
     * @param user the user guessing
     * @param guessedLocation the user's guessed location
     * @param mapSnapshot a snapshot of the map with the markers
     * @return a Future
     */
    CompletableFuture<Void> sendUserGuess(String uniqueId, String user, Location guessedLocation, Bitmap mapSnapshot);

    /**
     * Provides (later) the bitmap of a picture
     * @param uniqueId the picture unique Id
     * @return a Future of Bitmap
     */
    CompletableFuture<Bitmap> getBitmap(String uniqueId);

    /**
     * Provides (later) the map snapshot of a picture
     * @param uniqueId the picture unique Id
     * @return a Future of Bitmap
     */
    CompletableFuture<Bitmap> getMapSnapshot(Context context, String uniqueId);

    /**
     * Provides (later) a user guess for a picture
     * @param uniqueId id of the image
     * @return a Future of Location
     */
    CompletableFuture<Location> getUserGuess(String uniqueId);

    /**
     * Upload a picture with all it's initial information to the database
     * @return a Future
     */
    CompletableFuture<Void> uploadPicture(String uniqueId, String user, Location location, Uri uri);

    /**
     * Provides (later) karma associated to a picture
     * @param uniqueId the picture unique Id
     * @return a Future of karma
     */
    CompletableFuture<Long> getKarma(String uniqueId);

    /**
     * Update karma on the database
     * The future should only determine if the update was successful
     * @param uniqueId the picture unique Id
     * @param delta the karma to add/subtract
     * @return a Future
     */
    CompletableFuture<Void> updateKarma(String uniqueId, int delta);
}
