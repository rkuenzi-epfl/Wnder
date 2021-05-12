package com.github.wnder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.github.wnder.networkService.NetworkService;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.GuestUser;
import com.github.wnder.user.SignedInUser;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Class displaying a bottom navigation bar and letting us go from one fragment to the other depending on this bar
 */
@AndroidEntryPoint
public class NavigationActivity extends AppCompatActivity {

    @Inject
    public NetworkService networkInfo;

    private BottomNavigationView bottomNavigationView;

    private static final int REQUEST_POSITION_CODE = 100;

    /**
     * Execs on activity creation
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation);

        // If you want to test history without sign in:
        // GlobalUser.setUser(new SignedInUser("Bonbon", Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag)));

        //Setup map with corresponding strings
        Map<Integer, Class> iconMap = new HashMap<>();
        iconMap.put(R.id.profile_page, ProfileFragment.class);
        iconMap.put(R.id.take_picture_page, TakePictureFragment.class);
        iconMap.put(R.id.guess_page, GuessFragment.class);
        iconMap.put(R.id.history_page, HistoryFragment.class);

        //call updateFragment depending on clicked icon
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> updateFragment(iconMap.get(item.getItemId())));

        String[] ss = {Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(this, ss, 100); //Very important to have permission for future call
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
     * @param fragmentClass class associated to pressed button
     * @return Boolean true
     */
    private Boolean updateFragment(Class fragmentClass){


        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, fragmentClass, null)
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();

        return true;
    }

    /**
     * To run when we get user's answer for location permission
     * @param requestCode request code
     * @param permissions permissions
     * @param grantResults grant results
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //if it's not the good request, return
        if(requestCode == REQUEST_POSITION_CODE){
            //permission to get the location
            for(int i = 0; i < permissions.length; ++i){
                if(permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) && !(grantResults[i] == PackageManager.PERMISSION_GRANTED)){
                    // TODO: What happens if the user did not accept?
                    throw new UnsupportedOperationException();
                }

                LocationManager LocMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                LocMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, location -> {
                    //Nothing to do in case of location change, the request is being done when necessary with getLastKnownLocation
                });
            }
        }
    }
}
