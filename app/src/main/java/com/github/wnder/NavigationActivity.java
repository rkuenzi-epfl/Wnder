package com.github.wnder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.github.wnder.networkService.NetworkService;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.GuestUser;
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
     * @param id string defining pressed button
     * @return Boolean true
     */
    private Boolean updateFragment(String id){
        if(id.equals(TAKE_PICTURE_PAGE)){
            // Alert Guest user and user no connected to the internet
            if(GlobalUser.getUser() instanceof GuestUser){
                AlertBuilder.okAlert(getString(R.string.guest_not_allowed), getString(R.string.guest_no_upload), this)
                        .show();
            } else if(!networkInfo.isNetworkAvailable()){
                AlertBuilder.okAlert(getString(R.string.no_connection), getString(R.string.no_internet_upload), this)
                        .show();
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, new TakePictureFragment(getActivityResultRegistry()))
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();
        }

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
        if(requestCode == 100){
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
