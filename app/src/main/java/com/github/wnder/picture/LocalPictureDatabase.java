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

/**
 * Class serving the interaction with the local db
 */
public class LocalPictureDatabase {
    private Context context;
    private String IMAGE_DIR_NAME = "images";
    private String METADATA_DIR_NAME = "metadata";
    private File iDirectory;
    private File mDirectory;

    private final int REAL_LOCATION = 0;
    private final int GUESS_LOCATION = 1;

    /**
     * Constructor
     * @param context app context
     */
    public LocalPictureDatabase(Context context){
        this.context = context;
        //Will be used many times in this class, so init now
        iDirectory = context.getDir(IMAGE_DIR_NAME, Context.MODE_PRIVATE);
        mDirectory = context.getDir(METADATA_DIR_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Stores the picture in the internal storage
     * @param picture LocalPicture to store
     */
    public void storePictureAndMetadata(LocalPicture picture) {
        //serialize and store metadata
        String serializedPicture = LocalPictureSerializer.serializePicture(picture.getRealLocation(), picture.getGuessLocation(), picture.getScoreboard());
        storeMetadataFile(serializedPicture, picture.getUniqueId());

        //store picture
        storePictureFile(picture.getBitmap(), picture.getUniqueId());
    }

    /**
     * Update the scoreboard of the picture in the internal storage
     * @param scoreboard updated scoreboard
     */
    public void updateScoreboard(String uniqueId, Map<String, Double> scoreboard){
        try {
            //Get and remove old scoreboard
            String oldMetadata = openMetadataFile(uniqueId);
            JSONObject json = LocalPictureSerializer.deserializePicture(oldMetadata);
            json.remove("scoreboard");

            //put in new scoreboard
            json.put("scoreboard", scoreboard);
            storeMetadataFile(json.toString(), uniqueId);
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    /**
     * Tool method used to get real or guessed location using serialization
     * @param uniqueId uniqueId of picture
     * @param realOrGuess 0 for real, 1 for guess
     * @return real or guessed location, depending on realOrGuess
     */
    private Location getRealOrGuessed(String uniqueId, int realOrGuess){
        //get metadata
        String serializedData = openMetadataFile(uniqueId);
        JSONObject json = LocalPictureSerializer.deserializePicture(serializedData);

        //get real pic
        if (realOrGuess == 0) {
            return LocalPictureSerializer.getRealLocation(json);
        }
        //get guess pic
        else {
            return LocalPictureSerializer.getGuessLocation(json);
        }
    }

    /**
     * Get the location of the image from the local database
     * @param uniqueId id of the image
     * @return the actual location of the image
     */
    public Location getLocation(String uniqueId) {
        return getRealOrGuessed(uniqueId, REAL_LOCATION);
    }

    /**
     * Get the location of the image from the local database
     * @param uniqueId id of the image
     * @return the actual location of the image
     */
    public Location getGuessedLocation(String uniqueId) {
        return getRealOrGuessed(uniqueId, GUESS_LOCATION);
    }

    /**
     * Get the scoreboard of the image from the local database
     * @param uniqueId id of the image
     * @return a map of the scoreboard
     */
    public Map<String, Double> getScoreboard(String uniqueId) {
        //get metadata
        String serializedData = openMetadataFile(uniqueId);
        try {
            //deserialize and go back to hashmap
            JSONObject json = LocalPictureSerializer.deserializePicture(serializedData);
            String scoreboard = json.getString("scoreboard");
            return new Gson().fromJson(scoreboard, Map.class);
        } catch (JSONException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Reads the metadata file
     * @param uniqueId uniqueId of picture
     * @return content of file, empty if there is a problem
     */
    private String openMetadataFile(String uniqueId){
        String toReturn = "";

        //setup file input stream
        File file = new File(mDirectory, uniqueId);
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
     * Stores the updated metadata file
     * @param data the data to write
     * @param uniqueId unique id of picture
     */
    private void storeMetadataFile(String data, String uniqueId){
        //setup file output stream
        File file = new File(mDirectory, uniqueId);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            //write data as bytes
            fos.write(data.getBytes());
            //close stream
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Read the picture from a local file
     * @param uniqueId the id of the picture
     * @return a bitmap of the picture
     */
    public Bitmap getPicture(String uniqueId) throws FileNotFoundException {
        //setup file input stream and byte array
        File file = new File(iDirectory, uniqueId);
        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        try {
            //fulfill byte array
            fis.read(bytes, 0, bytes.length);
            fis.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        //decode byte array to get bitmap
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Store a picture in a local file
     * @param bmp picture
     * @param uniqueId uniqueId of the file
     */
    private void storePictureFile(Bitmap bmp, String uniqueId) {
        //setup file output stream
        File myPath = new File(iDirectory, uniqueId);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            //close stream
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete a picture file
     * @param uniqueId uniqueId of picture
     */
    private void deletePictureFile(String uniqueId){
        new File(iDirectory, uniqueId).delete();
    }

    /**
     * Delete a metadata file
     * @param uniqueId uniqueId of picture
     */
    private void deleteMetadataFile(String uniqueId){
        new File(mDirectory, uniqueId).delete();
    }

    /**
     * Deletes picture file AND metadata file
     * @param uniqueId uniqueId of picture
     */
    public void deleteFile(String uniqueId){
        //delete picture
        deletePictureFile(uniqueId);
        //delete metadata
        deleteMetadataFile(uniqueId);
    }
}
