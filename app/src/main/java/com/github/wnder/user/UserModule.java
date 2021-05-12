package com.github.wnder.picture;

import android.content.Context;

import com.github.wnder.user.FirebaseUserDatabase;
import com.github.wnder.user.UserDatabase;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class UserModule {

    @Binds
    public abstract UserDatabase bindUserDatabase(FirebaseUserDatabase firebaseImpl);
}