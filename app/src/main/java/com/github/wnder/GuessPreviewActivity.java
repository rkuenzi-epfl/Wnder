package com.github.wnder;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.location.Location;

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
import com.github.wnder.picture.ReportedPictures;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.User;

import static com.github.wnder.picture.ReportedPictures.addToReportedPictures;

public class GuessPreviewActivity extends AppCompatActivity{

    //EPFL Location
    public static final double DEFAULT_LAT = 46.5197;
    public static final double DEFAULT_LNG = 6.5657;

    private User user;
    private double pictureLat = DEFAULT_LAT;
    private double pictureLng = DEFAULT_LNG;
    private ExistingPicture previewPicture;
    private boolean reported = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_preview);
        findViewById(R.id.guessButton).setOnClickListener(id -> openGuessActivity());
        findViewById(R.id.skipButton).setOnClickListener(id -> openPreviewActivity());
        findViewById(R.id.reportButton).setOnClickListener(id -> reportImage());
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = GlobalUser.getUser();

        try {
            user.onNewPictureAvailable((LocationManager)getSystemService(Context.LOCATION_SERVICE), this, (picId) -> {
                if(!picId.equals("")){
                    // new ExistingPicture(picId).onBitmapAvailable((bmp)-> setImageViewBitmap(bmp));
                    previewPicture = new ExistingPicture(picId);
                    previewPicture.onBitmapAvailable((bmp) -> setImageViewBitmap(bmp));
                    previewPicture.onLocationAvailable((Lct) -> {
                        pictureLat = Lct.getLatitude();
                        pictureLng = Lct.getLongitude();
                    });
                } else{
                    // Maybe create a bitmap that tells that no pictures were available (this one is just the one available)
                    Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.raw.ladiag);
                    setImageViewBitmap(bmp);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openGuessActivity() {
        Intent intent = new Intent(this, GuessLocationActivity.class);

        intent.putExtra(GuessLocationActivity.EXTRA_CAMERA_LAT, user.getLocation().getLatitude());
        intent.putExtra(GuessLocationActivity.EXTRA_CAMERA_LNG, user.getLocation().getLatitude());
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_LAT, pictureLat);
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_LNG, pictureLng);
        intent.putExtra(GuessLocationActivity.EXTRA_DISTANCE, getIntent().getExtras().getInt(GuessLocationActivity.EXTRA_DISTANCE));

        startActivity(intent);
        finish();
    }

    private void openPreviewActivity() {
        Intent intent = new Intent(this, GuessPreviewActivity.class);
        startActivity(intent);
    }

    private void reportImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(R.string.report_confirm_title);
        builder.setMessage(R.string.report_confirm_message);
        builder.setPositiveButton("Confirm",
                (DialogInterface dialog, int which) -> {
                        if(reported){
                            previewPicture.updateKarma(-1); //TODO discuss the report karma policy
                            addToReportedPictures(previewPicture.getUniqueId());
                            reported = true;
                        }
                });
        builder.setNegativeButton(android.R.string.cancel, (DialogInterface dialog, int which) -> {});

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setImageViewBitmap(Bitmap bmp){
        ImageView img = findViewById(R.id.imagePreview);
        img.setImageBitmap(bmp);
    }
}

