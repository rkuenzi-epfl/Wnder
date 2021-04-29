package com.github.wnder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.wnder.picture.FirebasePicturesDatabase;
import com.github.wnder.picture.LocalPictureDatabase;
import com.github.wnder.picture.PicturesModule;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(JUnit4.class)
public class LocalPictureDatabaseTest {

    private static FirebasePicturesDatabase db = new FirebasePicturesDatabase();
    private static Context context = ApplicationProvider.getApplicationContext();
    private static LocalPictureDatabase LPD = new LocalPictureDatabase(context);

    private static String uniqueId;
    private static Bitmap bmp;
    private static Location realLoc;
    private static Location guessLoc;
    private static Map<String, Double> scoreboard;

    @BeforeClass
    public static void setup() throws ExecutionException, InterruptedException {
        //SETUP TEST IMAGE
        uniqueId = "testPicDontRm";
        bmp = db.getBitmap("testPicDontRm").get();
        realLoc = new Location("");
        guessLoc = new Location("");
        scoreboard = new HashMap<>();
        scoreboard.put("testUser", 200.);

    }

    @AfterClass
    public static void delete(){

        File directory = context.getDir("images", Context.MODE_PRIVATE);
        Log.d("HIHI", String.valueOf(directory.listFiles().length));
        new File(directory, uniqueId).delete();
        Log.d("HIHI", String.valueOf(directory.listFiles().length));
    }

    @Test
    public void storeAndOpenPictureWorks() throws IOException {
        LPD.storePictureFile(bmp, uniqueId);
        Bitmap readFile = LPD.openPictureFile(uniqueId);
        int w1 = bmp.getWidth();
        int w2 = readFile.getWidth();
        assertThat(w1, is(w2));
    }



}
