package com.github.wnder;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.github.wnder.networkService.NetworkService;
import com.github.wnder.user.GlobalUser;
import com.github.wnder.user.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SeekbarFragment extends Fragment implements OnSeekBarChangeListener {
    //Different distances for the radius
    private final int[] distances = {5, 10, 20, 50, 100, 500, 1000};

    @Inject
    public NetworkService networkInfo;

    private TextView radiusTextView;
    private FloatingActionButton guessButton;

    public SeekbarFragment() {
        super(R.layout.fragment_seekbar);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_seekbar, container, false);

        SeekBar radiusSeekBar = (SeekBar) rootView.findViewById(R.id.radiusSeekBar);

        radiusTextView = rootView.findViewById(R.id.radiusTextView);

        guessButton = rootView.findViewById(R.id.navigationToGuessButton);

        guessButton.setOnClickListener((view) -> openPreviewActivity());

        manageSeekBar(radiusSeekBar, radiusTextView);

        return rootView;
    }

    private void openPreviewActivity() {
        if(networkInfo.isNetworkAvailable()){
            Intent intent = new Intent(getActivity(), GuessPreviewActivity.class);
            startActivity(intent);
        }
        else{
            AlertDialog alert = AlertBuilder.noConnectionAlert(getString(R.string.no_connection), getString(R.string.no_internet_guess), getActivity());
            alert.show();
        }
    }
    /**
     * Manage the SeekBar
     */
    private void manageSeekBar(SeekBar radiusSeekBar, TextView radiusTextView){
        //Set radius seekbar depending on user selected radius
        int userRad = GlobalUser.getUser().getRadius();
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
        User u = GlobalUser.getUser();
        u.setRadius(distances[progress]);
        radiusTextView.setText(getString(R.string.set_radius, distances[progress]));
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
