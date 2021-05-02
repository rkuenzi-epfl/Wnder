package com.github.wnder.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LocalPictureDatabase {
    private Context context;
    private String IMAGE_DIR_NAME = "images";
    private String METADATA_DIR_NAME = "metadata";

    public LocalPictureDatabase(Context context){
        this.context = context;
    }

    /**
     * Stores the picture in the internal storage
     * @param uniqueId id of the image
     * @param bmp bitmap of the image
     * @param realLocation real location of the image
     * @param guessedLocation location that the user guessed
     * @param scoreboard scoreboard of the image
     */
    public void storePictureAndMetadata(String uniqueId, Bitmap bmp, Location realLocation, Location guessedLocation, Map<String, Double> scoreboard) {
        String serializedPicture = LocalPictureSerializer.seralizePicture(realLocation, guessedLocation, scoreboard);
        storeMetadataFile(serializedPicture, uniqueId);
        storePictureFile(bmp, uniqueId);
    }

    /**
     * Update thescoreboard of the picture in the internal storage
     * @param scoreboard updated scoreboard
     */
    public void updateScoreboard(String uniqueId, Map<String, Double> scoreboard){
        try {
            String oldMetadata = openMetadataFile(uniqueId);
            JSONObject json = LocalPictureSerializer.deserializePicture(oldMetadata);
            json.remove("scoreboard");
            json.put("scoreboard", scoreboard);
            storeMetadataFile(json.toString(), uniqueId);
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    /**
     * Get the location of the image from the local database
     * @param uniqueId id of the image
     * @return the actual location of the image
     */
    public Location getLocation(String uniqueId) {
        String serializedData = openMetadataFile(uniqueId);
        try {
            JSONObject json = LocalPictureSerializer.deserializePicture(serializedData);
            Location location = new Location("");
            location.setLongitude(json.getDouble("realLongiture"));
            location.setLatitude(json.getDouble("realLatitude"));
            return location;
        } catch (JSONException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the location of the image from the local database
     * @param uniqueId id of the image
     * @return the actual location of the image
     */
    public Location getGuessedLocation(String uniqueId) {
        String serializedData = openMetadataFile(uniqueId);
        try {
            JSONObject json = LocalPictureSerializer.deserializePicture(serializedData);
            Location location = new Location("");
            location.setLongitude(json.getDouble("guessedLongitude"));
            location.setLatitude(json.getDouble("guessedLatitude"));
            return location;
        } catch (JSONException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the scoreboard of the image from the local database
     * @param uniqueId id of the image
     * @return a map of the scoreboard
     */
    public Map<String, Double> getScoreboard(String uniqueId) {
        String serializedData = openMetadataFile(uniqueId);
        try {
            JSONObject json = LocalPictureSerializer.deserializePicture(serializedData);
            String scoreboard = json.getString("scoreboard");
            return  new Gson().fromJson(scoreboard, Map.class);
        } catch (JSONException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Reads the metadata file
     * @return content of file, empty if there is a problem
     */
    private String openMetadataFile(String filename){
        String toReturn = "";
        File directory = context.getDir(METADATA_DIR_NAME, Context.MODE_PRIVATE);
        File file = new File(directory, filename);
        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
            InputStreamReader inputStreamReader =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line);
                    line = reader.readLine();
                }
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                toReturn = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }

        return toReturn;
    }

    /**
     * Stores the updated metadata file
     * @param data the data to write
     */
    private void storeMetadataFile(String data, String filename){
        File directory = context.getDir(METADATA_DIR_NAME, Context.MODE_PRIVATE);
        File file = new File(directory, filename);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            // Use the compress method on the BitMap object to write image to the OutputStream
            fos.write(data.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Read the picture from a local file
     * @param filename the id of the picture
     * @return a bitmap of the picture
     */
    public Bitmap getPicture(String filename) throws FileNotFoundException {
        File directory = context.getDir(IMAGE_DIR_NAME, Context.MODE_PRIVATE);
        File file = new File(directory, filename);
        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        try {
            fis.read(bytes, 0, bytes.length);
            fis.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Store a picture in a local file
     * @param bmp picture
     * @param filename uniqueId of the file
     */
    private void storePictureFile(Bitmap bmp, String filename) {
        // path to /data/data/yourapp/app_images
        File directory = context.getDir(IMAGE_DIR_NAME, Context.MODE_PRIVATE);
        // Create imageDir
        File myPath = new File(directory, filename);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete a picture file
     * @param filename uniqueId of picture
     */
    private void deletePictureFile(String filename){
        File directory = context.getDir(IMAGE_DIR_NAME, Context.MODE_PRIVATE);
        new File(directory, filename).delete();
    }

    /**
     * Delete a metadata file
     * @param filename uniqueId of picture
     */
    private void deleteMetadataFile(String filename){
        File directory = context.getDir(METADATA_DIR_NAME, Context.MODE_PRIVATE);
        new File(directory, filename).delete();
    }

    /**
     * Deletes picture file AND metadata file
     * @param filename uniqueId of picture
     */
    public void deleteFile(String filename){
        deletePictureFile(filename);
        deleteMetadataFile(filename);
    }
}
