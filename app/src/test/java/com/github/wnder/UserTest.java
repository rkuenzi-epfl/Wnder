package com.github.wnder;

import android.net.Uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserTest {

    private static Uri uri = mock(Uri.class);

    @Test
    public void guestUserReturnGuestName(){
        User u = new GuestUser();
        assertThat(u.getName(), is("Guest"));
    }
    @Test
    public void guestUserReturnDefaultProfilePicturePath(){
        User u = new GuestUser();
        assertThat(u.getProfilePicture(), is(uri.parse("android.resource://raw/ladiag.jpg")));
    }

    @Test
    public void signedInUserReturnCorrectName(){
        // Reusing default Profile Picture for testing
        User u = new SignedInUser("TestUser", uri.parse("android.resource://raw/ladiag.jpg"));
        assertThat(u.getName(), is("TestUser"));
    }
    @Test
    public void signedInUserReturnCorrectProfilePictureUri(){
        // Reusing default Profile Picture for testing
        User u = new SignedInUser("TestUser", uri.parse("android.resource://raw/ladiag.jpg"));
        assertThat(u.getProfilePicture(), is(uri.parse("android.resource://raw/ladiag.jpg")));
    }
}
