package com.github.wnder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class ImageFromGallery extends AppCompatActivity {
    private Button findImage;
    private Uri imageUri;
    private TextView imageRef;
    private ImageView imageSelected;
    private Button download_button;
    private static final int SELECT_IMAGE = 0;
    Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_from_image);
        findImage = findViewById(R.id.getGalleryImage);
        download_button = findViewById(R.id.download_button);
        imageRef = findViewById(R.id.textView);
        imageSelected = findViewById(R.id.imageSelected);
        storage = new Storage();
        findImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        download_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { download_image(); }
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
            //imageSelected.setImageURI(imageUri);
            System.out.println(imageUri);
            storage.uploadToCloudStorage(imageUri, "test/img1.jpg");

        }
    };

    protected void download_image(){
        Uri img_uri = storage.downloadFromCloudStorage("test/img1.jpg");
        System.out.println(img_uri);
        MediaScannerConnection.scanFile(this, new String[]{img_uri.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        System.out.println(uri.getPath());
                    }
                });
        imageSelected.setImageURI(img_uri);
    }


    protected Uri getImageUri(){
        return imageUri;
    }
}