package com.github.wnder;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

/**
 * Class displaying a bottom navigation bar and letting us go from one fragment to the other depending on this bar
 */
public class NavigationActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    //Page strings
    private static final String PROFILE_PAGE = "profile";
    private static final String TAKE_PICTURE_PAGE = "take_picture";
    private static final String GUESS_PAGE = "guess";
    private static final String HISTORY_PAGE = "history";

    /**
     * Execs on activity creation
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation);

        //Setup map with corresponding strings
        Map<Integer, String> iconMap = new HashMap<>();
        iconMap.put(R.id.profile_page, PROFILE_PAGE);
        iconMap.put(R.id.take_picture_page, TAKE_PICTURE_PAGE);
        iconMap.put(R.id.guess_page, GUESS_PAGE);
        iconMap.put(R.id.history_page, HISTORY_PAGE);

        //call updateFragment depending on clicked icon
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> updateFragment(iconMap.get(item.getItemId())));
    }

    /**
     * Execs on activity start
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Update fragment depending on pressed navigation bar button
     * @param id string defining pressed button
     * @return Boolean true
     */
    private Boolean updateFragment(String id){
        //do nothing for now
        return true;
    }
}
