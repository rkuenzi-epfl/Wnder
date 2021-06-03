package com.github.wnder;

import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import com.github.wnder.user.FirebaseUserDatabase;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;
import com.github.wnder.user.User;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertFalse;

@RunWith(JUnit4.class)
public class FirebaseUserDatabaseTest {

    private static FirebaseUserDatabase db = new FirebaseUserDatabase(ApplicationProvider.getApplicationContext());
    private static final int RADIUS_FOR_REFERECE = 100000;

    @Test
    public void gettingNewPicturesReturnAPictureNotGuessed(){
        GlobalUser.setUser(new SignedInUser("testUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag),"testUser"));
        User user = GlobalUser.getUser();
        user.setRadius(RADIUS_FOR_REFERECE);
        List<String> guessedPics = new ArrayList<>();
        String receivedPic = "";
        try {
            guessedPics = db.getPictureList(user, "guessed").get();
            receivedPic = db.getNewPictureForUser(user).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertFalse(guessedPics.isEmpty());
        assertFalse(guessedPics.contains(receivedPic));
        GlobalUser.resetUser();
    }

}
