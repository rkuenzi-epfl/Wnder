package com.github.wnder.picture;

import android.content.Context;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class PicturesModule {

    @Binds
    public abstract PicturesDatabase bindPicturesDatabase(InternalCachePictureDatabase firebaseImpl);

    @Provides
    public static Context provideContext(@ApplicationContext Context context){
        return context;
    }

}
