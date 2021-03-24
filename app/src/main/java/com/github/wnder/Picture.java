package com.github.wnder;

import android.location.Location;

import java.util.Map;

public interface Picture {
    /**
     * Returns the unique ID of the picture
     * @return The unique ID of the picture
     */
    public String getUniqueId();

    /**
     * Returns the longitude and latitude of the location of the picture
     * @return the longitude and latitude of the location of the picture
     */
    public Location getLocation();

    /**
     * Returns the global scoreboard linked with the picture
     * @return a map containing every user with its associated score
     */
    public Map<String, Object> getScoreboard();

    /**
     * Returns all the guesses linked with the picture
     * @return A map containing every user with its associated guess
     */
    public Map<String, Object> getGuesses();
}
