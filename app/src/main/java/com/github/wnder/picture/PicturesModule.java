package com.github.wnder.picture;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class PicturesModule {

    @Singleton
    @Binds
    public abstract PicturesDatabase bindPicturesDatabase(InternalCachePictureDatabase firebaseImpl);

}
