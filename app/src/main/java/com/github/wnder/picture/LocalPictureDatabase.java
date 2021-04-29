package com.github.wnder.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;

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

public class LocalPictureDatabase {
    private Context context;
    private String imagesFolderPath = new File(context.getFilesDir(), "images").getPath();
    private String metadataFolder = new File(context.getFilesDir(), "metadata").getPath();

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
    public void storePictureAndMetadata(String uniqueId, Bitmap bmp, Location realLocation, Location guessedLocation, Map<String, Double> scoreboard){
        String serializedPicture = LocalPictureSerializer.seralizePicture(uniqueId, realLocation, guessedLocation, scoreboard);
        storeMetadataFile(serializedPicture, metadataFolder);
    }

    /**
     * Update thescoreboard of the picture in the internal storage
     * @param scoreboard
     */
    public void updateScoreboard(Map<String, Double> scoreboard){
        try {
            String oldMetadata = openMetadataFile(metadataFolder);
            JSONObject json = LocalPictureSerializer.deserializePicture(oldMetadata);
            json.remove("scoreboard");
            json.put("scoreboard", scoreboard);
            storeMetadataFile(json.toString(), metadataFolder);
        } catch (FileNotFoundException | JSONException e){
            e.printStackTrace();
        }
    }

    /**
     * Reads the metadata file
     * @return content of file, empty if there is a problem
     * @throws FileNotFoundException
     */
    public String openMetadataFile(String path) throws FileNotFoundException {
        String toReturn = "";
        FileInputStream fis = context.openFileInput(path);
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
        return toReturn;
    }

    /**
     * Stores the updated metadata file
     * @param data the data to write
     */
    private void storeMetadataFile(String data, String uniqueId){
        String filename = uniqueId;
        try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
