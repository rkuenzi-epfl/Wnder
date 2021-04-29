package com.github.wnder;

import android.location.Location;
import android.net.Uri;

import com.github.wnder.picture.NewPicture;
import com.github.wnder.picture.Picture;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class NewPictureTesting {

    private static NewPicture pic;
    private static String userName = "testUser";

    @BeforeClass
    public static void createPic() throws InterruptedException {
        Location loc = new Location("");
        loc.setLatitude(10d);
        loc.setLongitude(5d);
        pic = new NewPicture(userName, loc, Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
        CompletableFuture uploadStatus = pic.sendPictureToDb();
        try{
            // Make sure the picture finishes to upload before proceeding
            uploadStatus.get();
        } catch (Exception e){

        }
    }

    // Clean db
    @AfterClass
    public static void deleteTestPic() {
        FirebaseFirestore.getInstance().collection("pictures").document(pic.getUniqueId())
                .collection("userData").document("userGuesses").delete();
        FirebaseFirestore.getInstance().collection("pictures").document(pic.getUniqueId())
                .collection("userData").document("userScores").delete();
        FirebaseFirestore.getInstance().collection("pictures").document(pic.getUniqueId()).delete();
        FirebaseStorage.getInstance().getReference().child("pictures/"+pic.getUniqueId()+".jpg").delete();
    }

    @Test
    public void uniqueIdHasGoodFormat(){
        assertTrue(pic.getUniqueId().matches("testUser\\d+"));
    }

    @Test
    public void getUriWorks(){
        assertEquals(Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag), pic.getUri());
    }

    @Test
    public void onLocationAvailableWorksForNewPicture(){
        pic.onLocationAvailable((location)->{
            assertThat(location.getLatitude(), is(10d));
            assertThat(location.getLongitude(), is(5d));
        });
    }

    @Test
    public void scoreboardCorrectlyInitialized(){
        pic.onUpdatedScoreboardAvailable((scoreboard)->{
            assertThat(scoreboard.size(), is(1));
            assertTrue(scoreboard.containsKey(userName));
            assertThat(scoreboard.get(userName), is(-1d));
        });
    }

    @Test
    public void guessesCorrectlyInitialized(){
        pic.onUpdatedGuessesAvailable((userGuesses)->{
            assertThat(userGuesses.size(), is(1));
            assertTrue(userGuesses.containsKey(userName));
            assertThat(userGuesses.get(userName).getLatitude(), is(10d));
            assertThat(userGuesses.get(userName).getLongitude(), is(5d));
        });
    }

    @Test
    public void getKarmaWorks(){
        pic.onKarmaAvailable((k) -> {
            assertThat(k, is((long)0));
        });
    }
}
