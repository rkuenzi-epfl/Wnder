package com.github.wnder;


import android.net.Uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;


@RunWith(RobolectricTestRunner.class)
public class UserTest {


    @Test
    public void guestUserReturnGuestName(){
        User u = new GuestUser();
        assertEquals(u.getName(), "Guest");
    }
    @Test
    public void guestUserReturnDefaultProfilePicturePath(){
        User u = new GuestUser();
        assertEquals(u.getProfilePicture(), Uri.parse("android.resource://raw/ladiag.jpg"));
    }

    @Test
    public void signedInUserReturnCorrectName(){
        // Reusing default Profile Picture for testing
        User u = new SignedInUser("TestUser", Uri.parse("android.resource://raw/ladiag.jpg"));
        assertEquals(u.getName(), "TestUser");
    }
    @Test
    public void signedInUserReturnCorrectProfilePictureUri(){
        // Reusing default Profile Picture for testing
        User u = new SignedInUser("TestUser", Uri.parse("android.resource://raw/ladiag.jpg"));
        assertEquals(u.getProfilePicture(), Uri.parse("android.resource://raw/ladiag.jpg"));
    }
}
