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

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    //Setup buttons and google signin and texts
    private boolean areWeLoggedIn;
    private View signInButton;
    private View logoutButton;
    private TextView noConnection;

    private MaterialCardView nbrOfGuessesCard;
    private MaterialCardView averageScoreCard;
    private MaterialCardView totalScoreCard;

    private TextView nbrOfGuessesText;
    private TextView averageScoreText;
    private TextView totalScoreText;

    private TextView username;

    private GoogleSignInClient client;
    private final int RC_SIGN_IN = 10; // Arbitrary number
    private UserDatabase userDb;

    private View view;

    @Inject
    public NetworkService networkInfo;

    public ProfileFragment(){
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){

        this.view = view;

        username = view.findViewById(R.id.username);

        noConnection = view.findViewById(R.id.no_connection_text);
        noConnection.setVisibility(View.INVISIBLE);

        nbrOfGuessesCard = view.findViewById(R.id.nbrOfGuessesCard);
        nbrOfGuessesCard.setVisibility(View.INVISIBLE);

        averageScoreCard = view.findViewById(R.id.averageScoreCard);
        averageScoreCard.setVisibility(View.INVISIBLE);

        totalScoreCard = view.findViewById(R.id.totalScoreCard);
        totalScoreCard.setVisibility(View.INVISIBLE);

        nbrOfGuessesText = view.findViewById(R.id.nbrOfGuesses);
        averageScoreText = view.findViewById(R.id.averageScore);
        totalScoreText = view.findViewById(R.id.totalScore);

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

    @Override
    public void onStart(){
        super.onStart();

        //Checks if user has already signed in
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this.getContext());
        //If yes, skip this activity, else, give him the choice
        if(account != null){
            GlobalUser.setUser(new SignedInUser(account.getDisplayName(), account.getPhotoUrl()));
            areWeLoggedIn = true;
            updateLoginStatus();

        } else {
            areWeLoggedIn = false;
            updateLoginStatus();
        }
    }

    private void logout(){
        client.signOut();
        GlobalUser.resetUser();
        areWeLoggedIn = false;
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

            areWeLoggedIn = true;
            updateLoginStatus();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            //Log.w("LoginTag", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void updateLoginStatus(){
        if(areWeLoggedIn){
            logoutButton.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.INVISIBLE);
        }
        else{
            logoutButton.setVisibility(View.INVISIBLE);
            signInButton.setVisibility(View.VISIBLE);
        }

        User user = GlobalUser.getUser();
        ImageView profilePic = view.findViewById(R.id.profile_picture);

        Picasso.get().load(user.getProfilePicture()).into(profilePic);

        username.setText(user.getName());
        username.setVisibility(View.VISIBLE);

        userDb = new UserDatabase(this.getContext());

        updateNetworkStatus();
    }

    private void updateNetworkStatus(){
        if(networkInfo.isNetworkAvailable() && areWeLoggedIn){

            userDb.getNbrOfGuessedPictures().thenAccept(nbr -> nbrOfGuessesText.setText(String.format("%d", nbr)));
            userDb.getAverageScore().thenAccept(average -> averageScoreText.setText(String.format("%,.2f", average)));
            userDb.getTotalScore().thenAccept(total -> totalScoreText.setText(String.format("%,.2f", total)));

            nbrOfGuessesCard.setVisibility(View.VISIBLE);
            totalScoreCard.setVisibility(View.VISIBLE);
            averageScoreCard.setVisibility(View.VISIBLE);
            noConnection.setVisibility(View.INVISIBLE);
        }
        else{
            nbrOfGuessesCard.setVisibility(View.INVISIBLE);
            totalScoreCard.setVisibility(View.INVISIBLE);
            averageScoreCard.setVisibility(View.INVISIBLE);
            noConnection.setVisibility(View.VISIBLE);

            nbrOfGuessesText.setText("");
            averageScoreText.setText("");
            totalScoreText.setText("");
        }
    }
}
