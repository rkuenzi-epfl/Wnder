package com.github.wnder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        TextView textView = toolbar.findViewById(R.id.username);
        User user = GlobalUser.getUser();
        textView.setText(user.getName());

        ImageView imageView = toolbar.findViewById(R.id.profile_picture);
        imageView.setImageURI(user.getProfilePicture());

        findViewById(R.id.getPictureButton).setOnClickListener(this);
        findViewById(R.id.uploadPictureButton).setOnClickListener(this);
        findViewById(R.id.menuToHistoryButton).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.uploadPictureButton:
                openUploadActivity();
                break;
            case R.id.getPictureButton:
                openPreviewActivity();
                break;
            case R.id.menuToHistoryButton:
                openHistoryActivity();
                break;
            default:
                break;
            // Other buttons can be setup in this switch
        }
    }

    private void openUploadActivity() {
        Intent intent = new Intent(this, ImageFromGalleryActivity.class);
        startActivity(intent);
    }

    private void openPreviewActivity() {
        Intent intent = new Intent(this, GuessPreviewActivity.class);
        startActivity(intent);
    }

    private void openHistoryActivity() {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }
}
