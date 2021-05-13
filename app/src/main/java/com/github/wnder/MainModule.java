package com.github.wnder;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class MainModule {

    @Provides
    public static Context provideContext(@ApplicationContext Context context) {
        return context;
    }

}