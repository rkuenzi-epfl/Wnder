package com.github.wnder.localDatabase;

import android.location.Location;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class LocalPicture {
    @PrimaryKey
    private String uniqueId;

    private Location location;

    public LocalPicture(String uniqueId, Location location){
        this.uniqueId = uniqueId;
        this.location = location;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public Location getLocation() {
        return location;
    }
}
