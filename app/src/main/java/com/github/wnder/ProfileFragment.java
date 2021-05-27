package com.github.wnder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.wnder.networkService.NetworkService;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;
import com.github.wnder.user.User;
import com.github.wnder.user.UserDatabase;
import com.github.wnder.user.UserDatabaseUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.squareup.picasso.Picasso;

import java.util.Locale;

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
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private FirebaseAuth firebaseAuth;


    @Inject
    public UserDatabase userDb;
    private UserDatabaseUtils userDbUtils;

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
                .requestIdToken(getString(R.string.google_secret)).requestEmail().build();
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Get account from google and sign in to firebase
                    GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                            .addOnSuccessListener(account -> {
                                handleSignInResult(GoogleAuthProvider.getCredential(account.getIdToken(), null));
                            }).addOnFailureListener(failed -> signInFailed());

                });
        client = GoogleSignIn.getClient(this.getActivity(), gso);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Executes on fragment start
     */
    @Override
    public void onStart(){
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null){
            setGlobalUser(currentUser);
        }

        //update page status
        updateLoginStatus();
    }

    /**
     * Logs out the user
     */
    private void logout(){
        //global user becomes guest again
        GlobalUser.resetUser();
        //log out
        client.signOut();
        FirebaseAuth.getInstance().signOut();
        //update page status
        updateLoginStatus();
    }

    /**
     * Helps a user sign in
     */
    private void signIn() {
        //Checks if user has already signed in
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this.getContext());

        //If yes, retrieve this account and set the user
        if(account != null && account.getIdToken() != null) {
            handleSignInResult(GoogleAuthProvider.getCredential(account.getIdToken(), null));
        } else {
            googleSignInLauncher.launch(client.getSignInIntent());
        }

    }

    /**
     * Set the global user on a successful sign in
     * and update UI
     */
    private void setGlobalUser(FirebaseUser firebaseUser){
        Log.d("FACC", "GOT FACC");
        GlobalUser.setUser(new SignedInUser(firebaseUser.getDisplayName(), firebaseUser.getPhotoUrl(), firebaseUser.getUid()));
        updateLoginStatus();
    }

    /**
     * Get credential from google and sign in to firebase
     * @param credential credential from the auth provider
     */
    private void handleSignInResult(AuthCredential credential) {
        Log.d("GACC", "GOT s");

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("succ fo success", "signInWithCredential:success");
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        setGlobalUser(user);
                    } else {signInFailed();}
                });
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

        userDbUtils = new UserDatabaseUtils(userDb.getPictureList(GlobalUser.getUser(), "guessedPics"), userDb.getAllScores(GlobalUser.getUser()));

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
            userDbUtils.getNbrOfGuessedPictures().thenAccept(nbr -> nbrOfGuessesText.setText(String.format(Locale.getDefault(), "%d", nbr)));
            userDbUtils.getAverageScore().thenAccept(average -> averageScoreText.setText(String.format(Locale.getDefault(), "%,.2f", average)));
            userDbUtils.getTotalScore().thenAccept(total -> totalScoreText.setText(String.format(Locale.getDefault(), "%,.2f", total)));

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
    private boolean areWeLoggedIn() {
        // just check that the global user is a signed in one
        return (GlobalUser.getUser() instanceof SignedInUser);
    }

    /**
     * Display alert when signIn fail
     */
    private void signInFailed(){
        AlertBuilder.okAlert(getString(R.string.signin_fail), getString(R.string.signin_fail_retry), this.getContext());
    }
}
