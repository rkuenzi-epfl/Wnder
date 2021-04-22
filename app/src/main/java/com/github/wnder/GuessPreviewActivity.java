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

import com.github.wnder.picture.ExistingPicture;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import androidx.appcompat.app.AppCompatActivity;

import com.github.wnder.picture.ExistingPicture;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.User;

public class GuessPreviewActivity extends AppCompatActivity{

    public static final String EXTRA_GUESSLAT = "guessLat";
    public static final String EXTRA_GUESSLNG = "guessLng";
    public static final String EXTRA_CAMERALAT = "cameraLat";
    public static final String EXTRA_CAMERALNG = "cameraLng";

    private String displayedPic = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_preview);
        findViewById(R.id.guessButton).setOnClickListener(id -> openGuessActivity());
        findViewById(R.id.skipButton).setOnClickListener(id -> {
            if(!displayedPic.isEmpty()){
                new ExistingPicture(displayedPic).skipPicture();
            }
            openPreviewActivity();
        });
        findViewById(R.id.reportButton).setOnClickListener(id -> reportImage());
    }

    @Override
    protected void onStart() {
        super.onStart();
        User user = GlobalUser.getUser();

        try {
            user.onNewPictureAvailable((LocationManager)getSystemService(Context.LOCATION_SERVICE), this, (picId) -> {
                if(!picId.equals("")){
                    displayedPic = picId;
                    new ExistingPicture(picId).onBitmapAvailable((bmp)-> setImageViewBitmap(bmp));
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

    private void openGuessActivity() {
        // TODO: Load actual camera and picture location
        Intent intent = new Intent(this, GuessLocationActivity.class);
        intent.putExtra(GuessLocationActivity.EXTRA_CAMERA_LAT, 5.0);
        intent.putExtra(GuessLocationActivity.EXTRA_CAMERA_LNG, 5.0);
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_LAT, 46.5197);
        intent.putExtra(GuessLocationActivity.EXTRA_PICTURE_LNG, 6.5657);
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
                        //TODO Update the image karma in the database accordingly to the report policy and ad it to the reported pictures
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

