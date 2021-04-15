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
        findViewById(R.id.guessButton).setOnClickListener(this);
        findViewById(R.id.skipButton).setOnClickListener(this);

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
        Intent intent = new Intent(this, GuessLocationActivity.class);
        Bundle b = new Bundle();
        b.putDouble(EXTRA_GUESSLAT, 46.5197); //TODO instead of an arbitrary 5 get an appropriate double
        b.putDouble(EXTRA_GUESSLNG, 6.5657); //TODO instead of an arbitrary 5 get an appropriate double
        b.putDouble(EXTRA_CAMERALAT, 5.0); //TODO instead of an arbitrary 5 get an appropriate double
        b.putDouble(EXTRA_CAMERALNG, 5.0); //TODO instead of an arbitrary 5 get an appropriate double
        intent.putExtras(b);
        startActivity(intent);
        finish();
    }

    private void openPreviewActivity() {
        Intent intent = new Intent(this, GuessPreviewActivity.class);
        startActivity(intent);
    }
}

