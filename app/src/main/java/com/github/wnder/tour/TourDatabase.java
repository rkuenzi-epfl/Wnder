package com.github.wnder.tour;

import com.mapbox.mapboxsdk.geometry.LatLng;

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
     * Gets the distance from the first image of a tour to the given distance
     * @param tourUniqueId tour unique id
     * @param distanceTo place from which the distance to the tour will be computed
     * @return the distance between the given dist and the tour's first picture position
     */
    CompletableFuture<Double> getTourDistance(String tourUniqueId, LatLng distanceTo);

    /**
     * Gets a tour length
     * @param tourUniqueId tour unique id
     * @return the length of the tour (meters)
     */
    CompletableFuture<Double> getTourLength(String tourUniqueId);

    /**
     * Uploads a tour to the database
     * @param tourName the tour name
     * @param picsUniqueIds the unique ids of the pics in the tour
     */
    CompletableFuture<Void> uploadTour(String tourName, List<String> picsUniqueIds);
}
