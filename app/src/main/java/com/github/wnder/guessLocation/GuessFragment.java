package com.github.wnder.guessLocation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.github.wnder.AlertBuilder;
import com.github.wnder.GuessPreviewActivity;
import com.github.wnder.R;
import com.github.wnder.networkService.NetworkService;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

import static com.github.wnder.guessLocation.MapBoxHelper.drawCircle;
import static com.github.wnder.guessLocation.MapBoxHelper.updateCircle;
import static com.github.wnder.guessLocation.MapBoxHelper.zoomFromKilometers;

@AndroidEntryPoint
public class GuessFragment extends Fragment implements OnSeekBarChangeListener, OnMapReadyCallback {
    //Different distances for the radius
    private final int[] distances = {5, 10, 20, 50, 100, 500, 1000};

    @Inject
    public NetworkService networkInfo;

    private TextView radiusTextView;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private LatLng cameraPosition;
    private User user;

    public GuessFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState){

        Mapbox.getInstance(getActivity(), getString(R.string.mapbox_access_token));
        View rootView = inflater.inflate(R.layout.fragment_seekbar, container, false);

        user = GlobalUser.getUser();

        mapView = rootView.findViewById(R.id.mapViewFragment);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            AlertBuilder.okAlert(getString(R.string.gps_missing_title), getString(R.string.gps_missing_body), getContext()).show();
            return null;
        }

        double cameraLat = user.getPositionFromGPS((LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE), getActivity()).getLatitude();
        double cameraLng = user.getPositionFromGPS((LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE), getActivity()).getLongitude();
        cameraPosition = new LatLng(cameraLat, cameraLng);

        SeekBar radiusSeekBar = rootView.findViewById(R.id.radiusSeekBar);
        radiusTextView = rootView.findViewById(R.id.radiusTextView);
        manageSeekBar(radiusSeekBar, radiusTextView);

        FloatingActionButton guessButton = rootView.findViewById(R.id.navigationToGuessButton);
        guessButton.setOnClickListener((view) -> openPreviewActivity());

        return rootView;
    }

    private void setZoom(){
        //Set camera position
        CameraPosition position = new CameraPosition.Builder()
                .target(cameraPosition)
                .zoom(zoomFromKilometers(cameraPosition, user.getRadius()))
                .build();
        mapboxMap.setCameraPosition(position);
    }


    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        setZoom();

        //Set mapbox style
        mapboxMap.getUiSettings().setCompassEnabled(false); //Hide the default mapbox compass because we use our compass

        mapboxMap.setStyle(Style.SATELLITE_STREETS, this::onStyleLoaded);
    }

    /**
     * To be executed when the style has loaded
     * @param style on the mapbox map
     */
    private void onStyleLoaded(Style style){
        drawCircle(getActivity(), mapboxMap, cameraPosition);
    }

    private void openPreviewActivity() {
        if(networkInfo.isNetworkAvailable()){
            Intent intent = new Intent(getActivity(), GuessPreviewActivity.class);
            startActivity(intent);
        }
        else{
            AlertDialog alert = AlertBuilder.okAlert(getString(R.string.no_connection), getString(R.string.no_internet_guess), getActivity());
            alert.show();
        }
    }

    /**
     * Manage the SeekBar
     */
    private void manageSeekBar(SeekBar radiusSeekBar, TextView radiusTextView){
        //Set radius seekbar depending on user selected radius
        int userRad = user.getRadius();
        for(int i = 0; i < distances.length; i++){
            if(userRad == distances[i]){
                radiusSeekBar.setProgress(i);
                radiusTextView.setText(getString(R.string.set_radius, distances[i]));
                break;
            }
        }
        radiusSeekBar.setOnSeekBarChangeListener(this);
    }

    /**
     * When user interacts with radius seekbar
     * @param seekBar radius seekbar
     * @param progress step of seekbar
     * @param fromUser boolean
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        user.setRadius(distances[progress]);
        radiusTextView.setText(getString(R.string.set_radius, distances[progress]));
        setZoom();
        updateCircle(getActivity(), mapboxMap, cameraPosition);
    }

    //Necessary overwrites for MapView lifecycle methods
    /**
     * start mapbox
     */
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    /**
     * resume mapbox
     */
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * pause mapbox
     */
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * stop mapbox
     */
    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    /**
     * save mapbox instance state
     * @param outState output
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * when memory is low
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    /**
     * on mapbox destruction
     */
    @Override
    public void onDestroy() {
        //unregister listener!
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * To override for seekbar
     * @param seekBar
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //do nothing
    }

    /**
     * To override for seekbar
     * @param seekBar
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //do nothing
    }
}
