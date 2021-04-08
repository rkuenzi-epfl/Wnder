package com.github.wnder;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;

import java.util.IllegalFormatException;

public abstract class User {

    LatLng position = null;

    public abstract String getName();

    public abstract Uri getProfilePicture();

    public Location getPositionFromGPS(LocationManager manager, Context context){
        
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //TODO: Maybe ask for permission? Or just consider it to be an impossible case
            //throw new IllegalStateException();
        }

        return manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }
}
