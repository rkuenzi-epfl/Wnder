package com.github.wnder;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class Storage{
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;


    public Storage(){
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.storageRef = storage.getReference();
    }

    public Task<UploadTask.TaskSnapshot> uploadToCloudStorage(Uri uri, String databaseFilePath){
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();
        return storageRef.child(databaseFilePath).putFile(uri, metadata);
    }

    public Task<byte[]> downloadFromCloudStorage(String filepath) {
        return storageRef.child(filepath).getBytes(Long.MAX_VALUE);
    }

    public Task<Void> uploadToFirestore(Map<String, Object> map, String collection, String document){
        return db.collection(collection).document(document).set(map);
    }

    public Task<DocumentSnapshot> downloadFromFirestore(String collection, String document){
        return db.collection(collection).document(document).get();
    }

    /**
     * Uploads info to firestore
     * @param map info to upload
     * @param path path in the database(collection, document, collection, document)
     * @return the task
     */
    public Task<Void> uploadToFirestore(Map<String, Object> map, String[] path){
        return db.collection(path[0]).document(path[1]).collection(path[2]).document(path[3]).set(map);
    }

    /**
     * Downloads info from Firestore
     * @param path path in the database(collection, document, collection, document)
     * @return the task
     */
    public Task<DocumentSnapshot> downloadFromFirestore(String[] path){
        return db.collection(path[0]).document(path[1]).collection(path[2]).document(path[3]).get();
    }

    /**
     * Returns a future containing all the ids of all the currently uploaded pictures on the db
     * @return a future containing a set of string
     */
    public CompletableFuture<Set<String>> getIdsOfAllUploadedPictures(){
        CompletableFuture<Set<String>> idsToReturn = new CompletableFuture<>();
        Set<String> ids = new HashSet<>();

        //If success, complete the future, if failure, complete the future with an empty hashset
        db.collection("pictures").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                for(int i = 0; i < docs.size(); i++){
                    ids.add(docs.get(i).getId());
                }
                idsToReturn.complete(ids);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                idsToReturn.complete(new HashSet<>());
            }
        });
        return idsToReturn;
    }
}
