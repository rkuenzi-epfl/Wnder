package com.github.wnder;


import android.net.Uri;

import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.GuestUser;
import com.github.wnder.user.SignedInUser;
import com.github.wnder.user.User;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(RobolectricTestRunner.class)
public class UserTest {

    @After
    public void resetUser(){
        GlobalUser.resetUser();
    }

    @Test
    public void guestUserReturnGuestName(){
        User u = new GuestUser();
        assertEquals(u.getName(), "Guest");
    }
    @Test
    public void guestUserReturnDefaultProfilePicturePath(){
        User u = new GuestUser();
        assertEquals(u.getProfilePicture(), Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
    }

    @Test
    public void signedInUserReturnCorrectName(){
        // Reusing default Profile Picture for testing
        User u = new SignedInUser("TestUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag),"TestUser");
        assertEquals(u.getName(), "TestUser");
    }
    @Test
    public void signedInUserReturnCorrectProfilePictureUri(){
        // Reusing default Profile Picture for testing
        User u = new SignedInUser("TestUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag),"TestUser");
        assertEquals(u.getProfilePicture(), Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
    }

    @Test
    public void globalUserReturnGuestUserByDefault(){
        User u = GlobalUser.getUser();
        assertThat(u.getName(), is("Guest"));
        assertThat(u.getProfilePicture(), is(Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
    }

    @Test
    public void globalUserReturnCorrectUserAfterSet(){
        GlobalUser.setUser(new SignedInUser("SignedInUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag),"TestUser"));
        User u = GlobalUser.getUser();
        assertThat(u.getName(), is("SignedInUser"));
        assertThat(u.getProfilePicture(), is(Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
    }

    @Test
    public void globalUserReturnCorrectUserAfterReset(){
        GlobalUser.setUser(new SignedInUser("SignedInUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag),"TestUser"));
        GlobalUser.resetUser();
        User u = GlobalUser.getUser();
        assertThat(u.getName(), is("Guest"));
        assertThat(u.getProfilePicture(), is(Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
    }

    @Test
    public void userSkippedPictureSetIsUpdated(){
        GlobalUser.setUser(new SignedInUser("SignedInUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag),"TestUser"));
        User u =  GlobalUser.getUser();
        u.skipPicture("thePicture");
        assertTrue(u.getSkippedPictures().contains("thePicture"));
    }
}
