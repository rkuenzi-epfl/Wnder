package com.github.wnder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.wnder.networkService.NetworkService;
import com.github.wnder.picture.Picture;
import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.User;

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
    private String pictureId;
    private boolean reported = false;

    @Inject
    public NetworkService networkInfo;
    @Inject
    public PicturesDatabase picturesDb;

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
        findViewById(R.id.skipButton).setOnClickListener(id -> skipPicture());
        findViewById(R.id.reportButton).setOnClickListener(id -> reportImage());

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
                    pictureId = picId;
                    picturesDb.getBitmap(pictureId).thenAccept((bmp) -> setImageViewBitmap(bmp));
                    pictureID = picId;
                    picturesDb.getLocation(pictureId).thenAccept((Lct) -> {
                        pictureLat = Lct.getLatitude();
                        pictureLng = Lct.getLongitude();
                    });
                } else {
                    //If not, display default picture
                    // Maybe create a bitmap that tells that no pictures were available (this one is just the one available)
                    Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.raw.no_image);
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
            picturesDb.updateKarma(pictureId, -1);
        }

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
                    if(!reported && pictureID != Picture.UNINITIALIZED_ID){
                        picturesDb.updateKarma(pictureId,-10);
                        addToReportedPictures(pictureId);
                        reported = true;
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
    private void setImageViewBitmap(Bitmap bmp){
        ImageView img = findViewById(R.id.imagePreview);
        img.setImageBitmap(bmp);
    }
}

