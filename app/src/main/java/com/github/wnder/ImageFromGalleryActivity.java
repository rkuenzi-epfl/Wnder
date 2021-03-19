package com.github.wnder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class ImageFromGalleryActivity extends AppCompatActivity {
    private Button findImage;
    private Uri imageUri;
    private TextView imageRef;
    private ImageView imageSelected;
    private static final int SELECT_IMAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_from_image);
        findImage = findViewById(R.id.getGalleryImage);
        imageRef = findViewById(R.id.textView);
        imageSelected = findViewById(R.id.imageSelected);
        findImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

    }

    protected void openGallery() {
        Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(openGalleryIntent, SELECT_IMAGE);
    }

    private void changeTextWhenTheUriIsNull(Intent openGalleryIntent){
        Bundle extras = openGalleryIntent.getExtras();
        if (extras != null) {
            imageRef.setText(extras.getString("imageReturnedName"));
        } else {
            imageRef.setText("imageUri == null AND extras == null");
        }
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent openGalleryIntent){
        super.onActivityResult(requestCode, resultCode, openGalleryIntent);
        if (resultCode == RESULT_OK && requestCode == SELECT_IMAGE){
            changeText(openGalleryIntent);
        }
    };
}