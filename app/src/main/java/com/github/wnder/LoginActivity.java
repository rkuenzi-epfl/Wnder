package com.github.wnder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

/**
 * Activity for the login
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    //Setup buttons and google signin and texts
    private View signInButton;
    private TextView textLogin;
    private GoogleSignInClient client;
    private final int RC_SIGN_IN = 10; // Arbitrary number
    private final String TAG = "LoginActivity.java";

    /**
     * Executes on activity creation
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set layout
        setContentView(R.layout.activity_login);

        //Set login button
        textLogin = findViewById(R.id.textLogin);
        signInButton = findViewById(R.id.signInButton);
        signInButton.setVisibility(View.INVISIBLE);  // Hide the button
        signInButton.setOnClickListener(this);

        //Set guest button
        findViewById(R.id.guestButton).setOnClickListener(this);

        //Setup google sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        client = GoogleSignIn.getClient(this, gso);
    }

    /**
     * Executes on activity start
     */
    @Override
    protected void onStart() {
        super.onStart();

        //Checks if user has already signed in
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        //If yes, skip this activity, else, give him the choice
        if(account != null){
            GlobalUser.setUser(new SignedInUser(account.getDisplayName(), account.getPhotoUrl()));
            goToMain();
        } else {
            signInButton.setVisibility(View.VISIBLE);
            textLogin.setText("Please sign in.");
        }

    }

    /**
     * Sends user to main
     */
    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Manages what happens when a user clicks on a button
     * @param v button clicked
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signInButton:
                signIn();
                break;
            case R.id.guestButton:
                goToMain();
                break;
            // Other buttons can be setup in this switch
        }
    }

    /**
     * Helps a user sign in
     */
    private void signIn() {
        Intent signInIntent = client.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * To execute when a user just signed in
     * @param requestCode request code
     * @param resultCode result code
     * @param data intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    /**
     * Tool method for onActivityResult: handles sign in result
     * @param completedTask completed task
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            GlobalUser.setUser(new SignedInUser(account.getDisplayName(), account.getPhotoUrl()));
            goToMain();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            goToMain();
        }
    }

}