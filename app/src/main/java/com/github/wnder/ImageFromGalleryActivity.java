package com.github.wnder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity to retrieve an image from the gallery
 */
public class ImageFromGalleryActivity extends AppCompatActivity {

    //Set up all necessary vars
    private Button findImage;
    private Uri imageUri;
    private TextView imageRef;
    private ImageView imageSelected;
    private Button confirmButton;
    private static final int SELECT_IMAGE = 0;
    public static final String HAS_SUCCEEDED = "success";

    /**
     * Executes on activity creation
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set layout
        setContentView(R.layout.gallery_from_image);

        //image of gallery
        findImage = findViewById(R.id.getGalleryImage);
        //image reference
        imageRef = findViewById(R.id.textView);
        //image selected
        imageSelected = findViewById(R.id.imageSelected);
        //confirm button
        confirmButton = findViewById(R.id.confirmUploadButton);

        //When you click on the image of the gallery, open it
        findImage.setOnClickListener((view) -> {
            openGallery();
        });

        //When you confirm your choice, use upload activity to send it to db
        confirmButton.setOnClickListener((view) -> {
            Intent intent = new Intent(this, UploadActivity.class);
            startActivity(intent);
            this.finish();
        });


    }

    /**
     * Opens the gallery to select an image
     */
    protected void openGallery() {
        Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(openGalleryIntent, SELECT_IMAGE);
    }

    /**
     * tool for changeText method changing text when uri is null
     * @param openGalleryIntent intent
     */
    private void changeTextWhenTheUriIsNull(Intent openGalleryIntent){
        Bundle extras = openGalleryIntent.getExtras();
        if (extras != null) {
            imageRef.setText(extras.getString("imageReturnedName"));
        } else {
            imageRef.setText("imageUri == null AND extras == null");
        }
    }

    /**
     * Changes text when we select an image
     * @param openGalleryIntent gallery intent
     */
    private void changeText(Intent openGalleryIntent){
        imageUri = openGalleryIntent.getData();
        if (imageUri == null) { //For the tests only
            changeTextWhenTheUriIsNull(openGalleryIntent);
        } else {
            // Commented out for now: image URI don't look that great
            //imageRef.setText(imageUri.toString());
            imageSelected.setImageURI(imageUri);
        }
    }

    /**
     * Once open gallery activity is done, change the text
     * @param requestCode request code
     * @param resultCode result code
     * @param openGalleryIntent open gallery intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent openGalleryIntent){
        super.onActivityResult(requestCode, resultCode, openGalleryIntent);
        //If everything went well + what we did was select an image, change the text
        if (resultCode == RESULT_OK && requestCode == SELECT_IMAGE){
            changeText(openGalleryIntent);
        }
    };
}