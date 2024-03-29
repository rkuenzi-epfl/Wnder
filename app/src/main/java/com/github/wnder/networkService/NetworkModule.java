package com.github.wnder.networkService;

import android.content.Context;
import android.net.ConnectivityManager;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class NetworkModule {
    @Binds
    public abstract NetworkService bindNetworkService(NetworkInformation networkInformationImpl);

    /**
     * Gives a connectivity manager
     * @param ctx Context of the application
     * @return a connectivity manager
     */
    @Provides
    public static ConnectivityManager provideConnectivityManager(@ApplicationContext Context ctx){
        return (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
    }
}