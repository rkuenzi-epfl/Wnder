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
    public void storePictureAndMetadata(String uniqueId, Bitmap bmp, Location realLocation, Location guessedLocation, Map<String, Double> scoreboard) throws IOException {
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
        } catch (FileNotFoundException | JSONException e){
            e.printStackTrace();
        }
    }

    /**
     * Reads the metadata file
     * @return content of file, empty if there is a problem
     * @throws FileNotFoundException if there file does not exist
     */
    private String openMetadataFile(String filename) throws FileNotFoundException {
        String toReturn = "";
        File file = new File(metadataFolder, filename);
        FileInputStream fis = context.openFileInput(file.getPath());
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
    private void storeMetadataFile(String data, String filename){
        File file = new File(metadataFolder, filename);
        try (FileOutputStream fos = context.openFileOutput(file.getPath(), Context.MODE_PRIVATE)) {
            fos.write(data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap openPictureFile(String filename) throws FileNotFoundException {
        File directory = context.getDir("images", Context.MODE_PRIVATE);
        File file = new File(directory, filename);
        FileInputStream fis = context.openFileInput(filename);
        byte[] bytes = new byte[(int) file.length()];
        try {
            fis.read(bytes, 0, bytes.length);
            fis.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public void storePictureFile(Bitmap bmp, String filename) throws IOException {
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
