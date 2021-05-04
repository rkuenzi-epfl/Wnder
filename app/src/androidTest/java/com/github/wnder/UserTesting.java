package com.github.wnder;

import android.location.Location;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;
import com.github.wnder.user.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@RunWith(JUnit4.class)
public class UserTesting {

    @Rule
    public ActivityScenarioRule<MainActivity> testRule = new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule1 = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @Before
    public void initLoc(){
        Intents.init();
        onView(withId(R.id.getPictureButton)).perform(click());
    }

    @After
    public void releaseIntents(){
        Intents.release();
    }

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
                assertTrue(result[0] <= u.getRadius()*1000);
            }
        });
    }

    @Test
    public void getNewPictureForSignedInUserWorks() throws ExecutionException, InterruptedException, TimeoutException {
        SignedInUser realUser = new SignedInUser("testUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
        SignedInUser u = spy(realUser);
        u.setRadius(20000);
        Location loc = new Location("");
        loc.setLatitude(0);
        loc.setLongitude(0);
        GlobalUser.setUser(u);
        Set<String> allIds = new HashSet<>();
        allIds.add("testPicDontRm");
        doReturn(allIds).when(u).keepOnlyInRadius(any(), any(), any());

        u.onNewPictureAvailable(null, InstrumentationRegistry.getInstrumentation().getTargetContext(),(pic) -> {
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
            try {
                ensureInRadius(pic, u);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            GlobalUser.resetUser();
        });
    }

    @Test
    public void getNewPictureForGuestUserWorks() throws ExecutionException, InterruptedException, TimeoutException {
        User realUser = GlobalUser.getUser();
        User u = spy(realUser);


        Set<String> allIds = new HashSet<>();
        allIds.add("testPicDontRm");
        doReturn(allIds).when(u).keepOnlyInRadius(any(), any(), any());

        u.setRadius(20000);
        u.onNewPictureAvailable(null, null, (pic) -> {
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
            try {
                ensureInRadius(pic, u);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            GlobalUser.resetUser();
        });
    }
}
