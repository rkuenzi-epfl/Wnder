package com.github.wnder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.User;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private int[] distances = {5, 10, 20, 50, 100, 500, 1000};
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        TextView textView = toolbar.findViewById(R.id.username);
        User user = GlobalUser.getUser();
        textView.setText(user.getName());

        ImageView imageView = toolbar.findViewById(R.id.profile_picture);
        imageView.setImageURI(user.getProfilePicture());

        findViewById(R.id.getPictureButton).setOnClickListener(this);
        findViewById(R.id.uploadPictureButton).setOnClickListener(this);

        //SeekBar for radius
        SeekBar radiusSeekBar = (SeekBar) findViewById(R.id.radiusSeekBar);
        TextView radiusTextView = findViewById(R.id.radiusTextView);

        int userRad = GlobalUser.getUser().getRadius();
        for(int i = 0; i < distances.length; i++){
            if(userRad == distances[i]){
                radiusSeekBar.setProgress(i);
                radiusTextView.setText("Radius: "+distances[i]+"km");
                break;
            }
        }
        
        radiusSeekBar.setOnSeekBarChangeListener(this);

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

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        User u = GlobalUser.getUser();
        u.setRadius(distances[i]);
        TextView radiusTextView = findViewById(R.id.radiusTextView);
        radiusTextView.setText("Radius: "+distances[i]+"km");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //do nothing
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //do nothing
    }
}
