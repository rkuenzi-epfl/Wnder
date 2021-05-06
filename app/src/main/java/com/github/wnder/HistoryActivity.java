package com.github.wnder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.wnder.picture.ExistingPicture;
import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.User;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Defines activity for history
 */
@AndroidEntryPoint
public class HistoryActivity extends AppCompatActivity {
    //Displayed image
    private ImageView image;
    //Text displayed in case of no image available
    private TextView text;

    private Button leftButton;
    private Button rightButton;

    //Cyclic variable to display picture
    private int pictureIndex = 0;

    private List<String> pictureList; //To be filled with the appropriate function (from either the local or online database)
    private String pictureDisplayed;

    @Inject
    public PicturesDatabase picturesDb;
    /**
     * Executes when activity is created
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set layout
        setContentView(R.layout.activity_history);

        //Buttons to cycle between the images
        leftButton = findViewById(R.id.leftHistory);
        rightButton = findViewById(R.id.rightHistory);

        image = findViewById(R.id.historyImage);
        text = findViewById(R.id.noPicturesGuessed);

        text.setVisibility(View.INVISIBLE);
        leftButton.setVisibility(View.INVISIBLE);
        rightButton.setVisibility(View.INVISIBLE);
        image.setVisibility(View.INVISIBLE);

        getUserPictures();
        //When clicked open activity for specific picture history
        image.setOnClickListener((view) -> {
            Intent intent = new Intent(this, PictureHistoryActivity.class);
            startActivity(intent);
        });
    }

    private void setupButtons() {
        if(pictureList == null){
            throw new IllegalArgumentException();
        }
        if(pictureList.isEmpty()){
            text.setVisibility(View.VISIBLE);
        }
        else{

            pictureIndex = 0;

            //Set the first image to be displayed
            setImage(pictureIndex);
            image.setVisibility(View.VISIBLE);

            leftButton.setOnClickListener(view ->{
                pictureIndex = Math.floorMod(pictureIndex - 1, pictureList.size());
                setImage(pictureIndex);
            });

            rightButton.setOnClickListener(view ->{
                pictureIndex = Math.floorMod(pictureIndex + 1, pictureList.size());
                setImage(pictureIndex);
            });

            leftButton.setVisibility(View.VISIBLE);
            rightButton.setVisibility(View.VISIBLE);
        }
    }

    //Set the image, both for imageView and for the existingPicture
    private void setImage(int index){
        if(index < 0 || index >= pictureList.size()){
            throw new IllegalArgumentException();
        }

        picturesDb.getBitmap(pictureList.get(index)).thenAccept(bmp ->image.setImageBitmap(bmp));
        pictureDisplayed = pictureList.get(index);
    }

    private void getUserPictures(){

        GlobalUser.getUser().onPicturesAvailable(User.GUESSED_PICS, this, guessedPics -> {
            Log.d("Local pics",guessedPics.toString());
            pictureList = guessedPics;
            setupButtons();
        });
    }
}