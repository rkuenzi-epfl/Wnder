package com.github.wnder;

import android.graphics.Bitmap;
import android.location.Location;

import com.github.wnder.picture.ExistingPicture;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ExistingPictureTesting {

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
    public void onUserPositionAvailableWorksForExistingPicture(){
        testPic.onUserPositionAvailable("testUser", (position)->{
            assertThat(position.getLatitude(), is(20d));
            assertThat(position.getLongitude(), is(20d));
        });
    }

    @Test
    public void onUserGuessAvailableWorksForExistingPicture(){
        testPic.onUserGuessAvailable("testUser", (guess)->{
            assertThat(guess.getLatitude(), is(10d));
            assertThat(guess.getLongitude(), is(10d));
        });
    }

    @Test
    public void onUserRadiusAvailableWorksForExistingPicture(){
        testPic.onUserRadiusAvailable("testUser", (radius)->{
            assertThat(radius, is(5));
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
    public void getAndUpdateKarmaTest() throws InterruptedException {
        testPic.onKarmaAvailable((k1) -> {
            testPic.updateKarma(-1);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            testPic.onKarmaAvailable((k2) -> {
                assertThat(k2, is(k1 - 1));
            });
        });
        Thread.sleep(2000);
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

    @Test
    public void skipPictureWorks() throws InterruptedException {
        testPic.onKarmaAvailable((k1) -> {
            testPic.skipPicture();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            testPic.onKarmaAvailable((k2) ->{
              assertThat(k2, is(k1-1));
            });
        });
        Thread.sleep(2000);
    }
}
