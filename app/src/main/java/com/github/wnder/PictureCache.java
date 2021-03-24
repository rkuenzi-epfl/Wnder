package com.github.wnder;

import java.util.HashMap;
import java.util.Map;

public class PictureCache {

    private static Map<String, Picture> pictureCache = new HashMap<>();

    private PictureCache(){}

    public static void addPicture(String uniqueID, Picture pic){
        pictureCache.put(uniqueID, pic);
    }

    public static void removePicture(String uniqueID){
        pictureCache.remove(uniqueID);
    }

    public static Picture getPicture(String uniqueID){
        return pictureCache.get(uniqueID);
    }

    public static boolean isInCache(String uniqueID){
        return pictureCache.containsKey(uniqueID);
    }
}
