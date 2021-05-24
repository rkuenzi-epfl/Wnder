package com.github.wnder;

import android.location.Location;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import com.github.wnder.picture.FirebasePicturesDatabase;
import com.github.wnder.picture.UploadInfo;
import com.github.wnder.tour.FirebaseTourDatabase;
import com.github.wnder.tour.TourDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.lang.Thread.sleep;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(JUnit4.class)
public class TourDatabaseTest {

    private static FirebaseTourDatabase tdb;
    private static String tourUniqueId;
    private static String tourName;
    private static String firstUniqueId;
    private static String secondUniqueId;
    private static String thirdUniqueId;
    private static List<String> pictures;
    private static Location firstLoc;
    private static double totalLength;

    @BeforeClass
    public static void createTestTour() {

        tdb = new FirebaseTourDatabase(ApplicationProvider.getApplicationContext());
        FirebasePicturesDatabase db = new FirebasePicturesDatabase(ApplicationProvider.getApplicationContext());

        firstLoc = new Location("");
        firstLoc.setLatitude(0);
        firstLoc.setLongitude(0);

        Location secondLoc = new Location("");
        secondLoc.setLatitude(10);
        secondLoc.setLongitude(1);

        Location thirdLoc = new Location("");
        thirdLoc.setLatitude(5);
        thirdLoc.setLongitude(2);

        String user = "testUser";
        firstUniqueId = "first" + user + Calendar.getInstance().getTimeInMillis();
        secondUniqueId = "second" + user + Calendar.getInstance().getTimeInMillis();
        thirdUniqueId = "third" + user + Calendar.getInstance().getTimeInMillis();

        CompletableFuture<Void> uploadFirstPic = db.uploadPicture(firstUniqueId, new UploadInfo(user, firstLoc, Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
        CompletableFuture<Void> uploadSecondPic = db.uploadPicture(secondUniqueId, new UploadInfo(user, secondLoc, Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
        CompletableFuture<Void> uploadThirdPic = db.uploadPicture(thirdUniqueId, new UploadInfo(user, thirdLoc, Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));

        try {
            uploadFirstPic.get();
            uploadSecondPic.get();
            uploadThirdPic.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        tourName = "testTour";
        tourUniqueId = tdb.generateTourUniqueId(tourName);

        pictures = new ArrayList<>();
        pictures.add(firstUniqueId);
        pictures.add(secondUniqueId);
        pictures.add(thirdUniqueId);

        totalLength = 0;
        totalLength += firstLoc.distanceTo(secondLoc);
        totalLength += secondLoc.distanceTo(thirdLoc);

        CompletableFuture<Void> uploadTour = tdb.uploadTour(tourUniqueId, tourName, pictures);

        try{
            uploadTour.get();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    // Clean db
    @AfterClass
    public static void deleteTestTour() {
        FirebaseFirestore.getInstance().collection("pictures").document(firstUniqueId)
                .collection("userData").document("userGuesses").delete();
        FirebaseFirestore.getInstance().collection("pictures").document(firstUniqueId)
                .collection("userData").document("userScores").delete();
        FirebaseFirestore.getInstance().collection("pictures").document(firstUniqueId).delete();
        FirebaseStorage.getInstance().getReference().child("pictures/"+firstUniqueId+".jpg").delete();

        FirebaseFirestore.getInstance().collection("pictures").document(secondUniqueId)
                .collection("userData").document("userGuesses").delete();
        FirebaseFirestore.getInstance().collection("pictures").document(secondUniqueId)
                .collection("userData").document("userScores").delete();
        FirebaseFirestore.getInstance().collection("pictures").document(secondUniqueId).delete();
        FirebaseStorage.getInstance().getReference().child("pictures/"+secondUniqueId+".jpg").delete();

        FirebaseFirestore.getInstance().collection("pictures").document(thirdUniqueId)
                .collection("userData").document("userGuesses").delete();
        FirebaseFirestore.getInstance().collection("pictures").document(thirdUniqueId)
                .collection("userData").document("userScores").delete();
        FirebaseFirestore.getInstance().collection("pictures").document(thirdUniqueId).delete();
        FirebaseStorage.getInstance().getReference().child("pictures/"+thirdUniqueId+".jpg").delete();

        FirebaseFirestore.getInstance().collection("tours").document(tourUniqueId).delete();
    }

    @Test
    public void getTourPicsWorks(){
        List<String> pics = new ArrayList<>();
        try {
            pics = tdb.getTourPics(tourUniqueId).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertThat(pics.size(), is(3));
        assertThat(pics.get(0), is(pictures.get(0)));
        assertThat(pics.get(1), is(pictures.get(1)));
        assertThat(pics.get(2), is(pictures.get(2)));
    }

    @Test
    public void getTourNameWorks(){
        String name = "";
        try {
            name = tdb.getTourName(tourUniqueId).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertThat(name, is(tourName));
    }

    @Test
    public void getTourDistanceWorks(){
        double distToLat = 10;
        double distToLong = 10;
        Location location = new Location("");
        location.setLatitude(distToLat);
        location.setLongitude(distToLong);
        double dist = 0;
        try {
            dist = tdb.getTourDistance(tourUniqueId, location).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        double realDist = location.distanceTo(firstLoc);
        assertThat(dist, is(realDist));
    }

    @Test
    public void getTourLengthWorks(){
        double l = 0;
        try {
            l = tdb.getTourLength(tourUniqueId).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertThat(l, is(totalLength));
    }
}
