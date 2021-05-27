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
import androidx.activity.result.contract.ActivityResultContracts.TakePicture;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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

    @Inject
    public NetworkService networkInfo;

    private FirebaseTourDatabase tourDb;

    private ActivityResultLauncher<Uri> takePictureLauncher;

    private CoordinatorLayout coordinatorLayout;
    private FloatingActionButton takePictureButton;
    private ViewGroup.MarginLayoutParams takePictureButtonParams;
    private int takePictureButtonOffset;
    private FloatingActionButton uploadButton;
    private EditText enterText;
    private TextView numberOfPictures;
    private Button validateTour;

    private boolean tourMode = true;
    private List<Pair<String, UploadInfo>> tourPictures;
    private static final int MAX_NUMBER_OF_TOUR = 10;

    private User user;
    private String userName;

    private Uri takenPictureUri;
    private String takenPictureId;
    private Location takenPictureLocation;

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
        enterText = view.findViewById(R.id.enterName);
        numberOfPictures = view.findViewById(R.id.numberOfPictures);
        validateTour = view.findViewById(R.id.validateTour);

        validateTour.setVisibility(View.INVISIBLE);
        enterText.setVisibility(View.INVISIBLE);
        numberOfPictures.setVisibility(View.INVISIBLE);

        // Convert button size to dp
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56f, r.getDisplayMetrics());
        takePictureButtonOffset = takePictureButtonParams.bottomMargin*2 + (int) px;

        uploadButton = view.findViewById(R.id.uploadButton);
        uploadButton.hide();

        user = GlobalUser.getUser();
        userName = user.getName();

        if(tourMode){
            numberOfPictures.setVisibility(View.VISIBLE);
            tourPictures = new ArrayList<>();
            setTextForTourSize(tourPictures.size());
        }

        // Prepare to open the camera
        takePictureLauncher = registerForActivityResult(new TakePicture(), (stored) -> onTakePictureResult(stored));
        takePictureButton.setOnClickListener(button -> openCamera());

        // Alert Guest user and user no connected to the internet
        if(GlobalUser.getUser() instanceof GuestUser){
            NavigationActivity navigationActivity = (NavigationActivity) this.getActivity();
            navigationActivity.selectItem(R.id.profile_page);
            AlertBuilder.okAlert(getString(R.string.guest_not_allowed), getString(R.string.guest_no_upload), view.getContext()).show();
        } else if(!networkInfo.isNetworkAvailable()){
            Snackbar.make(getView(), R.string.upload_later, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Open camera to take photo
     */
    private void openCamera() {
        // Create an id and the Uri where to store the resulting picture
        takenPictureId = userName + Calendar.getInstance().getTimeInMillis();

        takenPictureUri = setupGalleryFile();
        if (takenPictureUri != null) {

            takePictureLauncher.launch(takenPictureUri);
        } else {
            Snackbar.make(getView(), R.string.could_not_launch_camera, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle the result of taking a picture.
     */
    private void onTakePictureResult(boolean stored) {
        if (stored) {

            // Move takePictureButton up and display upload button
            TransitionManager.beginDelayedTransition(coordinatorLayout);
            takePictureButtonParams.setMargins(takePictureButtonParams.leftMargin, takePictureButtonParams.topMargin, takePictureButtonParams.rightMargin, takePictureButtonOffset);
            coordinatorLayout.requestLayout();
            uploadButton.show();

            if(tourMode){
                takenPictureLocation = user.getPositionFromGPS((LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE), getContext());
                tourPictures.add(new Pair<>(takenPictureId, new UploadInfo(userName, takenPictureLocation, takenPictureUri)));
                if(!setTextForTourSize(tourPictures.size())){
                    takePictureButton.setVisibility(View.INVISIBLE);
                }
                uploadButton.setOnClickListener(button -> ChooseTourName());
            }
            else{

                ImageView takenPictureView = getView().findViewById(R.id.takenPicture);
                takenPictureView.setImageURI(takenPictureUri);
                uploadButton.setOnClickListener(button -> uploadTakenPicture());
            }

        } else {
            Snackbar.make(getView(), R.string.no_picture_from_camera, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Upload the picture taken
     */
    private void uploadTakenPicture() {
        if(user instanceof GuestUser){
            Snackbar.make(getView(), R.string.guest_no_upload, Snackbar.LENGTH_SHORT).show();
        } else {
            takenPictureLocation = user.getPositionFromGPS((LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE), getContext());
            UploadInfo uploadInfo = new UploadInfo(userName, takenPictureLocation, takenPictureUri);
            CompletableFuture<Void> uploadResult = picturesDb.uploadPicture(takenPictureId, uploadInfo);

            if(!uploadResult.isCompletedExceptionally()) {
                Snackbar.make(getView(), R.string.upload_started, Snackbar.LENGTH_SHORT).show();
                // Move takePictureButton down and hide upload button
                uploadButton.hide();
                TransitionManager.beginDelayedTransition(coordinatorLayout);
                takePictureButtonParams.setMargins(takePictureButtonParams.leftMargin, takePictureButtonParams.topMargin, takePictureButtonParams.rightMargin, takePictureButtonParams.leftMargin);
                coordinatorLayout.requestLayout();

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

    /**
     * Add the picture just taken to the shared media
     */
    private Uri setupGalleryFile(){
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
        return getContext().getContentResolver().insert(imageCollection, newPictureDetails);
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
        if(user instanceof GuestUser){
            Snackbar.make(getView(), "Guest user are not allowed to upload tour", Snackbar.LENGTH_SHORT).show();
        }
        else{
            List<String> pictures = new ArrayList<>();
            for(Pair<String, UploadInfo> pair : tourPictures){

                CompletableFuture<Void> uploadPic = picturesDb.uploadPicture(pair.first, pair.second);

                uploadPic.exceptionally(res -> {
                    pictures.clear(); //Signify that we lost one picture along the way
                    return null;
                }).thenAccept(res -> {
                    pictures.add(pair.first);

                    if(pictures.size() == tourPictures.size()){ //Which mean that we did not lose any pictures along the way
                        CompletableFuture<Void> uploadTour = tourDb.uploadTour(tourDb.generateTourUniqueId(enterText.getText().toString()), enterText.getText().toString(), pictures);

                        uploadTour.thenAccept(ress -> {
                            Snackbar.make(getView(), "Your tour was successfully uploaded.", Snackbar.LENGTH_SHORT).show();
                        }).exceptionally(ress -> {
                            Snackbar.make(getView(), "The upload of the tour failed.", Snackbar.LENGTH_SHORT).show();
                            return null;
                        });
                    }
                });
            }
            if(pictures.size() != tourPictures.size()){
                Snackbar.make(getView(), "One or more pictures failed to be uploaded", Snackbar.LENGTH_SHORT).show();
            }
        }

        enterText.setVisibility(View.INVISIBLE);
        validateTour.setVisibility(View.INVISIBLE);
    }

    /**
     * Modify the text indicating the number of pictures taken for the tour
     * @param size of the tour
     * @return true if we can add more photos, false otherwise
     */
    private boolean setTextForTourSize(int size){
        if(size >= MAX_NUMBER_OF_TOUR){
            numberOfPictures.setText("Your tour has " + size + " pictures. You can't add more.");
            return false;
        }
        else{
            numberOfPictures.setText("Your tour has " + size + " pictures");
            return true;
        }
    }
}
