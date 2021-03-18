package com.github.wnder;

import android.net.Uri;

import java.util.Map;

public interface Picture {
    public String getUniqueId();
    public Uri getUri();
    public Long getLongitude();
    public Long getLatitude();
    public Map<String, Object> getScoreboard();
    public Map<String, Object> getGuesses();
}
