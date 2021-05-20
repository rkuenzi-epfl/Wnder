package com.github.wnder.picture;

import android.location.Location;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;


/**
 * This allows to create, store, load and delete objects with all the information about an
 * a picture that is currently uploading (in order to restart the upload)
 */
public class UploadInfo {

    private static String USER_NAME = "user_name";
    private static String LATITUDE = "latitude";
    private static String LONGITUDE = "longitude";
    private static String FILE_URI = "file_uri";

    String userName;
    Location location;
    Uri pictureUri;

    /**
     * Store a file with the information for an upload
     * @param file the file store to
     * @param uploadInfo the infos to store
     * @throws JSONException
     * @throws IOException
     */
    public static void storeUploadInfo(File file, UploadInfo uploadInfo) throws JSONException, IOException {
        //Create json
        JSONObject json = new JSONObject();
        json.put(USER_NAME, uploadInfo.userName);
        json.put(LATITUDE, uploadInfo.location.getLatitude());
        json.put(LONGITUDE, uploadInfo.location.getLongitude());
        json.put(FILE_URI, uploadInfo.pictureUri.toString());

        // Store in file
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(json.toString().getBytes());
        fos.close();
    }

    /**
     * Get the upload information from a file
     * @param file the file to load from
     * @return a UploadInfo object, null if it fails at any point
     */
    public static UploadInfo loadUploadInfo(File file) {
        if(file.exists()) {

            // Try reading the file
            try {
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
                String content = readUploadInfoFile(fis, inputStreamReader);
                if (content == null) return null;
                // Try constructing a UploadInfo object from the JSON
                JSONObject json = new JSONObject(content);
                return extractUploadInfoFromJSON(json);
            } catch(Exception e){
                return null;
            }
        }
        return null;
    }

    /**
     * Read the upload information from a file
     * @param fis the file input stream
     * @param inputStreamReader the file input stream reader
     * @return a string with the file content
     */
    private static String readUploadInfoFile(FileInputStream fis, InputStreamReader inputStreamReader) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line);
                line = reader.readLine();
            }
            fis.close();
        } catch (IOException e) {
            return null;
        }
        return stringBuilder.toString();
    }

    /**
     * Extract each upload info items from JSONObject
     * @param json the json to extract from
     * @return the resulting UploadInfo
     * @throws JSONException
     */
    private static UploadInfo extractUploadInfoFromJSON(JSONObject json) throws JSONException {
        String userName = json.getString(USER_NAME);
        Location location = new Location("");
        location.setLatitude(json.getDouble(LATITUDE));
        location.setLongitude(json.getDouble(LONGITUDE));
        String pictureUri = json.getString(FILE_URI);

        return new UploadInfo(userName, location, Uri.parse(pictureUri));
    }

    /**
     * Delete an upload inforamtion file
     * @param file the file to delete
     * @return the result of the deletion
     */
    public static boolean deleteUploadInfo(File file){
        return file.delete();
    }

    public UploadInfo(String userName, Location location, Uri uri){
        this.userName = userName;
        this.location = location;
        this.pictureUri = uri;
    }

    /**
     * Get user name
     * @return userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Get user name
     * @return userName
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Get user name
     * @return userName
     */
    public Uri getPictureUri() {
        return pictureUri;
    }
}

