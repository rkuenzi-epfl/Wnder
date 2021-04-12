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
    public static Task<QuerySnapshot> downloadFromFirestore(String collection){
        return FirebaseFirestore.getInstance().collection(collection).get();
    }
}
