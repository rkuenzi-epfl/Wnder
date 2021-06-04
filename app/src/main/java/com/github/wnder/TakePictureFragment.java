package com.github.wnder;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.transition.TransitionManager;

import com.github.wnder.networkService.NetworkService;
import com.github.wnder.picture.PicturesDatabase;
import com.github.wnder.picture.UploadInfo;
import com.github.wnder.tour.FirebaseTourDatabase;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.GuestUser;
import com.github.wnder.user.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
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

    private FirebaseTourDatabase tourDb;

    private CoordinatorLayout coordinatorLayout;
    private FloatingActionButton takePictureButton;
    private ViewGroup.MarginLayoutParams takePictureButtonParams;
    private int takePictureButtonOffset;
    private FloatingActionButton uploadButton;
    private FloatingActionButton activateTourMode;

    private EditText enterText;
    private TextView numberOfPictures;
    private Button validateTour;

    private boolean tourMode;
    private List<Pair<String, UploadInfo>> tourPictures;
    private static final int MAX_NUMBER_OF_TOUR = 10;

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
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            AlertBuilder.okAlert(getString(R.string.gps_missing_title), getString(R.string.gps_missing_body), getContext()).show();
            return;
        }

        tourDb = new FirebaseTourDatabase(this.getContext());

        coordinatorLayout = view.findViewById(R.id.takePictureCoordinator);
        takePictureButton = view.findViewById(R.id.takePictureButton);
        takePictureButtonParams = (ViewGroup.MarginLayoutParams) takePictureButton.getLayoutParams();
        activateTourMode = view.findViewById(R.id.activateTour);
        enterText = view.findViewById(R.id.enterName);
        numberOfPictures = view.findViewById(R.id.numberOfPictures);
        validateTour = view.findViewById(R.id.validateTour);

        validateTour.setVisibility(View.INVISIBLE);
        enterText.setVisibility(View.INVISIBLE);
        numberOfPictures.setVisibility(View.INVISIBLE);
        tourMode = false;

        // Convert button size to dp
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56f, r.getDisplayMetrics());
        takePictureButtonOffset = takePictureButtonParams.bottomMargin*2 + (int) px;

        uploadButton = view.findViewById(R.id.uploadButton);
        uploadButton.hide();

        takenPictureView = getView().findViewById(R.id.takenPicture);
        previewView = view.findViewById(R.id.previewView);

        user = GlobalUser.getUser();
        userName = user.getName();

        activateTourMode.setOnClickListener(button -> {
            activateTourMode();
        });

        if (GlobalUser.getUser() instanceof GuestUser) {
            NavigationActivity navigationActivity = (NavigationActivity) this.getActivity();
            navigationActivity.selectItem(R.id.profile_page);
            AlertBuilder.okAlert(getString(R.string.guest_not_allowed), getString(R.string.guest_no_upload), view.getContext())
                    .show();
        } else {
            if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                initializeCameraPreview();
            } else {
                ActivityResultLauncher<String> requestPermissionLauncher =
                        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                            if (isGranted) {
                                initializeCameraPreview();
                            }
                        });
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        }
    }

    /**
     * Initialize the camera preview view and the shutter button
     */
    private void initializeCameraPreview() {
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

    /**
     * Try to take a picture and transition to the upload view if that worked
     * @param imageCapture the image capture instance needed to get the picture
     */
    private void takePicture(ImageCapture imageCapture) {
        String takenPictureId = userName + Calendar.getInstance().getTimeInMillis();

        activateTourMode.setVisibility(View.INVISIBLE);

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
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
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

    /**
     * Transition to the upload view which shows a preview of the picture that was just taken
     * @param takenPictureId the identifier of the picture
     * @param takenPictureUri the uri of the picture
     */
    private void transitionToUpload(String takenPictureId, Uri takenPictureUri) {
        // Move takePictureButton up and display upload button
        TransitionManager.beginDelayedTransition(coordinatorLayout);
        takePictureButtonParams.setMargins(takePictureButtonParams.leftMargin, takePictureButtonParams.topMargin, takePictureButtonParams.rightMargin, takePictureButtonOffset);
        coordinatorLayout.requestLayout();
        uploadButton.show();

        takenPictureView.setImageURI(takenPictureUri);
        takenPictureView.setVisibility(View.VISIBLE);
        previewView.setVisibility(View.INVISIBLE);

        if(tourMode){
            Location takenPictureLocation = user.getPositionFromGPS((LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE), getContext());
            tourPictures.add(new Pair<>(takenPictureId, new UploadInfo(userName, takenPictureLocation, takenPictureUri)));
            if(!setTextForTourSize(tourPictures.size())){
                takePictureButton.setVisibility(View.INVISIBLE);
            }
            uploadButton.setOnClickListener(button -> ChooseTourName());
        }
        else{
            uploadButton.setOnClickListener(button -> uploadTakenPicture(takenPictureId, takenPictureUri));
        }

        takePictureButton.setOnClickListener(_button -> {
            transitionToBase();
            takenPictureView.setVisibility(View.INVISIBLE);
            previewView.setVisibility(View.VISIBLE);
            takePictureButton.setOnClickListener(button -> takePicture(imageCapture));
        });
    }

    /**
     * Go back from the upload view to the preview view
     */
    private void transitionToBase() {
        // Move takePictureButton down and hide upload button
        if(!tourMode){
            activateTourMode.setVisibility(View.VISIBLE);
        }
        uploadButton.hide();
        TransitionManager.beginDelayedTransition(coordinatorLayout);
        takePictureButtonParams.setMargins(takePictureButtonParams.leftMargin, takePictureButtonParams.topMargin, takePictureButtonParams.rightMargin, takePictureButtonParams.leftMargin);
        coordinatorLayout.requestLayout();
    }

    /**
     * Try to upload a picture to the database
     * @param takenPictureId the identifier of the picture
     * @param takenPictureUri the uri of the picture
     */
    private void uploadTakenPicture(String takenPictureId, Uri takenPictureUri) {
        if(user instanceof GuestUser){
            Snackbar.make(getView(), R.string.guest_no_upload, Snackbar.LENGTH_SHORT).show();
        } else {
            Location takenPictureLocation = user.getPositionFromGPS((LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE), getContext());
            UploadInfo uploadInfo = new UploadInfo(userName, takenPictureLocation, takenPictureUri);
            CompletableFuture<Void> uploadResult = picturesDb.uploadPicture(takenPictureId, uploadInfo);

            if(!uploadResult.isCompletedExceptionally()) {
                transitionToBase();
                Snackbar.make(getView(), R.string.upload_started, Snackbar.LENGTH_SHORT).show();
                uploadResult.thenAccept(res -> {
                    Snackbar.make(getView(), R.string.upload_successful, Snackbar.LENGTH_SHORT).show();
                }).exceptionally(res -> {
                    Snackbar.make(getView(), R.string.upload_failed, Snackbar.LENGTH_SHORT).show();
                    return null;
                });
            } else {
                Snackbar.make(getView(), R.string.upload_not_started, Snackbar.LENGTH_SHORT).show();
            }
        }
    }
    
    private void ChooseTourName(){
        //Hide and show the concerned part of the UI
        enterText.setVisibility(View.VISIBLE);
        validateTour.setVisibility(View.VISIBLE);
        uploadButton.setVisibility(View.INVISIBLE);
        takePictureButton.setVisibility(View.INVISIBLE);
        numberOfPictures.setVisibility(View.INVISIBLE);
        validateTour.setOnClickListener(button -> finalizeTour());
    }

    private void finalizeTour(){
        //To avoid empty string as name
        if(enterText.getText().toString().equals("")){
            return;
        }
        if(user instanceof GuestUser){
            Snackbar.make(getView(), R.string.guest_tour_forbidden, Snackbar.LENGTH_SHORT).show();
        }
        else{
            List<String> pictures = new ArrayList<>();
            for(Pair<String, UploadInfo> pair : tourPictures){

                CompletableFuture<Void> uploadPic = picturesDb.uploadPicture(pair.first, pair.second);

                uploadPic.exceptionally(res -> {
                    Snackbar.make(getView(), R.string.failed_tour_picture_upload, Snackbar.LENGTH_SHORT).show();
                    pictures.clear(); //Signify that we lost one picture along the way
                    return null;
                }).thenAccept(res -> {
                    pictures.add(pair.first);

                    if(pictures.size() == tourPictures.size()){ //Which mean that we did not lose any pictures along the way and the tour can be uploaded
                        CompletableFuture<Void> uploadTour = tourDb.uploadTour(tourDb.generateTourUniqueId(enterText.getText().toString()), enterText.getText().toString(), pictures);

                        uploadTour.thenAccept(ress -> {
                            Snackbar.make(getView(), R.string.tour_uploaded, Snackbar.LENGTH_SHORT).show();
                        }).exceptionally(ress -> {
                            Snackbar.make(getView(), R.string.tour_upload_failed, Snackbar.LENGTH_SHORT).show();
                            return null;
                        });
                    }
                });
            }
        }
    }

    private void fromTourToNormal(){
        enterText.setVisibility(View.INVISIBLE);
        validateTour.setVisibility(View.INVISIBLE);
        takePictureButton.setVisibility(View.VISIBLE);

        tourMode = false;
        transitionToBase();
    }

    /**
     * Modify the text indicating the number of pictures taken for the tour
     * @param size of the tour
     * @return true if we can add more photos, false otherwise
     */
    private boolean setTextForTourSize(int size){
        if(size >= MAX_NUMBER_OF_TOUR){
            numberOfPictures.setText(String.format(Locale.ENGLISH, "%d", size));
            return false;
        }
        else{
            numberOfPictures.setText(String.format(Locale.ENGLISH, "%d", size));
            return true;
        }
    }

    private void activateTourMode(){
        activateTourMode.setVisibility(View.INVISIBLE);
        numberOfPictures.setVisibility(View.VISIBLE);
        tourPictures = new ArrayList<>();
        setTextForTourSize(tourPictures.size());
        tourMode = true;
    }
}
