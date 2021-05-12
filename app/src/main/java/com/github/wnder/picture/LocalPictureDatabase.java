package com.github.wnder.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Class serving the interaction with the local db
 */
public class LocalPictureDatabase {
    private final String BITMAP_DIR_NAME = "images";
    private final String MAP_DIR_NAME = "maps";
    private final String METADATA_DIR_NAME = "metadata";
    private File bitmapDirectory;
    private File mapSnapshotDirectory;
    private File metadataDirectory;
    private final String SCOREBOARD = "scoreboard";


    /**
     * Constructor
     * @param context app context
     */
    public LocalPictureDatabase(Context context) {
        // Will be used many times in this class, so init now
        bitmapDirectory = context.getDir(BITMAP_DIR_NAME, Context.MODE_PRIVATE);
        metadataDirectory = context.getDir(METADATA_DIR_NAME, Context.MODE_PRIVATE);
        mapSnapshotDirectory = context.getDir(MAP_DIR_NAME, Context.MODE_PRIVATE);
    }


    /**
     * Store a bitmap to a local file
     * @param bmp bitmap to store
     * @param directory directory in which the bitmap will be stored
     * @param uniqueId uniqueId of the file
     */
    private void storeBitmapFile(Bitmap bmp, File directory, String uniqueId) {
        File myPath = new File(directory, uniqueId);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Store the metadata file
     * @param data the data to write
     * @param uniqueId unique id of picture
     */
    private void storeMetadataFile(String data, String uniqueId) {
        File file = new File(metadataDirectory, uniqueId);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(data.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Store the picture in the internal storage
     * @param picture LocalPicture to store
     */
    public void storePicture(LocalPicture picture) {
        storeBitmapFile(picture.getBitmap(), bitmapDirectory, picture.getUniqueId());

        storeBitmapFile(picture.getMapSnapshot(), mapSnapshotDirectory, picture.getUniqueId());

        String serializedPicture = LocalPictureSerializer.serializePicture(picture.getRealLocation(), picture.getGuessLocation(), picture.getScoreboard());
        storeMetadataFile(serializedPicture, picture.getUniqueId());
    }


    /**
     * Delete a file
     * @param directory directory in which to delete the file
     * @param uniqueId uniqueId of picture
     */
    private void deleteFile(File directory, String uniqueId) {
        new File(directory, uniqueId).delete();
    }

    /**
     * Deletes picture file AND metadata file
     * @param uniqueId uniqueId of picture
     */
    public void deletePicture(String uniqueId) {
        deleteFile(bitmapDirectory, uniqueId);
        deleteFile(mapSnapshotDirectory, uniqueId);
        deleteFile(metadataDirectory, uniqueId);
    }


    /**
     * Load a bitmap from a local file
     * @param directory directory from which the bitmap will be loaded
     * @param uniqueId the id of the picture
     * @return a bitmap of the picture
     */
    private Bitmap loadBitmapFile(File directory, String uniqueId) {
        File file = new File(directory, uniqueId);
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];

            fis.read(bytes, 0, bytes.length);
            fis.close();

            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return null;
        }
    }

    /**
     * Load the metadata file
     * @param uniqueId uniqueId of picture
     * @return content of file, empty if there is a problem
     */
    private String loadMetadataFile(String uniqueId){
        String toReturn = "";

        //setup file input stream
        File file = new File(metadataDirectory, uniqueId);
        FileInputStream fis;
        try {
            fis = new FileInputStream(file);

            //setup input stream reader and string builder
            InputStreamReader inputStreamReader =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();

            //use buffer: for as long as there is data, read it.
            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line);
                    line = reader.readLine();
                }

                //close stream
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //cast string builder
                toReturn = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }

        return toReturn;
    }


    /**
     * Get the bitmap of the image from the local database
     * @param uniqueId id of the image
     * @return the bitmap of the image
     */
    public Bitmap getBitmap(String uniqueId) {
        return loadBitmapFile(bitmapDirectory, uniqueId);
    }

    /**
     * Get the map of the image from the local database
     * @param uniqueId id of the image
     * @return the map of the image
     */
    public Bitmap getMapSnapshot(String uniqueId) {
        return loadBitmapFile(mapSnapshotDirectory, uniqueId);
    }

    /**
     * Tool method used to get real or guessed location using serialization
     * @param uniqueId uniqueId of picture
     * @param type real or guess
     * @return real or guessed location, depending on realOrGuess
     */
    private Location getRealOrGuessed(String uniqueId, LocationType type){
        String serializedData = loadMetadataFile(uniqueId);
        JSONObject json = LocalPictureSerializer.deserializePicture(serializedData);

        if (type == LocationType.REAL) {
            return LocalPictureSerializer.getRealLocation(json);
        } else {
            return LocalPictureSerializer.getGuessLocation(json);
        }
    }

    /**
     * Get the location of the image from the local database
     * @param uniqueId id of the image
     * @return the actual location of the image
     */
    public Location getLocation(String uniqueId) {
        return getRealOrGuessed(uniqueId, LocationType.REAL);
    }

    /**
     * Get the location of the image from the local database
     * @param uniqueId id of the image
     * @return the actual location of the image
     */
    public Location getGuessedLocation(String uniqueId) {
        return getRealOrGuessed(uniqueId, LocationType.GUESSED);
    }

    /**
     * Get the scoreboard of the image from the local database
     * @param uniqueId id of the image
     * @return a map of the scoreboard
     */
    public Map<String, Double> getScoreboard(String uniqueId) {
        String serializedData = loadMetadataFile(uniqueId);
        try {
            //deserialize and go back to hashmap
            JSONObject json = LocalPictureSerializer.deserializePicture(serializedData);
            String scoreboard = json.getString(SCOREBOARD);
            return new Gson().fromJson(scoreboard, Map.class);
        } catch (JSONException e){
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Update the scoreboard of the picture in the internal storage
     * @param scoreboard updated scoreboard
     */
    public void updateScoreboard(String uniqueId, Map<String, Double> scoreboard) {
        try {
            //Get and remove old scoreboard
            String oldMetadata = loadMetadataFile(uniqueId);
            JSONObject json = LocalPictureSerializer.deserializePicture(oldMetadata);
            json.remove(SCOREBOARD);

            //put in new scoreboard
            json.put(SCOREBOARD, scoreboard);
            storeMetadataFile(json.toString(), uniqueId);
        } catch (JSONException e){
            e.printStackTrace();
        }
    }
}
