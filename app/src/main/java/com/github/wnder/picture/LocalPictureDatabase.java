package com.github.wnder.picture;

import android.content.Context;
import android.location.Location;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class LocalPictureDatabase {
    private Context context;

    public LocalPictureDatabase(Context context){
        this.context = context;
    }

    public void storePicture(String uniqueId, Location realLocation, Location guessedLocation, Map<String, Double> scoreboard){

    }

    /**
     * Reads the metadata file
     * @return content of file, empty if there is a problem
     * @throws FileNotFoundException
     */
    public String openMetadataFile(String uniqueId) throws FileNotFoundException {
        String toReturn = "";
        FileInputStream fis = context.openFileInput(uniqueId);
        InputStreamReader inputStreamReader =
                new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
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
