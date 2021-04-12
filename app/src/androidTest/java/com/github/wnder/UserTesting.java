package com.github.wnder;

import android.location.Location;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;
import com.github.wnder.user.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.android.gms.tasks.Tasks.await;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class UserTesting {

    @Test
    public void getAndSetRadiusWorks(){
        //Signed in user
        SignedInUser u = new SignedInUser("testUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
        u.setRadius(50);
        assertThat(u.getRadius(), is(50));

        //Guest user
        User u1 = GlobalUser.getUser();
        u1.setRadius(50);
        assertThat(u1.getRadius(), is(50));
        GlobalUser.resetUser();
    }

    private void ensureInRadius(String id, User u) throws InterruptedException, ExecutionException, TimeoutException {
        Task<DocumentSnapshot> task = Storage.downloadFromFirestore("pictures", id).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                float[] result = new float[1];
                //TODO: replace with leonard's location getter
                Location.distanceBetween((Double.parseDouble(documentSnapshot.get("latitude").toString())), Double.parseDouble(documentSnapshot.get("longitude").toString()), 0, 0, result);
                assertTrue(result[0] <= u.getRadius());
            }
        });

        await(task, 5, TimeUnit.SECONDS);
    }

    @Test
    public void getNewPictureForSignedInUserWorks() throws ExecutionException, InterruptedException, TimeoutException {
        SignedInUser u = new SignedInUser("testUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
        String pic = u.getNewPicture();

        //Check that it is not in user's uploaded and guessed pictures
        Set<String> upAdownPics = new HashSet<>();

        Task<DocumentSnapshot> task = Storage.downloadFromFirestore("users", "testUser").addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
        Task<QuerySnapshot> task1 = Storage.downloadCollectionFromFirestore("pictures").addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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

        //Ensure location is in radius
        ensureInRadius(pic, u);

        await(task, 5, TimeUnit.SECONDS);
        await(task1, 5, TimeUnit.SECONDS);
    }

    @Test
    public void getNewPictureForGuestUserWorks() throws ExecutionException, InterruptedException, TimeoutException {
        User u = GlobalUser.getUser();
        String pic = u.getNewPicture();

        Set<String> allPictures = new HashSet<>();
        Task<QuerySnapshot> task = Storage.downloadCollectionFromFirestore("pictures").addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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

        //Ensure location is in radius
        ensureInRadius(pic, u);

        await(task, 5, TimeUnit.SECONDS);
        GlobalUser.resetUser();
    }
}
