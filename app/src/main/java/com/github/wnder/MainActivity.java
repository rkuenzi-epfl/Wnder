package com.github.wnder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.github.wnder.user.*;



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

        String[] ss = {Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(this, ss, 100); //Very important to have permission for future call

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if(requestCode != 100){
            return;
        }
        //permission to get the location
        for(int i = 0; i < permissions.length; ++i){
            if(permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) && !(grantResults[i] == PackageManager.PERMISSION_GRANTED)){
                // TODO: What happens if the user did not accept?
                throw new UnsupportedOperationException();
            }
        }
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
}
