package com.github.wnder;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Class displaying a bottom navigation bar and letting us go from one fragment to the other depending on this bar
 */
@AndroidEntryPoint
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

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, ProfileFragment.class, null)
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();
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
        if(id.equals(PROFILE_PAGE)){
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, ProfileFragment.class, null)
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();
        }
        return true;
    }
}
