package com.github.wnder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class GuessPreviewActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String EXTRA_GUESSLAT = "guessLat";
    public static final String EXTRA_GUESSLNG = "guessLng";
    public static final String EXTRA_CAMERALAT = "cameraLat";
    public static final String EXTRA_CAMERALNG = "cameraLng";

    private Button guessButton;
    private Button skipButton;
    private Button reportButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_preview);

        guessButton = findViewById(R.id.guessButton);
        guessButton.setOnClickListener((view) -> { openGuessActivity(); });

        skipButton = findViewById(R.id.skipButton);
        skipButton.setOnClickListener((view) -> { openPreviewActivity(); });

        reportButton = findViewById(R.id.reportButton);
        reportButton.setOnClickListener((view) -> { reportImage(); });
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
}

