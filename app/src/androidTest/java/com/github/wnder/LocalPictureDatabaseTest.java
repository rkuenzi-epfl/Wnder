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

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

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
    public void setup() throws ExecutionException, InterruptedException {
        //SETUP TEST IMAGE
        uniqueId = "testImage";
        bmp = db.getBitmap("testPicDontRm").get();
        realLoc = new Location("");
        guessLoc = new Location("");
        scoreboard = new HashMap<>();
        scoreboard.put("testUser", 200.);

    }

    @Test
    public void random(){
        Log.d("HIHI", context.getFilesDir().toString());
        Log.d("HIHI", String.valueOf(context.getFilesDir().isDirectory()));
        File file = new File(context.getFilesDir(), "images");
        Log.d("HIHI", String.valueOf(file.isDirectory()));
        Log.d("HIHI", file.getPath());

    }

    @Test
    public static void storeAndOpenPictureWorks() throws IOException {
        LPD.storePictureFile(bmp, uniqueId);
        Bitmap readFile = LPD.openPictureFile(uniqueId);
        assertThat(bmp.getWidth(), is(readFile.getWidth()));
    }



}
