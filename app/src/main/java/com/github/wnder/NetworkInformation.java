package com.github.wnder;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static androidx.core.content.ContextCompat.getSystemService;

public final class NetworkInformation {

    private NetworkInformation(){
        //Non-instanciable class
    }

    /**
     * Checks if the cellular or wifi connection is available
     * @param ctx Context of the application
     * @return true if we are connected to internet
     */
    public static boolean isNetworkAvailable(Context ctx) {
        ConnectivityManager manager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }
}
