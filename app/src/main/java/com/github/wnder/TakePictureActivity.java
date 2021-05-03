package com.github.wnder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.github.wnder.picture.NewPicture;
import com.github.wnder.user.GlobalUser;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

/**
 * Activity to take a picture
 */
public class TakePictureActivity extends AppCompatActivity {
    //Buttons
    private Button takePictureButton;
    private Button pictureConfirmButton;
    //Image view
    private ImageView picture;
    private final int TAKE_PHOTO = 0;
    //Strings
    private String currentPhotoPath;
    private String imageFileName;
    //Image
    private Uri imageUri;
    private Bitmap currentBitmap;
    public static final String HAS_SUCCEEDED = "success";

    /**
     * Executes when activity is created
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set layout
        setContentView(R.layout.activity_take_picture);
        //get image from the camera
        picture = findViewById(R.id.imageFromCamera);

        //button to take picture
        takePictureButton = findViewById(R.id.takePictureButton);
        takePictureButton.setOnClickListener((view) -> dispatchTakePictureIntent());

        //button to confirm the taken picture, once confirmed, stored in gallery
        pictureConfirmButton = findViewById(R.id.pictureConfirmButton);
        pictureConfirmButton.setVisibility(View.INVISIBLE);
        pictureConfirmButton.setOnClickListener((view) -> {
            boolean IS_ONLINE = false;
            if (IS_ONLINE){
                storeBitmapInGallery();

                boolean hasSucceeded = storeBitmapInDB();

                Intent intent = new Intent(this, UploadActivity.class);
                intent.putExtra(HAS_SUCCEEDED, hasSucceeded);
                startActivity(intent);
                this.finish();
            }
            else{
                openAlertDialogWhenNoConnection("Your internet connection was lost, please try again later");
            }
        });
    }

    private void openAlertDialogWhenNoConnection(String message){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("No internet connection !");
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * Do something based on taking photo activity result
     * @param requestCode request code
     * @param resultCode result code
     * @param cameraIntent intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent cameraIntent){
        super.onActivityResult(requestCode, resultCode, cameraIntent);
        //If everything is ok + we just took a photo, then proceed to show it and to make confirm button visible
        if(resultCode == RESULT_OK && requestCode == TAKE_PHOTO){
            setPic();
            pictureConfirmButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Method that stores a bitmap into the gallery
     */
    private void storeBitmapInGallery(){
        MediaStore.Images.Media.insertImage(getContentResolver(), currentBitmap, imageFileName , "");
    }

    /**
     * Method that stores a bitmap into the db
     * @return
     */
    private boolean storeBitmapInDB(){
        try {
            Location loc = GlobalUser.getUser().getPositionFromGPS((LocationManager)getSystemService(Context.LOCATION_SERVICE), this);
            NewPicture picture = new NewPicture(GlobalUser.getUser().getName(), loc, imageUri);

            CompletableFuture<Void> futur = picture.sendPictureToDb();

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Method to create an image file
     * @return image File
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Dispatches taken picture intent
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            imageUri = FileProvider.getUriForFile(this,
                    "com.github.wnder.android.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePictureIntent, TAKE_PHOTO);
        }

    }

    /**
     * Displays taken pic on the screen
     */
    private void setPic() {
        // Get the dimensions of the View
        int targetW = picture.getWidth();
        int targetH = picture.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        currentBitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        picture.setImageBitmap(currentBitmap);
    }
}


