package com.github.wnder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.wnder.picture.ExistingPicture;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;
import com.github.wnder.user.User;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Defines activity for history
 */
public class HistoryActivity extends AppCompatActivity {
    //Displayed image
    private ImageView image;
    //Text displayed in case of no image available
    private TextView text;

    private Button leftButton;
    private Button rightButton;

    //Cyclic variable to display picture
    private int pictureIndex = 0;

    private List<ExistingPicture> pictureList; //To be filled with the appropriate function (from either the local or online database)
    private ExistingPicture pictureDisplayed; //To be used to recover the image we clicked on

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

        pictureList.get(index).onBitmapAvailable(bmp ->image.setImageBitmap(bmp));
        pictureDisplayed = pictureList.get(index);
    }

    private void getUserPictures(){
        List<ExistingPicture> picsList = new ArrayList<>();
        User user = GlobalUser.getUser();

        if(user instanceof SignedInUser) { //We can search for the pictures of the signed in user on the online database
            ((SignedInUser) user).onPicturesAvailable(Storage.GUESSED_PICS, guessedPics -> {
                for (String id : guessedPics) {
                    picsList.add(new ExistingPicture(id));
                }
                pictureList = picsList;
                setupButtons();
            });
        }
        else { //We have a guest user
            //TODO: use the local cache of the phone to get potential images from the user
        }
    }
}