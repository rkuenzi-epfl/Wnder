package com.github.wnder;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
        storage.uploadToFirestore(testMap, collection);
        storage.downloadFromFirestore(collection).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot documents = task.getResult();
                    if(documents.size() == 0){
                        assertThat(1, is(2));
                    }
                    for (QueryDocumentSnapshot doc : documents) {
                        assertThat(testMap, is(doc.getData()));
                    }
                } else {
                    assertThat(1, is(2));
                }
            }
        });
    }

    @Test
    public void testUploadAndDownloadToCloudStorage(){
        Uri uri = Uri.parse("android.resource://raw/ladiag.jpg");
        Storage storage = new Storage();

        String filepath = "test/img1.jpg";
        System.out.println("aille");
        storage.uploadToCloudStorage(uri, filepath);

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
