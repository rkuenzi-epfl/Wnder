package com.github.wnder.tour;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class TourModule {
    @Binds
    public abstract TourDatabase bindTourDatabase(FirebaseTourDatabase firebaseImpl);
}
