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



public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private int[] distances = {5, 10, 20, 50, 100, 500, 1000};
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        TextView textView = toolbar.findViewById(R.id.username);
        User user = GlobalUser.getUser();
        textView.setText(user.getName());

        ImageView imageView = toolbar.findViewById(R.id.profile_picture);
        imageView.setImageURI(user.getProfilePicture());

        findViewById(R.id.getPictureButton).setOnClickListener(this);
        findViewById(R.id.uploadPictureButton).setOnClickListener(this);
        findViewById(R.id.menuToHistoryButton).setOnClickListener(this);

        //SeekBar for radius
        SeekBar radiusSeekBar = (SeekBar) findViewById(R.id.radiusSeekBar);
        TextView radiusTextView = findViewById(R.id.radiusTextView);

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

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if(requestCode != 100){
            return;
        }
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

    @Override
    public void onClick(View v) {
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


    private void openUploadActivity() {
        Intent intent = new Intent(this, TakePictureActivity.class);
        startActivity(intent);
    }

    private void openPreviewActivity() {
        Intent intent = new Intent(this, GuessPreviewActivity.class);
        startActivity(intent);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        User u = GlobalUser.getUser();
        u.setRadius(distances[i]);
        TextView radiusTextView = findViewById(R.id.radiusTextView);
        radiusTextView.setText("Radius: "+distances[i]+"km");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //do nothing
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //do nothing
    }

    private void openHistoryActivity() {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }
}
