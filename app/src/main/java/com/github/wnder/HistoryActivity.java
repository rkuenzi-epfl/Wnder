package com.github.wnder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.wnder.picture.ExistingPicture;
import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.SignedInUser;
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
    //For now, a placeholder
    private ImageView image;

    private Button left_button;
    private Button right_button;

    //Cyclic variables to display picture
    private int pictures_max_number = 0;
    private int picture_index = 0;

    //@Inject
    //private PicturesDatabase picDB;
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

        pictureList = getUserPictures();

        //Buttons to cycle between the images
        left_button = findViewById(R.id.left_history);
        right_button = findViewById(R.id.right_history);
        setupButtons();

        //set placeholder
        image = findViewById(R.id.historyImage);
        image.setImageURI(Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
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
            //TODO: add default image or just nothing
        }
        else{
            pictures_max_number = pictureList.size() - 1;
            picture_index = 0;

            //Set the first image to be displayed
            setImage(0);

            left_button.setOnClickListener(view ->{
                picture_index = picture_index == 0 ? pictures_max_number : picture_index - 1;
                setImage(picture_index);
            });

            right_button.setOnClickListener(view ->{
                picture_index = picture_index == pictures_max_number ? 0 : picture_index + 1;
                setImage(picture_index);
            });
        }
    }

    //Set the image, both for imageView and for the existingPicture
    private void setImage(int index){
        if(index < 0 || index > pictures_max_number){
            throw new IllegalArgumentException();
        }

        pictureList.get(index).onBitmapAvailable(bmp ->image.setImageBitmap(bmp));
        pictureDisplayed = pictureList.get(index);
    }

    private List<ExistingPicture> getUserPictures(){
        List<ExistingPicture> picsList = new ArrayList<>();
        User user = GlobalUser.getUser();

        if(user instanceof SignedInUser) { //We can search for the pictures of the signed in user on the online database
            ((SignedInUser) user).onGuessedPicturesAvailable(guessedPics -> {
                for (String id : guessedPics) {
                    picsList.add(new ExistingPicture(id));
                }
            });
        }
        else { //We have a guest user
            //TODO: use the local cache of the phone to get potential images from the user
        }

        return picsList;
    }
}