package com.github.wnder;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.TakePicture;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.transition.TransitionManager;

import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.user.GlobalUser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A {@link Fragment} to take and upload a picture.
 */
@AndroidEntryPoint
public class TakePictureFragment extends Fragment {

    @Inject
    public PicturesDatabase picturesDb;

    private ActivityResultLauncher<Uri> takePictureLauncher;

    private static final int CONSTRAINT_DISTANCE_RIGHT = 16;
    private static final int CONSTRAINT_DISTANCE_BOTTOM = 32;

    private ConstraintLayout constraintLayout;
    private FloatingActionButton takePictureButton;
    private FloatingActionButton uploadButton;
    private String userName;

    private Uri takenPictureUri;
    private String takenPictureId;
    private Location takenPictureLocation;

    public TakePictureFragment() {
        super(R.layout.fragment_take_picture);
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        constraintLayout = (ConstraintLayout) getView();
        takePictureButton = view.findViewById(R.id.takePictureButton);
        uploadButton = view.findViewById(R.id.uploadButton);
        userName = GlobalUser.getUser().getName();
        takePictureLauncher = registerForActivityResult(new TakePicture(), (stored) -> onTakePictureResult(stored));

        takePictureButton.setOnClickListener(button -> openCamera());
        uploadButton.setVisibility(View.INVISIBLE);
        uploadButton.setClickable(false);
    }

    /**
     * Open camera to take photo
     */
    private void openCamera() {
        // Create an id and the Uri where to store the resulting picture
        takenPictureId = userName + Calendar.getInstance().getTimeInMillis();

        // Create the File where the photo should go
        File pictureFile = null;
        try {
            File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            pictureFile = File.createTempFile(takenPictureId, ".jpg", storageDir);
        } catch (IOException ex) {
            // Error occurred while creating the File
        }
        // Continue only if the File was successfully created
        if (pictureFile != null) {
            takenPictureUri = FileProvider.getUriForFile(getContext(),
                    "com.github.wnder.android.fileprovider",
                    pictureFile);
            takePictureLauncher.launch(takenPictureUri);
        }
    }

    /**
     * Handle the result of taking a picture.
     */
    private void onTakePictureResult(boolean stored) {
        if (stored) {
            // Move takePictureButton to the left
            TransitionManager.beginDelayedTransition(constraintLayout);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.load(getContext(), R.layout.fragment_take_picture_upload);
            constraintSet.applyTo(constraintLayout);

            ImageView takenPictureView = getView().findViewById(R.id.takenPicture);
            takenPictureView.setImageURI(takenPictureUri);
            uploadButton.setOnClickListener(button -> uploadTakenPicture());
        }
    }

    /**
     * Upload the picture taken
     */
    private void uploadTakenPicture() {
        takenPictureLocation = GlobalUser.getUser().getPositionFromGPS((LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE), getContext());
        CompletableFuture<Void> uploadResult = picturesDb.uploadPicture(takenPictureId, userName, takenPictureLocation, takenPictureUri);
        uploadResult.thenAccept(res -> {

            // Move takePictureButton to the right
            TransitionManager.beginDelayedTransition(constraintLayout);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.load(getContext(), R.layout.fragment_take_picture);
            constraintSet.applyTo(constraintLayout);

            uploadButton.setClickable(false);
        }).exceptionally(res -> {
            // TODO: Display upload failed message
            return null;
        });
    }
}