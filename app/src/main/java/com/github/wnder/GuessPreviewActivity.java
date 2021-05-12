package com.github.wnder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.wnder.networkService.NetworkService;
import com.github.wnder.picture.Picture;
import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.User;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

import static com.github.wnder.picture.ReportedPictures.addToReportedPictures;

/**
 * Preview activity class
*/
@AndroidEntryPoint
public class GuessPreviewActivity extends AppCompatActivity{

    //EPFL Location
    public static final double DEFAULT_LAT = 46.5197;
    public static final double DEFAULT_LNG = 6.5657;

    private User user;
    private double pictureLat = DEFAULT_LAT;
    private double pictureLng = DEFAULT_LNG;
    private boolean reported = false;

    private ImageView imageDisplayed;
    private Bitmap bitmap;

    @Inject
    public NetworkService networkInfo;
    @Inject
    public PicturesDatabase picturesDb;

    private static String pictureID = Picture.UNINITIALIZED_ID;
    
    /**
     * executed on activity creation
     * @param savedInstanceState saved instance state
     */
    @SuppressLint("ClickableViewAccessibility") //This suppress is to avoid warning about accessibility problem caused by touching functionality
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set layout
        setContentView(R.layout.activity_guess_preview);

        //Find the image view
        imageDisplayed = findViewById(R.id.imagePreview);

        //Setup buttons
        findViewById(R.id.SaveToGallery).setOnClickListener(id -> saveToGallery());



        //Setup swipe and click action
        imageDisplayed.setOnClickListener(view -> {
            openGuessActivity();
        });
        imageDisplayed.setOnTouchListener(new OnSwipeTouchListener(this){
            @Override
            public boolean onSwipeRight() {
                skipPicture();
                return true;
            }
        });
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if(id == R.id.report){
                reportImage();
                return true;
            }
            else if(id == R.id.help){
                helpMenu();
                return true;
            }
            return false;
        });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.help_menu, popup.getMenu());
        popup.show();
    }


    /**
     * Executed on activity start
     */
    @Override
    protected void onStart() {
        super.onStart();

        //Get user
        user = GlobalUser.getUser();

        //Get a new picture to display
        try {
            user.onNewPictureAvailable((LocationManager)getSystemService(Context.LOCATION_SERVICE), this, (picId) -> {
                if(!picId.equals("")){
                    //If there is a picture, display it
                    picturesDb.getBitmap(picId).thenAccept((bmp) -> setImageViewBitmap(bmp, picId));
                    picturesDb.getLocation(picId).thenAccept((Lct) -> {
                        pictureLat = Lct.getLatitude();
                        pictureLng = Lct.getLongitude();
                    });
                } else {
                    //If not, display default picture
                    // Maybe create a bitmap that tells that no pictures were available (this one is just the one available)
                    Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.raw.no_image);
                    setImageViewBitmap(bmp, picId);
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
        if (networkInfo.isNetworkAvailable()){
            Intent intent = new Intent(this, GuessLocationActivity.class);

            intent.putExtra(GuessLocationActivity.EXTRA_CAMERA_LAT, user.getPositionFromGPS((LocationManager) getSystemService(Context.LOCATION_SERVICE), this).getLatitude());
            intent.putExtra(GuessLocationActivity.EXTRA_CAMERA_LNG, user.getPositionFromGPS((LocationManager) getSystemService(Context.LOCATION_SERVICE), this).getLongitude());
            intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_LAT, pictureLat);
            intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_LNG, pictureLng);
            intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_ID, pictureID);


            startActivity(intent);
            finish();
        }
        else{
            AlertDialog alert = AlertBuilder.noConnectionAlert(getString(R.string.no_connection), getString(R.string.no_internet_body), this);
            alert.show();
        }
    }

    /**
     * Opens guess preview activity
     */
    private void skipPicture() {
        if(!pictureID.equals(Picture.UNINITIALIZED_ID)){
            picturesDb.updateKarma(pictureID, -1);
        }

        Intent intent = new Intent(this, GuessPreviewActivity.class);
        startActivity(intent);
    }

    /**
     * Create an alert popup to explain how to use this activity
     */
    private void helpMenu(){
        AlertBuilder.noConnectionAlert(getString(R.string.help_title), getString(R.string.help_body), this).show();
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
                    if(!reported && pictureID != Picture.UNINITIALIZED_ID){
                        picturesDb.updateKarma(pictureID,-10);
                        addToReportedPictures(pictureID);
                        reported = true;
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.imagePreview), R.string.bar_report, BaseTransientBottomBar.LENGTH_SHORT);
                        snackbar.show();
                    }
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
    private void setImageViewBitmap(Bitmap bmp, String newPictureID){
        imageDisplayed.setImageBitmap(bmp);
        bitmap = bmp;
        pictureID = newPictureID;
    }

    private void saveToGallery(){
        if(pictureID.equals(Picture.UNINITIALIZED_ID)){
            //Snack bar
            Snackbar snackbar = Snackbar.make(findViewById(R.id.imagePreview), R.string.bar_save_is_impossible, BaseTransientBottomBar.LENGTH_SHORT);
            snackbar.show();
        }
        else{
            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, pictureID, "");
            Snackbar snackbar = Snackbar.make(findViewById(R.id.imagePreview), R.string.bar_save_is_ok, BaseTransientBottomBar.LENGTH_SHORT);
            snackbar.show();
        }
    }
}

