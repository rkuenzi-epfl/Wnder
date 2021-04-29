package com.github.wnder.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

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
    private String imagesFolderPath = new File(context.getFilesDir(), "images").getPath();
    private String metadataFolder = new File(context.getFilesDir(), "metadata").getPath();

    public String getImagesFolderPath(){
        return imagesFolderPath;
    }

    public String getMetadataFolder(){
        return metadataFolder;
    }

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
    public void storePictureAndMetadata(String uniqueId, Bitmap bmp, Location realLocation, Location guessedLocation, Map<String, Double> scoreboard) throws IOException {
        String serializedPicture = LocalPictureSerializer.seralizePicture(uniqueId, realLocation, guessedLocation, scoreboard);
        storeMetadataFile(serializedPicture, metadataFolder);
        storePictureFile(bmp, imagesFolderPath);
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
     * @throws FileNotFoundException if there file does not exist
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

    public Bitmap openPictureFile(String uniqueId) throws FileNotFoundException {
        File file = new File(uniqueId);
        FileInputStream fis = context.openFileInput(uniqueId);
        byte[] bytes = new byte[(int) file.length()];
        try {
            fis.read(bytes, 0, bytes.length);
            fis.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public void storePictureFile(Bitmap bmp, String path) throws IOException {
        FileOutputStream fileobj = context.openFileOutput(path, Context.MODE_PRIVATE);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        fileobj.write(stream.toByteArray()); //writing to file
        fileobj.close(); //File closed
    }
}
