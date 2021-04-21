package com.github.wnder;

import android.location.Location;
import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class Storage{
    private Storage(){
        //Non-instanciable class
    }

    public static Task<UploadTask.TaskSnapshot> uploadToCloudStorage(Uri uri, String databaseFilePath){
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();
        return storageRef.child(databaseFilePath).putFile(uri, metadata);
    }

    public static Task<byte[]> downloadFromCloudStorage(String filepath) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        return storageRef.child(filepath).getBytes(Long.MAX_VALUE);
    }

    public static Task<Void> uploadToFirestore(Map<String, Object> map, String collection, String document){
        return FirebaseFirestore.getInstance().collection(collection).document(document).set(map);
    }

    /**
     * Update a field in a given document in a given collection on firestore
     * @param collection the collection in which to update
     * @param document the document to update
     * @param field the field to update
     * @param newValue the new value
     * @return the task
     */
    public static Task<Void> updateOnFirestore(String collection, String document, String field, Object newValue){
        return FirebaseFirestore.getInstance().collection(collection).document(document).update(field, newValue);
    }

    public static Task<DocumentSnapshot> downloadFromFirestore(String collection, String document){
        return FirebaseFirestore.getInstance().collection(collection).document(document).get();
    }

    public static Task<QuerySnapshot> downloadCollectionFromFirestore(String collection){
        return FirebaseFirestore.getInstance().collection(collection).get();
    }

    /**
     * Uploads info to firestore
     * @param map info to upload
     * @param path path in the database(collection, document, collection, document)
     * @return the task
     */
    public static Task<Void> uploadToFirestore(Map<String, Object> map, String[] path){
        return FirebaseFirestore.getInstance().collection(path[0]).document(path[1]).collection(path[2]).document(path[3]).set(map);
    }

    /**
     * Downloads info from Firestore
     * @param path path in the database(collection, document, collection, document)
     * @return the task
     */
    public static Task<DocumentSnapshot> downloadFromFirestore(String[] path){
        return FirebaseFirestore.getInstance().collection(path[0]).document(path[1]).collection(path[2]).document(path[3]).get();
    }

    /**
     * Returns a future containing all the ids of all the currently uploaded pictures on the db
     * @return a future containing a set of string
     */
    public static CompletableFuture<Set<String>> getIdsOfAllUploadedPictures(){
        CompletableFuture<Set<String>> idsToReturn = new CompletableFuture<>();
        Set<String> ids = new HashSet<>();

        //If success, complete the future, if failure, complete the future with an empty hashset
        FirebaseFirestore.getInstance().collection("pictures").get().addOnSuccessListener((queryDocumentSnapshots) -> {
            List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
            for(int i = 0; i < docs.size(); i++){
                ids.add(docs.get(i).getId());
            }
            idsToReturn.complete(ids);
        }).addOnFailureListener((exception) -> {
            idsToReturn.complete(new HashSet<>());
        });

        return idsToReturn;
    }

    /**
     * Returns a future containing all the ids and locations of all the currently uploaded pictures on the db
     * @return a future containing a map between strings and locations
     */
    public static void onIdsAndLocAvailable(Consumer<Map<String, Location>> idsAndLocsAvailable){
        Map<String, Location> idsAndLoc = new HashMap<>();

        //If success, complete the future, if failure, complete the future with an empty hashmap
        FirebaseFirestore.getInstance().collection("pictures").get().addOnSuccessListener((queryDocumentSnapshots) -> {
            List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
            for(int i = 0; i < docs.size(); i++){
                Location loc = new Location("");
                loc.setLatitude(docs.get(i).getDouble("latitude"));
                loc.setLongitude(docs.get(i).getDouble("longitude"));

                idsAndLoc.put(docs.get(i).getId(), loc);
            }
            idsAndLocsAvailable.accept(idsAndLoc);
        }).addOnFailureListener((exception) -> {
            idsAndLocsAvailable.accept(new HashMap<>());
        });
    }

    /**
     * Returns a future containing all the ids and karma of all the currently uploaded pictures on the db
     * @return a future containing a map between strings and karma
     */
    public static void onIdsAndKarmaAvailable(Consumer<Map<String, Long>> idsAndKarmaAvailable){
        Map<String, Long> idsAndKarma = new HashMap<>();

        //If success, complete the future, if failure, complete the future with an empty hashmap
        FirebaseFirestore.getInstance().collection("pictures").get().addOnSuccessListener((queryDocumentSnapshots) -> {
            List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
            for(int i = 0; i < docs.size(); i++){
                long karma;
                try{
                    //If a picture doesn't have the karma field
                    karma = (long)docs.get(i).get("karma");
                }
                catch (NullPointerException nullPo){
                    //Consider it to be zero
                    karma = 0;
                }


                idsAndKarma.put(docs.get(i).getId(), karma);
            }
            idsAndKarmaAvailable.accept(idsAndKarma);
        }).addOnFailureListener((exception) -> {
            idsAndKarmaAvailable.accept(new HashMap<>());
        });
    }
}
