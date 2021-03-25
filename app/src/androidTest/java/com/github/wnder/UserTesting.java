package com.github.wnder;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.android.gms.tasks.Tasks.await;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class UserTesting {

    @Test
    public void getNewPictureForSignedInUserWorks() throws ExecutionException, InterruptedException, TimeoutException {
        SignedInUser u = new SignedInUser("testUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
        String pic = u.getNewPicture();
        FirebaseFirestore storage = FirebaseFirestore.getInstance();

        //Check that it is not in user's uploaded and guessed pictures
        Set<String> upAdownPics = new HashSet<>();
        Task<DocumentSnapshot> task = storage.collection("users").document("testUser").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                List<String> guessedPictures = (List<String>) documentSnapshot.get("guessedPics");
                List<String> uploadedPictures = (List<String>) documentSnapshot.get("uploadedPics");
                if (guessedPictures == null) {
                    guessedPictures = new ArrayList<>();
                }
                if (uploadedPictures == null) {
                    uploadedPictures = new ArrayList<>();
                }
                upAdownPics.addAll(guessedPictures);
                upAdownPics.addAll(uploadedPictures);
                assertTrue(!upAdownPics.contains(pic));
            }
        });

        //Check that it's in the pool of pictures
        Set<String> allPictures = new HashSet<>();
        Task<QuerySnapshot> task1 = storage.collection("pictures").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                for(int i = 0; i < docs.size(); i++){
                    allPictures.add(docs.get(i).getId());
                }
                assertTrue(allPictures.contains(pic));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                assertTrue(false);
            }
        });
        await(task, 5, TimeUnit.SECONDS);
        await(task1, 5, TimeUnit.SECONDS);

    }

    @Test
    public void getNewPictureForGuestUserWorks() throws ExecutionException, InterruptedException, TimeoutException {
        User u = GlobalUser.getUser();
        String pic = u.getNewPicture();
        FirebaseFirestore storage = FirebaseFirestore.getInstance();

        Set<String> allPictures = new HashSet<>();
        Task<QuerySnapshot> task = storage.collection("pictures").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                for(int i = 0; i < docs.size(); i++){
                    allPictures.add(docs.get(i).getId());
                }
                assertTrue(allPictures.contains(pic));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                assertTrue(false);
            }
        });
        await(task, 5, TimeUnit.SECONDS);
        GlobalUser.resetUser();
    }
}
