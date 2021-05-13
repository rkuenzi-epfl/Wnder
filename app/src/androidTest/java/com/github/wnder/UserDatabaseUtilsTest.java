package com.github.wnder;

import android.net.Uri;

import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;
import com.github.wnder.user.User;
import com.github.wnder.user.UserDatabase;
import com.github.wnder.user.UserDatabaseUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import dagger.hilt.android.testing.BindValue;
import dagger.hilt.android.testing.HiltAndroidTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@HiltAndroidTest
public class UserDatabaseUtilsTest {
    private UserDatabaseUtils userDbUtils;

    @BindValue
    public static UserDatabase userDb = Mockito.mock(UserDatabase.class);

    @Before
    public void setup(){
        CompletableFuture<List<String>> guessedPicturesFuture = new CompletableFuture<>();
        List<String> guessedPictures = new ArrayList<>();
        guessedPictures.add("pic1");
        guessedPictures.add("pic2");
        guessedPicturesFuture.complete(guessedPictures);

        CompletableFuture<Set<Double>> allScoresFuture = new CompletableFuture<>();
        HashSet<Double> allScores = new HashSet<>();
        allScores.add(250.);
        allScores.add(200.);
        allScoresFuture.complete(allScores);

        Mockito.when(userDb.getAllScores()).thenReturn(allScoresFuture);
        Mockito.when(userDb.getAllGuessedPictures()).thenReturn(guessedPicturesFuture);
        this.userDbUtils = new UserDatabaseUtils(userDb.getAllGuessedPictures(), userDb.getAllScores());
        User user = new SignedInUser("testUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
        GlobalUser.setUser(user);
    }

    @After
    public void teardown(){
        GlobalUser.resetUser();
    }

    @Test
    public void getTotalScoreWorks(){
        userDbUtils.getTotalScore().thenAccept(total -> assertThat(total, is(250 + 200)));
    }

    @Test
    public void getNbrOfGuessedPicsWorks(){
        userDbUtils.getNbrOfGuessedPictures().thenAccept(nbr -> assertThat(nbr, is(2)));
    }

    @Test
    public void getAverageScoreWorks(){
        userDbUtils.getAverageScore().thenAccept(average -> assertThat(average, is((double)(250 + 200)/2)));
    }
}
