package com.github.wnder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    private LoginActivity testActivity;

    private Button mockButton = mock(Button.class);

    @Before
    private void setup(){

        testActivity = Mockito.spy(LoginActivity.class);
        doReturn(mockButton).when(testActivity).findViewById(anyInt());

    }

    @Test
    public void testLogin(){

        ArgumentCaptor<View.OnClickListener> captor = ArgumentCaptor.forClass(View.OnClickListener.class);
        Mockito.doNothing().when(mockButton).setOnClickListener(captor.capture());

        testActivity.onCreate(mock(Bundle.class));
        testActivity.onStart();

        Mockito.doNothing().when(testActivity).startActivity((Intent) any());
        Mockito.doNothing().when(testActivity).finish();  // We should also test this to be called in a new test.
        captor.getValue().onClick(mockButton);
        Mockito.verify(testActivity).startActivity((Intent) any());
    }
}
