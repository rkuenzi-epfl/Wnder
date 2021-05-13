package com.github.wnder;

import android.content.Context;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;
import com.github.wnder.user.User;
import com.github.wnder.user.UserDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UserDatabaseTest {
    private UserDatabase userDb;

    private static Context context = ApplicationProvider.getApplicationContext();
    @Before
    public void setup(){
        this.userDb = new UserDatabase(context);
        User user = new SignedInUser("testUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
        GlobalUser.setUser(user);
    }

    @After
    public void teardown(){
        GlobalUser.resetUser();
    }

    @Test
    public void getTotalScoreWorks(){
        userDb.getTotalScore().thenAccept(total -> assertThat(total, is(156)));
    }

    @Test
    public void getNbrOfGuessedPicsWorks(){
        userDb.getNbrOfGuessedPictures().thenAccept(nbr -> assertThat(nbr, is(2)));
    }

    @Test
    public void getAverageScoreWorks(){
        userDb.getAverageScore().thenAccept(average -> assertThat(average, is(156)));
    }
    
}
