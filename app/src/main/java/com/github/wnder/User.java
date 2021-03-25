package com.github.wnder;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;

public abstract class User {

    LatLng position = null;

    public abstract String getName();

    public abstract Uri getProfilePicture();

    public void getPositionFromGPS(LocationManager manager, Context context){

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location l) {
                position = new LatLng(l.getLatitude(), l.getLongitude());
            }
        };

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //TODO: Maybe ask for permission? Or just consider it to be an impossible case
            throw new IllegalStateException();
        }

        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }
}
