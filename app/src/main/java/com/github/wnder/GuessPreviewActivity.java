package com.github.wnder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.github.wnder.picture.ExistingPicture;

public class GuessPreviewActivity extends AppCompatActivity implements View.OnClickListener{

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
        ImageView image = findViewById(R.id.imagePreview);
        image.setImageURI(Uri.parse("android.resource://com.github.wnder/" + R.raw.ladiag));
        // TODO: Get random image from DB and display it
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.guessButton:
                //TODO: modify this once PR 90 is merged
                final String pictureId = "testPicDontRm";
                ExistingPicture picture = new ExistingPicture(pictureId);
                picture.updateKarma(-1);
                openGuessActivity();
                break;
            case R.id.skipButton:
                openPreviewActivity();
                break;
            default:
                break;
                // Other buttons can be setup in this switch
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
}

