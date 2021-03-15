package com.github.wnder;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.OnCompleteListener;
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
                    Log.w("Error", "Error getting documents.", task.getException());
                }
            }
        });
    }

    @Test
    public void testUploadAndDownloadToCloudStorage(){

    }
}
