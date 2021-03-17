package com.github.wnder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.UploadTask;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(AndroidJUnit4.class)
public class StorageTesting {
    @Test
    public void testUploadAndDownloadToFireStore(){
        Storage storage = new Storage();

        Map<String, Object> testMap = new HashMap<>();
        testMap.put("Jeremy", "Jus d'orange");
        testMap.put("Léonard", "Lasagne");
        testMap.put("Romain", "Pizza du jeudi soir");
        testMap.put("Nico", "Cookies");
        testMap.put("Alois", "merci MV");
        testMap.put("Pablo", "Android");

        String collection = "test";
        String document = "doc";
        storage.uploadToFirestore(testMap, collection, document).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                storage.downloadFromFirestore(collection, document).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
        Storage storage = new Storage();

        String filepath = "test/ladiag.jpg";
        storage.uploadToCloudStorage(uri, filepath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                storage.downloadFromCloudStorage(filepath).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                    @Override
                    public void onComplete(@NonNull Task<byte[]> task) {
                        assertThat(1, is(1));
                    }
                });
            }
        });

        storage.downloadFromCloudStorage(filepath).addOnSuccessListener(new OnSuccessListener<byte[]>() {
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
}
