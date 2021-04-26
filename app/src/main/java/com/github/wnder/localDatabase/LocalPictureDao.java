package com.github.wnder.localDatabase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LocalPictureDao {
    @Query("SELECT * FROM localpicture WHERE uniqueId = (:uniqueId)")
    LocalPicture get(String uniqueId);

    @Query("SELECT * FROM localpicture")
    List<LocalPicture> getAll();

    @Insert
    void insert(LocalPicture localPicture);

    @Insert
    void insertAll(LocalPicture... localPicture);

    @Delete
    void delete(LocalPicture localPicture);
}
