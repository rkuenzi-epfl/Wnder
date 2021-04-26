package com.github.wnder;

import android.graphics.Bitmap;
import android.location.Location;

import com.github.wnder.picture.ExistingPicture;

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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

@RunWith(JUnit4.class)
public class FirebasePicturesDatabaseTest {

    private static ExistingPicture testPic;
    private static Location loc;

    @BeforeClass
    public static void getTestPic(){
        testPic = new ExistingPicture("picture1");
        loc = new Location("");
        loc.setLatitude(10d);
        loc.setLongitude(10d);
        CompletableFuture guessSentResult = testPic.sendUserGuess("testUser", loc);
        CompletableFuture karmaResult = testPic.updateKarma(-1);

        try{
            // Make sure the picture finishes to upload before proceeding
            guessSentResult.get();
            karmaResult.get();
        } catch (Exception e){

        }
    }

    @Test
    public void onLocationAvailableWorksForExistingPicture(){
        testPic.onLocationAvailable((location)->{
            assertThat(location.getLatitude(), is(10d));
            assertThat(location.getLongitude(), is(10d));
        });
    }

    @Test
    public void onBitmapAvailableWorks(){
        testPic.onBitmapAvailable((bmp)->{
            assertTrue(bmp instanceof Bitmap);
        });

        // Verify that it is properly accessible once it's stored in the object
        testPic.onBitmapAvailable((bmp)->{
            assertTrue(bmp instanceof Bitmap);
        });
    }

    @Test
    public void userGuessIsProperlyStored(){

        testPic.onUpdatedGuessesAvailable((userGuesses)->{
            assertTrue(userGuesses.containsKey("testUser"));
            assertThat(userGuesses.get("testUser").getLatitude(), is(10d));
            assertThat(userGuesses.get("testUser").getLongitude(), is(10d));
        });

        testPic.onUpdatedScoreboardAvailable((scoreboard)->{
            assertTrue(scoreboard.containsKey("testUser"));

            Location guessedLoc = new Location("");
            guessedLoc.setLatitude(10d);
            guessedLoc.setLongitude(10d);
            assertThat(scoreboard.get("testUser"), is(Score.computeScore(loc, guessedLoc)));
        });
    }

    @Test
    public void scoreboardCorrectlyInitialized(){
        testPic.onUpdatedScoreboardAvailable((scoreboard)->{
            assertTrue(scoreboard.containsKey("user1"));
            assertTrue(scoreboard.containsKey("user3"));
            assertThat(scoreboard.get("user2"), is(13d));
            assertThat(scoreboard.get("user4"), is(9d));
        });
    }

    @Test
    public void guessesCorrectlyInitialized(){
        testPic.onUpdatedGuessesAvailable((userGuesses)->{
            assertTrue(userGuesses.containsKey("user0"));
            assertThat(userGuesses.get("user0").getLatitude(), is(10d));
            assertThat(userGuesses.get("user0").getLongitude(), is(10d));
        });
    }

    @Test
    public void getAndUpdateKarmaTest(){
        testPic.onKarmaAvailable((k1) -> {
                    testPic.updateKarma(-1);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    testPic.onKarmaAvailable((k2) -> {
                        assertThat(k2, is(k1-1));
                    });
                }
        );
    }

    @Test
    public void approximateLocationIsInRange(){
        double radius = 200; // meters
        double epsilon = 10; // meters
        ExistingPicture pic = testPic;
        pic.onApproximateLocationAvailable((approximateLocation) -> {
            assertTrue(approximateLocation.distanceTo(loc) < radius + epsilon);
        });
    }
}
