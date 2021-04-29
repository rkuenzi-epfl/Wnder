package com.github.wnder;

import android.Manifest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.User;


/**
 * Main activity
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    //Different distances for the radius
    private int[] distances = {5, 10, 20, 50, 100, 500, 1000};
    //Toolbar
    private Toolbar toolbar;

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
        findViewById(R.id.getPictureButton).setOnClickListener(this);
        findViewById(R.id.uploadPictureButton).setOnClickListener(this);
        findViewById(R.id.menuToHistoryButton).setOnClickListener(this);

        //SeekBar for radius
        SeekBar radiusSeekBar = (SeekBar) findViewById(R.id.radiusSeekBar);
        TextView radiusTextView = findViewById(R.id.radiusTextView);

        //Set radius seekbar depending on user selected radius
        int userRad = GlobalUser.getUser().getRadius();
        for(int i = 0; i < distances.length; i++){
            if(userRad == distances[i]){
                radiusSeekBar.setProgress(i);
                radiusTextView.setText("Radius: "+distances[i]+"km");
                break;
            }
        }
        
        radiusSeekBar.setOnSeekBarChangeListener(this);

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
                LocMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, location -> {
                    //Nothing to do in case of location change, the request is being done when necessary with getLastKnownLocation
                });
            }
        }
    }

    /**
     * Manages what happens when use clicks on buttons
     * @param v button clicked
     */
    @Override
    public void onClick(View v) {
        //When a button is clicked, set the user's location
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        GlobalUser.getUser().setLocation(GlobalUser.getUser().getPositionFromGPS(locationManager, getApplicationContext()));
        switch (v.getId()) {
            case R.id.uploadPictureButton:
                openUploadActivity();
                break;
            case R.id.getPictureButton:
                openPreviewActivity();
                break;
            case R.id.menuToHistoryButton:
                openHistoryActivity();
                break;
            default:
                break;
            // Other buttons can be setup in this switch
        }
    }

    /**
     * To call when upload button is clicked
     */
    private void openUploadActivity() {
        Intent intent = new Intent(this, TakePictureActivity.class);
        startActivity(intent);
    }

    /**
     * To call when guess button is clicked
     */
    private void openPreviewActivity() {
        Intent intent = new Intent(this, GuessPreviewActivity.class);
        startActivity(intent);
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
        radiusTextView.setText("Radius: "+distances[i]+"km");
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
     * To be called when history is clicked
     */
    private void openHistoryActivity() {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }
}
