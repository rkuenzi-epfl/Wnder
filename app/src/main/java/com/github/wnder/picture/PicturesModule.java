package com.github.wnder.picture;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class PicturesModule {

    @Binds
    public abstract PicturesDatabase bindPicturesDatabase(FirebasePicturesDatabase firebaseImpl);

}
