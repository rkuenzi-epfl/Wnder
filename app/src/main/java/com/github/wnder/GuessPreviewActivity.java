package com.github.wnder;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.wnder.picture.ExistingPicture;
import com.github.wnder.picture.Picture;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.User;

/**
 * Preview activity class
*/
public class GuessPreviewActivity extends AppCompatActivity{

    public static final String EXTRA_GUESSLAT = "guessLat";
    public static final String EXTRA_GUESSLNG = "guessLng";
    public static final String EXTRA_CAMERALAT = "cameraLat";
    public static final String EXTRA_CAMERALNG = "cameraLng";

    private static String pictureID = Picture.UNINITIALIZED_ID;

    /**
     * executed on activity creation
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set layout
        setContentView(R.layout.activity_guess_preview);

        //Setup buttons
        findViewById(R.id.guessButton).setOnClickListener(id -> openGuessActivity());
        findViewById(R.id.skipButton).setOnClickListener(id -> openPreviewActivity());
        findViewById(R.id.reportButton).setOnClickListener(id -> reportImage());
    }

    /**
     * Executed on activity start
     */
    @Override
    protected void onStart() {
        super.onStart();

        //Get user
        User user = GlobalUser.getUser();

        //Get a new picture to display
        try {
            user.onNewPictureAvailable((LocationManager)getSystemService(Context.LOCATION_SERVICE), this, (picId) -> {
                //If there is a picture, display it
                if(!picId.equals(Picture.UNINITIALIZED_ID)){
                    new ExistingPicture(picId).onBitmapAvailable((bmp)-> setImageViewBitmap(bmp));
                    pictureID = picId;

                    //If not, display default picture
                } else{
                    // Maybe create a bitmap that tells that no pictures were available (this one is just the one available)
                    Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.raw.ladiag);
                    setImageViewBitmap(bmp);
                }
            }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Open guessing activity
     */
    private void openGuessActivity() {
        // TODO: Load actual camera and picture location
        Intent intent = new Intent(this, GuessLocationActivity.class);
        intent.putExtra(GuessLocationActivity.EXTRA_CAMERA_LAT, 5.0);
        intent.putExtra(GuessLocationActivity.EXTRA_CAMERA_LNG, 5.0);
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_LAT, 46.5197);
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_LNG, 6.5657);
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_ID, pictureID);
        startActivity(intent);
        finish();
    }

    /**
     * Opens guess preview activity
     */
    private void openPreviewActivity() {
        Intent intent = new Intent(this, GuessPreviewActivity.class);
        startActivity(intent);
    }

    /**
     * Call this method on an image report
     */
    private void reportImage() {
        //Confirm report
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(R.string.report_confirm_title);
        builder.setMessage(R.string.report_confirm_message);

        //What to do when confirmed
        builder.setPositiveButton("Confirm",
                (DialogInterface dialog, int which) -> {
                        //TODO Update the image karma in the database accordingly to the report policy and ad it to the reported pictures
                });
        //Cancellation possible
        builder.setNegativeButton(android.R.string.cancel, (DialogInterface dialog, int which) -> {});

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Displays the bitmap image
     * @param bmp bitmap of image
     */
    private void setImageViewBitmap(Bitmap bmp){
        ImageView img = findViewById(R.id.imagePreview);
        img.setImageBitmap(bmp);
    }
}

