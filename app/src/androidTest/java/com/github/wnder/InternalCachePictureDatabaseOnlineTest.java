package com.github.wnder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import com.github.wnder.picture.InternalCachePictureDatabase;
import com.github.wnder.picture.UploadInfo;
import com.github.wnder.user.SignedInUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

//Those tests are essentially the same as the ones in FirebasePicturesDatabaseTest, maybe deleting it would be better
//TODO delete this later
@RunWith(JUnit4.class)
public class InternalCachePictureDatabaseOnlineTest {

    private static Context context = ApplicationProvider.getApplicationContext();
    private static InternalCachePictureDatabase ICPD;

    private static Location location;
    private static String uniqueId;
    private static String user;

    @BeforeClass
    public static void setup(){
        //Mock online check
        ICPD = Mockito.spy(new InternalCachePictureDatabase(context));
        Mockito.doReturn(true).when(ICPD).isOnline();

        location = new Location("");
        location.setLatitude(10);
        location.setLongitude(15);
        user = "testUser";
        uniqueId = user + Calendar.getInstance().getTimeInMillis();
        CompletableFuture<Void> uploaded = ICPD.uploadPicture(uniqueId, new UploadInfo(user, location, Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
        try {
            uploaded.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Clean db
    @AfterClass
    public static void deleteTestPic() {
        FirebaseFirestore.getInstance().collection("pictures").document(uniqueId)
                .collection("userData").document("userGuesses").delete();
        FirebaseFirestore.getInstance().collection("pictures").document(uniqueId)
                .collection("userData").document("userScores").delete();
        FirebaseFirestore.getInstance().collection("pictures").document(uniqueId).delete();
        FirebaseStorage.getInstance().getReference().child("pictures/"+uniqueId+".jpg").delete();
    }

    @Test
    public void getLocationWorks(){
        Location loc = new Location("");
        loc.setLatitude(80);
        loc.setLongitude(80);
        try {
            loc = ICPD.getLocation(uniqueId).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertThat(loc.getLatitude(), is(location.getLatitude()));
        assertThat(loc.getLongitude(), is(location.getLongitude()));
    }

    @Test
    public void getValidApproximateLocation() {
        double radius = 200; // meters
        double epsilon = 10; // meters
        Location loc = new Location("");
        // This should fail the test if we don't get a result I guess
        loc.setLatitude(80);
        loc.setLongitude(80);
        try {
            loc = ICPD.getApproximateLocation(uniqueId).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(loc.distanceTo(location) < radius + epsilon);
    }

    @Test
    public void successfullyAddGuessesAndScore() {
        List<Map.Entry<String, Location>> guesses = new ArrayList<>();
        List<Map.Entry<String, Double>> scoreboard = new ArrayList<>();
        try {
            guesses = ICPD.getUserGuesses(uniqueId).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Location otherLoc = new Location("");
        otherLoc.setLatitude(20);
        otherLoc.setLongitude(22);
        SignedInUser otherUser = new SignedInUser("otherUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag), "otherUser");
        Bitmap mapSnapshot = BitmapFactory.decodeResource(ApplicationProvider.getApplicationContext().getResources(), R.raw.picture1);
        try {
            ICPD.sendUserGuess(uniqueId, otherUser, otherLoc, mapSnapshot).get();
            guesses = ICPD.getUserGuesses(uniqueId).get();
            scoreboard = ICPD.getScoreboard(uniqueId).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Location otherLocRes = null;
        for(Map.Entry<String, Location> e: guesses){
            if(e.getKey().equals(otherUser.getName())){
                otherLocRes = e.getValue();
            }
        }
        assertNotNull(otherLocRes);
        assertThat(otherLocRes.getLatitude(), is(otherLoc.getLatitude()));
        assertThat(otherLocRes.getLongitude(), is(otherLoc.getLongitude()));

        Double score = null;
        for(Map.Entry<String, Double> e: scoreboard){
            if(e.getKey().equals(otherUser.getName())){
                score = e.getValue();
            }
        }
        assertNotNull(score);
        assertThat(score, is(Score.computeScore(location, otherLoc)));
    }

    @Test
    public void successfullyUpdateAndGetKarma() {
        Long karma = 123456789L;
        try {
            karma = ICPD.getKarma(uniqueId).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertThat(karma, is(100L));
        try {
            ICPD.updateKarma(uniqueId, -5).get();
            karma = ICPD.getKarma(uniqueId).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertThat(karma, is(95L));
        try {
            ICPD.updateKarma(uniqueId, 10).get();
            karma = ICPD.getKarma(uniqueId).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertThat(karma, is(105L));

    }

    @Test
    public void bitmapIsUploaded() {
        Bitmap bmp = null;
        try {
            bmp = ICPD.getBitmap(uniqueId).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Check random "easily avilable" property of the picture
        assertThat(bmp.getWidth(), is(192));
    }

}
