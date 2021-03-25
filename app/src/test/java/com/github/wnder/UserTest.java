package com.github.wnder;


import android.net.Uri;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


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
        assertEquals(u.getProfilePicture(), Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
    }

    @Test
    public void signedInUserReturnCorrectName(){
        // Reusing default Profile Picture for testing
        User u = new SignedInUser("TestUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
        assertEquals(u.getName(), "TestUser");
    }
    @Test
    public void signedInUserReturnCorrectProfilePictureUri(){
        // Reusing default Profile Picture for testing
        User u = new SignedInUser("TestUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
        assertEquals(u.getProfilePicture(), Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
    }

    @Test
    public void globalUserReturnGuestUserByDefault(){
        User u = GlobalUser.getUser();
        assertThat(u.getName(), is("Guest"));
        assertThat(u.getProfilePicture(), is(Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
        GlobalUser.resetUser();
    }

    @Test
    public void globalUserReturnCorrectUserAfterSet(){
        GlobalUser.setUser(new SignedInUser("SignedInUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
        User u = GlobalUser.getUser();
        assertThat(u.getName(), is("SignedInUser"));
        assertThat(u.getProfilePicture(), is(Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
        GlobalUser.resetUser();
    }

    @Test
    public void globalUserReturnCorrectUserAfterReset(){
        GlobalUser.setUser(new SignedInUser("SignedInUser", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
        GlobalUser.resetUser();
        User u = GlobalUser.getUser();
        assertThat(u.getName(), is("Guest"));
        assertThat(u.getProfilePicture(), is(Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));
        GlobalUser.resetUser();
    }
}
