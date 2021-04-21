package com.github.wnder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.wnder.picture.ExistingPicture;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.User;

public class GuessPreviewActivity extends AppCompatActivity{

    public static final String EXTRA_GUESSLAT = "guessLat";
    public static final String EXTRA_GUESSLNG = "guessLng";
    public static final String EXTRA_CAMERALAT = "cameraLat";
    public static final String EXTRA_CAMERALNG = "cameraLng";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_preview);
        findViewById(R.id.guessButton).setOnClickListener(id -> openGuessActivity());
        findViewById(R.id.skipButton).setOnClickListener(id -> openPreviewActivity());
    }

    @Override
    protected void onStart() {
        super.onStart();
        User user = GlobalUser.getUser();

        try {
            user.onNewPictureAvailable((LocationManager)getSystemService(Context.LOCATION_SERVICE), this, (picId) -> {
                if(!picId.equals("")){
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

    private void setImageViewBitmap(Bitmap bmp){
        ImageView img = findViewById(R.id.imagePreview);
        img.setImageBitmap(bmp);
    }
}

