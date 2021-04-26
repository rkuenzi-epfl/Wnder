package com.github.wnder.localDatabase;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {LocalPicture.class}, version = 1)
public abstract class LocalDatabase extends RoomDatabase {
    public abstract LocalPictureDao localPictureDao();
}
