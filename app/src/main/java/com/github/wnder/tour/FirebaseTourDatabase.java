package com.github.wnder.tour;

import android.location.Location;
import android.util.Log;

import com.github.wnder.picture.FirebasePicturesDatabase;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class FirebaseTourDatabase implements TourDatabase{

    @Inject
    public FirebasePicturesDatabase db;

    private final CollectionReference tourCollection;

    public FirebaseTourDatabase() {
        tourCollection = FirebaseFirestore.getInstance().collection("tours");
    }

    @Override
    public CompletableFuture<List<String>> getTourPics(String tourUniqueId) {
        CompletableFuture<List<String>> tour = new CompletableFuture<>();

        tourCollection.document(tourUniqueId).get().
            addOnSuccessListener((documentSnapshot) -> {
                List<String> picIds = (List<String>) documentSnapshot.get("tourPics");
                tour.complete(picIds);
            })
            .addOnFailureListener(tour::completeExceptionally);

        return tour;
    }

    @Override
    public CompletableFuture<String> getTourName(String tourUniqueId) {
        CompletableFuture<String> tourNameFuture = new CompletableFuture<>();

        tourCollection.document(tourUniqueId).get().
            addOnSuccessListener((documentSnapshot) -> {
                String tourName = documentSnapshot.getString("tourName");
                tourNameFuture.complete(tourName);
            })
            .addOnFailureListener(tourNameFuture::completeExceptionally);

        return tourNameFuture;
    }

    @Override
    public CompletableFuture<Double> getTourDistance(String tourUniqueId, LatLng distanceTo){
        CompletableFuture<Double> distanceFuture = new CompletableFuture<>();

        tourCollection.document(tourUniqueId).get().
                addOnSuccessListener((documentSnapshot) -> {
                    double latitude = documentSnapshot.getDouble("tourFirstLat");
                    double longitude = documentSnapshot.getDouble("tourFirstLong");

                    LatLng tourLocation = new LatLng(latitude, longitude);

                    double distance = tourLocation.distanceTo(distanceTo);

                    distanceFuture.complete(distance);
                })
                .addOnFailureListener(distanceFuture::completeExceptionally);

        return distanceFuture;
    }

    @Override
    public CompletableFuture<Double> getTourLength(String tourUniqueId){
        CompletableFuture<Double> tourLengthFuture = new CompletableFuture<>();

        tourCollection.document(tourUniqueId).get().
                addOnSuccessListener((documentSnapshot) -> {
                    double tourLength = documentSnapshot.getDouble("tourLength");
                    tourLengthFuture.complete(tourLength);
                })
                .addOnFailureListener(tourLengthFuture::completeExceptionally);

        return tourLengthFuture;
    }

    @Override
    public CompletableFuture<Void> uploadTour(String tourUniqueId, String tourName, List<String> picsUniqueIds) {

        CompletableFuture<Void> nameUpload = uploadTourName(tourUniqueId, tourName);
        CompletableFuture<Void> picsUpload = uploadTourPics(tourUniqueId, picsUniqueIds);
        CompletableFuture<Void> firstLocUpload = uploadTourFirstLocation(tourUniqueId, picsUniqueIds.get(0));
        CompletableFuture<Void> lengthUpload = uploadTourLength(tourUniqueId, picsUniqueIds);

        return CompletableFuture.allOf(nameUpload, picsUpload, firstLocUpload, lengthUpload);
    }

    /**
     * Uploads a tour name to the db
     * @param tourUniqueId tour unique id
     * @param tourName tour name
     * @return a future completing when transaction is done
     */
    private CompletableFuture<Void> uploadTourName(String tourUniqueId, String tourName){
        CompletableFuture<Void> uploadTourNameFuture = new CompletableFuture<>();

        Map<String, String> tourNameMap = new HashMap<>();
        tourNameMap.put("tourName", tourName);

        tourCollection.document(tourUniqueId).set(tourNameMap, SetOptions.merge())
                .addOnSuccessListener(result -> uploadTourNameFuture.complete(null))
                .addOnFailureListener(uploadTourNameFuture::completeExceptionally);

        return uploadTourNameFuture;
    }

    /**
     * Uploads a tour pic list in the db
     * @param tourUniqueId tour unique id
     * @param tourPics tour pics
     * @return a future completing when transaction is done
     */
    private CompletableFuture<Void> uploadTourPics(String tourUniqueId, List<String> tourPics){
        CompletableFuture<Void> uploadTourPicsFuture = new CompletableFuture<>();

        Map<String, List<String>> tourPicsMap = new HashMap<>();
        tourPicsMap.put("tourPics", tourPics);

        tourCollection.document(tourUniqueId).set(tourPicsMap, SetOptions.merge())
                .addOnSuccessListener(result -> uploadTourPicsFuture.complete(null))
                .addOnFailureListener(uploadTourPicsFuture::completeExceptionally);

        return uploadTourPicsFuture;
    }

    /**
     * Uploads a tour first position in the db
     * @param tourUniqueId tour unique id
     * @param firstPicId id of the first pic of the tour
     * @return a future completing when transaction is done
     */
    private CompletableFuture<Void> uploadTourFirstLocation(String tourUniqueId, String firstPicId){
        CompletableFuture<Void> uploadLocFuture = new CompletableFuture<>();

        db.getLocation(firstPicId).thenAccept(location -> {
            Map<String, Double> tourFirstLoc = new HashMap<>();
            tourFirstLoc.put("tourFirstLat", location.getLatitude());
            tourFirstLoc.put("tourFirstLong", location.getLongitude());

            tourCollection.document(tourUniqueId).set(tourFirstLoc, SetOptions.merge())
                    .addOnSuccessListener(result -> uploadLocFuture.complete(null))
                    .addOnFailureListener(uploadLocFuture::completeExceptionally);
        });

        return uploadLocFuture;
    }

    /**
     * Uploads a tour length
     * @param tourUniqueId tour unique id
     * @param picIds ids of the pics in the tour (in order)
     * @return a future completing when the transaction is done
     */
    private CompletableFuture<Void> uploadTourLength(String tourUniqueId, List<String> picIds){
        CompletableFuture<Void> uploadLengthFuture = new CompletableFuture<>();
        CompletableFuture<Double>[] lengthsBetweenPics = new CompletableFuture[picIds.size() - 1];

        double[] allLengths = new double[picIds.size() - 1];

        //Get all the distances between the pics
        for(int i = 0; i < picIds.size() - 1; i++){
            final int id = i;
            getDistBetweenTwoPics(picIds.get(i), picIds.get(i+1)).thenAccept(distance -> {
                allLengths[id] = distance;
                lengthsBetweenPics[id].complete(distance);
            });
        }

        //Once all distances gotten, compute total length
        CompletableFuture.allOf(lengthsBetweenPics).thenAccept(result -> {
           double totalLength = 0;
           for(int i = 0; i < allLengths.length; i++){
               totalLength += allLengths[i];
           }

           //Once total length computed, upload it
            Map<String, Double> tourLength = new HashMap<>();
            tourLength.put("tourLength", totalLength);

            tourCollection.document(tourUniqueId).set(tourLength, SetOptions.merge())
                    .addOnSuccessListener(res -> uploadLengthFuture.complete(null))
                    .addOnFailureListener(uploadLengthFuture::completeExceptionally);
        });

        return uploadLengthFuture;
    }

    /**
     * Gets the distance between two pics
     * @param firstPicId first pic
     * @param secondPicId second pic
     * @return distance between first and second pic
     */
    private CompletableFuture<Double> getDistBetweenTwoPics(String firstPicId, String secondPicId){
        CompletableFuture<Double> distanceFuture = new CompletableFuture<>();

        db.getLocation(firstPicId).thenAccept(location1 -> {
            db.getLocation(secondPicId).thenAccept(location2 -> {
               double distance = location1.distanceTo(location2);

               distanceFuture.complete(distance);
            });
        });

        return distanceFuture;
    }

    @Override
    public String generateTourUniqueId(String tourName){
        return tourName + Calendar.getInstance().getTimeInMillis();
    }
}
