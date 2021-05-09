package com.github.wnder;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Class displaying a bottom navigation bar and letting us go from one fragment to the other depending on this bar
 */
public class NavigationActivity extends AppCompatActivity {

    //Navigation view
    private BottomNavigationView bottomNavigationView;

    /**
     * Execs on activity creation
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation);

        //Setup bottom navigation bar
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> updateFragment(item.getItemId()));
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
     * @param id id of the pressed icon
     * @return true when done
     */
    private Boolean updateFragment(Integer id){
        switch(id){
            case R.id.profile_page:
                //update fragment
            case R.id.guess_page:
                //update fragment
            case R.id.history_page:
                //update fragment
        }
        return true;
    }
}
