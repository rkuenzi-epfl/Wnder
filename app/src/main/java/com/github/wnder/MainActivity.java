package com.github.wnder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.github.wnder.networkService.NetworkInformation;
import com.github.wnder.networkService.NetworkService;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.User;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;


/**
 * Main activity
 */
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    //Different distances for the radius
    private int[] distances = {5, 10, 20, 50, 100, 500, 1000};
    //Toolbar
    private Toolbar toolbar;
    @Inject
    public NetworkService networkInfo;

    /**
     * Executes on activity creation
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set layout
        setContentView(R.layout.activity_main);

        //Set toolbar
        this.toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        //Set user profile pic and name on toolbar
        TextView textView = toolbar.findViewById(R.id.username);
        User user = GlobalUser.getUser();
        textView.setText(user.getName());
        ImageView imageView = toolbar.findViewById(R.id.profile_picture);
        imageView.setImageURI(user.getProfilePicture());

        //Set the buttons: guess, upload, history
        findViewById(R.id.uploadPictureButton).setOnClickListener(id -> openUploadActivity());

        findViewById(R.id.getPictureButton).setOnClickListener(id -> openPreviewActivity());
        findViewById(R.id.menuToHistoryButton).setOnClickListener(id -> openHistoryActivity());

        //SeekBar for radius
        SeekBar radiusSeekBar = (SeekBar) findViewById(R.id.radiusSeekBar);
        TextView radiusTextView = findViewById(R.id.radiusTextView);

        manageSeekBar(radiusSeekBar, radiusTextView);

        //rights for location services
        String[] ss = {Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(this, ss, 100); //Very important to have permission for future call
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

    /**
     * To call when upload button is clicked
     */
    private void openUploadActivity() {
        if(networkInfo.isNetworkAvailable()){
            Intent intent = new Intent(this, TakePictureActivity.class);
            startActivity(intent);
        }
        else{
            AlertDialog alert = AlertBuilder.noConnectionAlert(getString(R.string.no_connection), getString(R.string.no_internet_upload), this);
            alert.show();
        }
    }

    /**
     * To call when guess button is clicked
     */
    private void openPreviewActivity() {
        if(networkInfo.isNetworkAvailable()){
            Intent intent = new Intent(this, GuessPreviewActivity.class);
            startActivity(intent);
        }
        else{
            AlertDialog alert = AlertBuilder.noConnectionAlert(getString(R.string.no_connection), getString(R.string.no_internet_guess), this);
            alert.show();
        }
    }

    /**
     * When user interacts with radius seekbar
     * @param seekBar radius seekbar
     * @param i step of seekbar
     * @param b boolean
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        User u = GlobalUser.getUser();
        u.setRadius(distances[i]);
        TextView radiusTextView = findViewById(R.id.radiusTextView);
        radiusTextView.setText(getString(R.string.set_radius, distances[i]));
    }

    /**
     * To override for seekbar
     * @param seekBar
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //do nothing
    }

    /**
     * To override for seekbar
     * @param seekBar
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //do nothing
    }

    /**
     * Manage the SeekBar
     */
    private void manageSeekBar(SeekBar radiusSeekBar, TextView radiusTextView){
        //Set radius seekbar depending on user selected radius
        int userRad = GlobalUser.getUser().getRadius();
        for(int i = 0; i < distances.length; i++){
            if(userRad == distances[i]){
                radiusSeekBar.setProgress(i);
                radiusTextView.setText(getString(R.string.set_radius, distances[i]));
                break;
            }
        }
        radiusSeekBar.setOnSeekBarChangeListener(this);
    }

    /**
     * To be called when history is clicked
     */
    private void openHistoryActivity() {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }
}
