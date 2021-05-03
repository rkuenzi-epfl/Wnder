package com.github.wnder.networkService;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import javax.inject.Inject;

public class NetworkInformation implements NetworkService {

    private static NetworkInformation INSTANCE;
    private ConnectivityManager manager;

    @Inject
    public NetworkInformation(ConnectivityManager manager){
        this.manager = manager;
    }

    /**
     * Checks if the cellular or wifi connection is available
     * @return true if we are connected to internet
     */
    @Override
    public boolean isNetworkAvailable() {
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }
}
