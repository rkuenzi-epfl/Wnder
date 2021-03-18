package com.github.wnder;

import android.net.Uri;

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

import java.util.Map;

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

    public Task<Void> uploadToFirestore(Map<String, Object> map, String collection1, String collection2, String document1, String document2){
        return db.collection(collection1).document(document1).collection(collection2).document(document2).set(map);
    }

    public Task<DocumentSnapshot> downloadFromFirestore(String collection1, String collection2, String document1, String document2){
        return db.collection(collection1).document(document1).collection(collection2).document(document2).get();
    }
}
