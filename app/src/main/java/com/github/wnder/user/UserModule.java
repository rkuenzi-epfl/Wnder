package com.github.wnder.picture;

import com.github.wnder.user.FirebaseUserDatabase;
import com.github.wnder.user.UserDatabase;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class UserModule {

    @Binds
    public abstract UserDatabase bindUserDatabase(FirebaseUserDatabase firebaseImpl);
}