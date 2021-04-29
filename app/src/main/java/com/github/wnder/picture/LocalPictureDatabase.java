package com.github.wnder.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Log;

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
    private final File metadataFolder;

    public LocalPictureDatabase(Context context){
        this.context = context;
        metadataFolder = new File(context.getFilesDir(), "metadata");
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
     * @throws FileNotFoundException if the file does not exist
     * @throws JSONException if Json fails
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
     * @throws FileNotFoundException if the file does not exist
     * @throws JSONException if Json fails
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
     * Reads the metadata file
     * @return content of file, empty if there is a problem
     * @throws FileNotFoundException if the file does not exist
     */
    private String openMetadataFile(String filename) {
        String toReturn = "";
        File file = new File(metadataFolder, filename);
        FileInputStream fis;
        try {
            fis = context.openFileInput(file.getPath());
            InputStreamReader inputStreamReader =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line).append('\n');
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
        File file = new File(metadataFolder, filename);
        try (FileOutputStream fos = context.openFileOutput(file.getPath(), Context.MODE_PRIVATE)) {
            fos.write(data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Bitmap openPictureFile(String filename) {
        File directory = context.getDir("images", Context.MODE_PRIVATE);
        File file = new File(directory, filename);
        try {
            FileInputStream fis = context.openFileInput(file.getPath());
            byte[] bytes = new byte[(int) file.length()];
            try {
                fis.read(bytes, 0, bytes.length);
                fis.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }


    public void storePictureFile(Bitmap bmp, String filename) {
        // path to /data/data/yourapp/app_images
        File directory = context.getDir("images", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory, filename);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
