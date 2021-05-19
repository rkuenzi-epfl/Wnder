package com.github.wnder;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.contract.ActivityResultContracts.TakePicture;
import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.transition.TransitionManager;

import com.github.wnder.networkService.NetworkService;
import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.GuestUser;
import com.github.wnder.user.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A {@link Fragment} to take and upload a picture.
 */
@AndroidEntryPoint
public class TakePictureFragment extends Fragment {

    @Inject
    public PicturesDatabase picturesDb;

    @Inject
    public NetworkService networkInfo;

    private ConstraintLayout constraintLayout;
    private FloatingActionButton takePictureButton;
    private FloatingActionButton uploadButton;
    private ImageView takenPictureView;
    private PreviewView previewView;

    private ImageCapture imageCapture;

    private User user;
    private String userName;

    public TakePictureFragment() {
        super(R.layout.fragment_take_picture);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        constraintLayout = view.findViewById(R.id.takePictureConstraint);
        takePictureButton = view.findViewById(R.id.takePictureButton);
        uploadButton = view.findViewById(R.id.uploadButton);
        takenPictureView = getView().findViewById(R.id.takenPicture);
        previewView = view.findViewById(R.id.previewView);

        user = GlobalUser.getUser();
        userName = user.getName();

        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        initializeCameraPreview(view);
                    }
                });

        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initializeCameraPreview(view);
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        // Alert Guest user and user no connected to the internet
        if(GlobalUser.getUser() instanceof GuestUser){
            AlertBuilder.okAlert(getString(R.string.guest_not_allowed), getString(R.string.guest_no_upload), view.getContext())
                    .show();
        } else if(!networkInfo.isNetworkAvailable()){
            AlertBuilder.okAlert(getString(R.string.no_connection), getString(R.string.no_internet_upload), view.getContext())
                    .show();
        }
    }

    private void initializeCameraPreview(@NonNull View view) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this.getContext());
        cameraProviderFuture.addListener(() -> {
            ProcessCameraProvider cameraProvider = null;
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder()
                        .build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();
                cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageCapture, preview);

                takePictureButton.setOnClickListener(button -> {
                    takePicture(imageCapture);
                });
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this.getContext()));
    }

    private void takePicture(ImageCapture imageCapture) {
        String takenPictureId = userName + Calendar.getInstance().getTimeInMillis();

        Uri imageCollection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            imageCollection = MediaStore.Images.Media
                    .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        ContentValues newPictureDetails = new ContentValues();
        newPictureDetails.put(MediaStore.Images.Media.DISPLAY_NAME, takenPictureId);
        newPictureDetails.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(
                getContext().getContentResolver(),
                imageCollection,
                newPictureDetails
        ).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this.getContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NotNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri takenPictureUri = outputFileResults.getSavedUri();
                        transitionToUpload(takenPictureId, takenPictureUri);
                    }

                    @Override
                    public void onError(ImageCaptureException error) {
                        error.printStackTrace();
                    }
                }
        );
    }

    private void transitionToUpload(String takenPictureId, Uri takenPictureUri) {
        TransitionManager.beginDelayedTransition(constraintLayout);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.load(getContext(), R.layout.fragment_take_picture_upload);
        constraintSet.applyTo(constraintLayout);

        takenPictureView.setImageURI(takenPictureUri);
        takenPictureView.setVisibility(View.VISIBLE);
        previewView.setVisibility(View.INVISIBLE);
        uploadButton.setOnClickListener(button -> uploadTakenPicture(takenPictureId, takenPictureUri));
        takePictureButton.setOnClickListener(button -> transitionToBase());
    }

    private void transitionToBase() {
        TransitionManager.beginDelayedTransition(constraintLayout);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.load(getContext(), R.layout.fragment_take_picture);
        constraintSet.applyTo(constraintLayout);

        takenPictureView.setVisibility(View.INVISIBLE);
        previewView.setVisibility(View.VISIBLE);
        takePictureButton.setOnClickListener(button -> takePicture(imageCapture));
    }

    private void uploadTakenPicture(String takenPictureId, Uri takenPictureUri) {
        if(user instanceof GuestUser){
            Snackbar.make(getView(), R.string.guest_no_upload, Snackbar.LENGTH_SHORT)
                    .show();
        } else {
            Location takenPictureLocation = user.getPositionFromGPS((LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE), getContext());
            CompletableFuture<Void> uploadResult = picturesDb.uploadPicture(takenPictureId, userName, takenPictureLocation, takenPictureUri);
            uploadResult.thenAccept(res -> {
                TransitionManager.beginDelayedTransition(constraintLayout);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.load(getContext(), R.layout.fragment_take_picture);
                constraintSet.applyTo(constraintLayout);
                Snackbar.make(getView(), R.string.upload_successful, Snackbar.LENGTH_SHORT)
                        .show();

                uploadButton.setClickable(false);
            }).exceptionally(res -> {

                Snackbar.make(getView(), R.string.upload_failed, Snackbar.LENGTH_SHORT)
                        .show();
                return null;
            });
        }
    }
}