package com.github.wnder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.github.wnder.networkService.NetworkInformation;
import com.github.wnder.networkService.NetworkService;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;
import com.github.wnder.user.User;
import com.github.wnder.user.UserDatabase;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import javax.annotation.Signed;
import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * This class is the fragment for the profile page of the app
 */
@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    //Setup buttons and google signin and texts
    private View signInButton;
    private View logoutButton;
    private TextView noConnection;

    //setup stat cards
    private MaterialCardView nbrOfGuessesCard;
    private MaterialCardView averageScoreCard;
    private MaterialCardView totalScoreCard;

    //setup stat cards texts
    private TextView nbrOfGuessesText;
    private TextView averageScoreText;
    private TextView totalScoreText;

    //setup username and profile pic
    private TextView username;
    private ImageView profilePic;

    //setup sign in client and userdb
    private GoogleSignInClient client;
    private final int RC_SIGN_IN = 10; // Arbitrary number
    private UserDatabase userDb;

    private View view;

    @Inject
    public NetworkService networkInfo;

    /**
     * Constructor for the profile fragment
     */
    public ProfileFragment(){
        super(R.layout.fragment_profile);
    }

    /**
     * Executes on view created
     * @param view current view
     * @param savedInstanceState saved instance state
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){

        //ALL VIEWS ARE INVISIBLE WHEN CREATED

        this.view = view;

        //setup username and profile pic
        username = view.findViewById(R.id.username);
        profilePic = view.findViewById(R.id.profile_picture);

        //setup text for when no connection or when logged out
        noConnection = view.findViewById(R.id.no_connection_text);
        noConnection.setVisibility(View.INVISIBLE);

        //Setup the stat cards
        nbrOfGuessesCard = view.findViewById(R.id.nbrOfGuessesCard);
        nbrOfGuessesCard.setVisibility(View.INVISIBLE);

        averageScoreCard = view.findViewById(R.id.averageScoreCard);
        averageScoreCard.setVisibility(View.INVISIBLE);

        totalScoreCard = view.findViewById(R.id.totalScoreCard);
        totalScoreCard.setVisibility(View.INVISIBLE);

        //Setup the stat cards texts
        nbrOfGuessesText = view.findViewById(R.id.nbrOfGuesses);
        averageScoreText = view.findViewById(R.id.averageScore);
        totalScoreText = view.findViewById(R.id.totalScore);

        //Sign in and logout buttons
        signInButton = view.findViewById(R.id.signInButton);
        signInButton.setVisibility(View.INVISIBLE);  // Hide the button
        signInButton.setOnClickListener(id -> signIn());

        logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setVisibility(View.INVISIBLE);
        logoutButton.setOnClickListener(id -> logout());

        //Setup google sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        client = GoogleSignIn.getClient(this.getActivity(), gso);
    }

    /**
     * Executes on fragment start
     */
    @Override
    public void onStart(){
        super.onStart();

        //Checks if user has already signed in
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this.getContext());
        //If yes, skip this activity, else, give him the choice
        if(account != null) {
            GlobalUser.setUser(new SignedInUser(account.getDisplayName(), account.getPhotoUrl()));
        }
        //update page status
        updateLoginStatus();
    }

    /**
     * Logs out the user
     */
    private void logout(){
        //log out
        client.signOut();
        //global user becomes guest again
        GlobalUser.resetUser();
        //update page status
        updateLoginStatus();
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

            updateLoginStatus();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            //Log.w("LoginTag", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    /**
     * To call when the logged in/logged out or connection/no connection states change
     */
    private void updateLoginStatus(){
        //Choose right button (sign in or logout)
        if(areWeLoggedIn()){
            logoutButton.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.INVISIBLE);
        }
        else{
            logoutButton.setVisibility(View.INVISIBLE);
            signInButton.setVisibility(View.VISIBLE);
        }

        //setup right profile picture
        User user = GlobalUser.getUser();
        Picasso.get().load(user.getProfilePicture()).into(profilePic);

        //setup right username
        username.setText(user.getName());
        username.setVisibility(View.VISIBLE);

        //update user database
        userDb = new UserDatabase(this.getContext());

        //update network status
        updateNetworkStatus();
    }

    /**
     * ONLY TO BE CALLED AT THE END OF UPDATELOGINSTATUS()
     * update ui depending on logged in/out and connection/no connection state
     */
    private void updateNetworkStatus(){
        //if connection and logged in
        if(networkInfo.isNetworkAvailable() && areWeLoggedIn()){

            //fill in cards with user info
            userDb.getNbrOfGuessedPictures().thenAccept(nbr -> nbrOfGuessesText.setText(String.format(Locale.getDefault(), "%d", nbr)));
            userDb.getAverageScore().thenAccept(average -> averageScoreText.setText(String.format(Locale.getDefault(), "%,.2f", average)));
            userDb.getTotalScore().thenAccept(total -> totalScoreText.setText(String.format(Locale.getDefault(), "%,.2f", total)));

            //display cards
            nbrOfGuessesCard.setVisibility(View.VISIBLE);
            totalScoreCard.setVisibility(View.VISIBLE);
            averageScoreCard.setVisibility(View.VISIBLE);
            noConnection.setVisibility(View.INVISIBLE);
        }
        else{
            //make cards invisible
            nbrOfGuessesCard.setVisibility(View.INVISIBLE);
            totalScoreCard.setVisibility(View.INVISIBLE);
            averageScoreCard.setVisibility(View.INVISIBLE);
            noConnection.setVisibility(View.VISIBLE);

            //empty cards
            nbrOfGuessesText.setText("");
            averageScoreText.setText("");
            totalScoreText.setText("");
        }
    }

    /**
     * Tells us if we are logged in or not
     * @return true if logged in, false otherwise
     */
    private boolean areWeLoggedIn(){
        // just check that the global user is a signed in one
        return (GlobalUser.getUser() instanceof SignedInUser);
    }
}
