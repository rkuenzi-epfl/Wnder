package com.github.wnder.tour;

import android.location.Location;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TourDatabase {

    /**
     * Retrieves a tour pics from a tour unique id
     * @param tourUniqueId the tour unique id
     * @return a list of the pic ids in the tour
     */
    CompletableFuture<List<String>> getTourPics(String tourUniqueId);

    /**
     * Retrieves a tour name from a tour unique id
     * @param tourUniqueId the tour unique id
     * @return the associated tour name
     */
    CompletableFuture<String> getTourName(String tourUniqueId);

    /**
     * Gets the distance from the first image of a tour to the given place
     * @param tourUniqueId tour unique id
     * @param place place from which the distance to the tour will be computed
     * @return the distance between the given place and the tour's first picture position
     */
    CompletableFuture<Double> getTourDistance(String tourUniqueId, Location place);

    /**
     * Gets a tour length
     * @param tourUniqueId tour unique id
     * @return the length of the tour (meters)
     */
    CompletableFuture<Double> getTourLength(String tourUniqueId);

    /**
     * Uploads a tour to the database
     * THIS ONLY UPLOADS METADATA, MAKE SURE THE PICS CONCERNED PICS HAVE BEEN UPLOADED BEFORE USING THIS METHOD!!!!!!!
     * @param tourUniqueId unique id of the tour
     * @param tourName the tour name
     * @param picsUniqueIds the unique ids of the pics in the tour
     */
    CompletableFuture<Void> uploadTour(String tourUniqueId, String tourName, List<String> picsUniqueIds);

    /**
     * Creates a tour unique id from a tour name
     * @param tourName tour name
     * @return unique id for a tour
     */
    String generateTourUniqueId(String tourName);
}
