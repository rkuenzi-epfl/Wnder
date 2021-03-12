package com.github.wnder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class ImageFromGallery extends AppCompatActivity {
    private Button findImage;
    private Uri imageUri;
    private TextView imageRef;
    private ImageView imageSelected;
    private static final int SELECT_IMAGE = 0;
    private Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_from_image);
        findImage = findViewById(R.id.getGalleryImage);
        imageRef = findViewById(R.id.textView);
        imageSelected = findViewById(R.id.imageSelected);
        storage = new Storage();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent openGalleryIntent){
        super.onActivityResult(requestCode, resultCode, openGalleryIntent);
        if (resultCode == RESULT_OK && requestCode == SELECT_IMAGE){
            imageUri = openGalleryIntent.getData();
            imageRef.setText(imageUri.toString());
            imageSelected.setImageURI(imageUri);
        }
    };

    protected File uploadAndDownloadToCloudStorage(){
        String dbPath = "test/image1";
        storage.uploadToCloudStorage(imageUri, dbPath);
        return storage.downloadFromCloudStorage(dbPath);
    }
}