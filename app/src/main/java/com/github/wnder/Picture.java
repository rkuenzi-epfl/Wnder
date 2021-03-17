package com.github.wnder;

import android.net.Uri;

import java.util.Map;

public interface Picture {
    public String getUniqueId();
    public Uri getUri();
    public Double getLongitude();
    public Double getLatitude();
    public Map<String, Double> getScoreboard();
    public Map<String, Map<String, Double>> getGuesses();
}
