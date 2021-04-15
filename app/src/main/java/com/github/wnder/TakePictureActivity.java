package com.github.wnder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.github.wnder.picture.NewPicture;
import com.github.wnder.user.GlobalUser;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class TakePictureActivity extends AppCompatActivity {
    private Button takePictureButton;
    private Button pictureConfirmButton;
    private ImageView picture;
    private final int TAKE_PHOTO = 0;
    private String currentPhotoPath;
    private String imageFileName;
    private Uri imageUri;
    private Bitmap currentBitmap;
    public static final String HAS_SUCCEEDED = "success";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);
        picture = findViewById(R.id.imageFromCamera);

        takePictureButton = findViewById(R.id.takePictureButton);
        takePictureButton.setOnClickListener((view) -> dispatchTakePictureIntent());

        pictureConfirmButton = findViewById(R.id.pictureConfirmButton);
        pictureConfirmButton.setVisibility(View.INVISIBLE);
        pictureConfirmButton.setOnClickListener((view) -> {
            storeBitmapInGallery();

            boolean hasSucceeded = storeBitmapInDB();

            Intent intent = new Intent(this, UploadActivity.class);
            intent.putExtra(HAS_SUCCEEDED, hasSucceeded);
            startActivity(intent);
            this.finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent cameraIntent){
        super.onActivityResult(requestCode, resultCode, cameraIntent);
        if(resultCode == RESULT_OK && requestCode == TAKE_PHOTO){
            setPic();
            pictureConfirmButton.setVisibility(View.VISIBLE);
        }
    }

    private void storeBitmapInGallery(){
        MediaStore.Images.Media.insertImage(getContentResolver(), currentBitmap, imageFileName , "");
    }

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


