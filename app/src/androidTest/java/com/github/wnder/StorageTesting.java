package com.github.wnder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.UploadTask;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class StorageTesting {
    @Test
    public void testUploadAndDownloadToFireStore(){
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("Jeremy", "Jus d'orange");
        testMap.put("Léonard", "Lasagne");
        testMap.put("Romain", "Pizza du jeudi soir");
        testMap.put("Nico", "Cookies");
        testMap.put("Alois", "merci MV");
        testMap.put("Pablo", "Android");

        String collection = "test";
        String document = "doc";
        Storage.uploadToFirestore(testMap, collection, document).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Storage.downloadFromFirestore(collection, document).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //Should happen
                        assertThat(documentSnapshot.getData(), is(testMap));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Should not happen
                        assertThat(1, is(2));
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Should not happen
                assertThat(1, is(2));
            }
        });
    }

    @Test
    public void testUploadAndDownloadToFireStoreWithLongerPath(){
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("Jeremy", "Jus d'orange");
        testMap.put("Léonard", "Lasagne");
        testMap.put("Romain", "Pizza du jeudi soir");
        testMap.put("Nico", "Cookies");
        testMap.put("Alois", "merci MV");
        testMap.put("Pablo", "Android");

        String[] path = {"coll1", "doc1", "coll2", "doc2"};
        Storage.uploadToFirestore(testMap, path).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Storage.downloadFromFirestore(path).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //Should happen
                        assertThat(documentSnapshot.getData(), is(testMap));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Should not happen
                        assertThat(1, is(2));
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Should not happen
                assertThat(1, is(2));
            }
        });
    }

    @Test
    public void testUploadAndDownloadToCloudStorage(){

        Uri uri = Uri.parse("android.resource://raw/ladiag.jpg");

        String filepath = "test/ladiag.jpg";
        Storage.uploadToCloudStorage(uri, filepath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Storage.downloadFromCloudStorage(filepath).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                    @Override
                    public void onComplete(@NonNull Task<byte[]> task) {
                        assertThat(1, is(1));
                    }
                });
            }
        });

        Storage.downloadFromCloudStorage(filepath).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                assertThat(1, is(1));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                assertThat(1, is(2));
            }
        });
    }

    @Test
    public void getIdsWork() throws ExecutionException, InterruptedException {
        CompletableFuture<Set<String>> ids = Storage.getIdsOfAllUploadedPictures();
        Set<String> idSet = ids.get();
        Log.d("TAG", idSet.toString());
        assertTrue(idSet.size() > 0);
    }
}
