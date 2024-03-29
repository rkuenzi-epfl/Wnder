package com.github.wnder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import com.github.wnder.picture.InternalCachePictureDatabase;
import com.github.wnder.picture.UploadInfo;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
    public void successfullyAddGuessesAndScore() {
        Map<String, Location> guesses = new HashMap<>();
        Map<String, Double> scoreboard = new HashMap<>();
        try {
            guesses = ICPD.getUserGuesses(uniqueId).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(guesses.containsKey(user));
        Location loc = guesses.get(user);
        assertThat(loc.getLatitude(), is(location.getLatitude()));
        assertThat(loc.getLongitude(), is(location.getLongitude()));

        Location otherLoc = new Location("");
        otherLoc.setLatitude(20);
        otherLoc.setLongitude(22);
        String otherUser = "otherUser";
        Bitmap mapSnapshot = BitmapFactory.decodeResource(ApplicationProvider.getApplicationContext().getResources(), R.raw.picture1);
        try {
            ICPD.sendUserGuess(uniqueId, otherUser, otherLoc, mapSnapshot).get();
            guesses = ICPD.getUserGuesses(uniqueId).get();
            scoreboard = ICPD.getScoreboard(uniqueId).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(guesses.containsKey(user));
        loc = guesses.get(user);
        assertThat(loc.getLatitude(), is(location.getLatitude()));
        assertThat(loc.getLongitude(), is(location.getLongitude()));

        assertTrue(guesses.containsKey(otherUser));
        Location otherLocRes = guesses.get(otherUser);
        assertThat(otherLocRes.getLatitude(), is(otherLoc.getLatitude()));
        assertThat(otherLocRes.getLongitude(), is(otherLoc.getLongitude()));

        assertTrue(scoreboard.containsKey(otherUser));
        assertThat(scoreboard.get(otherUser), is(Score.computeScore(location, otherLoc)));
    }

    @Test
    public void successfullyUpdateAndGetKarma() {
        Long karma = 123456789L;
        try {
            karma = ICPD.getKarma(uniqueId).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertThat(karma, is(0L));
        try {
            ICPD.updateKarma(uniqueId, -5).get();
            karma = ICPD.getKarma(uniqueId).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertThat(karma, is(-5L));
        try {
            ICPD.updateKarma(uniqueId, 10).get();
            karma = ICPD.getKarma(uniqueId).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertThat(karma, is(5L));

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
